package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.dto.ProcessCreateRequest;
import com.gw.api.v1.dto.ProcessResponse;
import com.gw.api.v1.dto.ProcessSummary;
import com.gw.api.v1.dto.ProcessUpdateRequest;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStartRequest;
import com.gw.api.v1.dto.RunStartResponse;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.tools.EnvironmentTool;
import com.gw.tools.ExecutionTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Agent API process surface: create/edit/run Python and Shell processes on localhost only.
 * Jupyter/builtin/remote SSH are intentionally not exposed here.
 */
@Service
public class AgentProcessService {

  private static final Logger logger = LoggerFactory.getLogger(AgentProcessService.class);
  private static final Set<String> ALLOWED_LANGS = Set.of("python", "shell");

  private final ProcessRepository processRepository;
  private final HistoryRepository historyRepository;
  private final ProcessTool processTool;
  private final ExecutionTool executionTool;
  private final AgentRunSupport runSupport;
  private final AgentRunIdempotencyStore idempotencyStore;
  private final AgentRunRateLimiter rateLimiter;

  public AgentProcessService(
      ProcessRepository processRepository,
      HistoryRepository historyRepository,
      ProcessTool processTool,
      ExecutionTool executionTool,
      AgentRunSupport runSupport,
      AgentRunIdempotencyStore idempotencyStore,
      AgentRunRateLimiter rateLimiter) {
    this.processRepository = processRepository;
    this.historyRepository = historyRepository;
    this.processTool = processTool;
    this.executionTool = executionTool;
    this.runSupport = runSupport;
    this.idempotencyStore = idempotencyStore;
    this.rateLimiter = rateLimiter;
  }

  public List<ProcessSummary> listProcesses() {
    List<ProcessSummary> out = new ArrayList<>();
    for (GWProcess p : processRepository.findAll()) {
      String lang = langOf(p);
      if (!ALLOWED_LANGS.contains(normalizeLang(lang))) {
        continue; // hide non-python/shell from agent inventory
      }
      out.add(new ProcessSummary(p.getId(), p.getName(), lang, p.getOwner()));
    }
    return out;
  }

  public ProcessResponse getProcess(String id) {
    return toResponse(requirePythonOrShellProcess(id));
  }

  public ProcessResponse createProcess(ProcessCreateRequest req) {
    if (req == null || BaseTool.isNull(req.name()) || BaseTool.isNull(req.code())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and code are required");
    }
    String lang = requireAllowedLang(BaseTool.isNull(req.lang()) ? "python" : req.lang());
    GWProcess p = new GWProcess();
    String id = new RandomString(6).nextString();
    p.setId(id);
    p.setName(req.name());
    p.setCode(req.code());
    p.setLang(lang);
    p.setDescription(BaseTool.isNull(req.description()) ? lang : req.description());
    p.setOwner("111111");
    p.setConfidential("FALSE");
    processTool.save(p);
    logger.info(
        "Agent API created process id={} lang={} fingerprint={}",
        id,
        lang,
        AgentApiAuthContext.getTokenFingerprint());
    return toResponse(p);
  }

  public ProcessResponse updateProcess(String id, ProcessUpdateRequest req) {
    GWProcess p = requirePythonOrShellProcess(id);
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
    }
    if (!BaseTool.isNull(req.name())) {
      p.setName(req.name());
    }
    if (!BaseTool.isNull(req.code())) {
      p.setCode(req.code());
    }
    if (!BaseTool.isNull(req.lang())) {
      String lang = requireAllowedLang(req.lang());
      p.setLang(lang);
      p.setDescription(lang);
    }
    if (!BaseTool.isNull(req.description())) {
      p.setDescription(req.description());
    }
    processTool.update(p);
    logger.info(
        "Agent API updated process id={} fingerprint={}",
        id,
        AgentApiAuthContext.getTokenFingerprint());
    return toResponse(requirePythonOrShellProcess(id));
  }

  public RunStartResponse startRun(String processId, RunStartRequest req) {
    requirePythonOrShellProcess(processId);
    String fp = AgentApiAuthContext.getTokenFingerprint();
    if (!rateLimiter.tryAcquire(fp)) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Run rate limit exceeded for this token");
    }

    String clientRunKey = req == null ? null : req.clientRunKey();
    String existing = idempotencyStore.lookup(fp, clientRunKey);
    if (existing != null) {
      try {
        History h = runSupport.requireHistory(existing);
        return new RunStartResponse(
            existing, AgentExecutionStatuses.normalize(h.getIndicator()), true);
      } catch (ResponseStatusException e) {
        // stale key
      }
    }

    String hostId =
        runSupport.requireLocalhostHostId(req == null ? null : req.hostId());
    String password = runSupport.prepareLocalhostPassword(req == null ? null : req.hostPassword());
    String envId = req == null ? null : req.envId();

    String historyId = new RandomString(12).nextString();
    try {
      String bin = null;
      String pyenv = null;
      String basedir = null;
      if (!BaseTool.isNull(envId)) {
        EnvironmentTool envt = BeanTool.getBean(EnvironmentTool.class);
        Environment envobj = envt.getEnvironmentById(envId);
        if (envobj != null) {
          bin = envobj.getBin();
          pyenv = envobj.getPyenv();
          basedir = envobj.getBasedir();
        }
      }
      executionTool.executeProcess(
          historyId,
          processId,
          hostId,
          password,
          "agent-api",
          false,
          bin,
          pyenv,
          basedir);
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      throw mapRunFailure(e, "processId=" + processId, fp);
    } finally {
      runSupport.clearLocalhostBypass();
    }

    idempotencyStore.put(fp, clientRunKey, historyId);
    String status = ExecutionStatus.RUNNING;
    try {
      History h = runSupport.requireHistory(historyId);
      status = AgentExecutionStatuses.normalize(h.getIndicator());
      if (BaseTool.isNull(status) || ExecutionStatus.UNKOWN.equals(status)) {
        status = ExecutionStatus.RUNNING;
      }
    } catch (ResponseStatusException ignored) {
      // async start may not have flushed history yet
    }
    logger.info(
        "Agent API started run historyId={} processId={} fingerprint={}",
        historyId,
        processId,
        fp);
    return new RunStartResponse(historyId, status, false);
  }

  public RunResponse getRun(String historyId, Integer logLimit) {
    return runSupport.getRun(historyId, logLimit);
  }

  public List<RunResponse> listRunsForProcess(String processId, Integer logLimit) {
    requirePythonOrShellProcess(processId);
    List<History> histories = historyRepository.findByProcessIdFull(processId);
    List<RunResponse> out = new ArrayList<>();
    if (histories == null) {
      return out;
    }
    int n = 0;
    for (History h : histories) {
      out.add(runSupport.toRunResponse(h, logLimit == null ? 0 : Math.min(logLimit, 4000)));
      if (++n >= 50) {
        break;
      }
    }
    return out;
  }

  public RunResponse stopRun(String historyId) {
    return runSupport.stopRun(historyId);
  }

  private GWProcess requirePythonOrShellProcess(String id) {
    Optional<GWProcess> opt = processRepository.findById(id);
    if (opt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found");
    }
    GWProcess p = opt.get();
    if (!ALLOWED_LANGS.contains(normalizeLang(langOf(p)))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Agent API supports only python and shell processes");
    }
    return p;
  }

  private static String requireAllowedLang(String lang) {
    String n = normalizeLang(lang);
    if (!ALLOWED_LANGS.contains(n)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "lang must be python or shell");
    }
    return n;
  }

  private static String normalizeLang(String lang) {
    return lang == null ? "" : lang.trim().toLowerCase(Locale.ROOT);
  }

  private static String langOf(GWProcess p) {
    if (!BaseTool.isNull(p.getLang())) {
      return p.getLang();
    }
    return p.getDescription();
  }

  private ProcessResponse toResponse(GWProcess p) {
    return new ProcessResponse(
        p.getId(), p.getName(), langOf(p), p.getDescription(), p.getCode(), p.getOwner());
  }

  private ResponseStatusException mapRunFailure(Exception e, String target, String fp) {
    String msg = e.getMessage() == null ? "" : e.getMessage();
    logger.warn("Agent API run failed {} fingerprint={} msg={}", target, fp, msg);
    String lower = msg.toLowerCase(Locale.ROOT);
    if (lower.contains("authentication") || lower.contains("wrong password")) {
      return new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Localhost password authentication failed");
    }
    return new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start run: " + msg);
  }
}
