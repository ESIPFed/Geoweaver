package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.api.v1.dto.ProcessCreateRequest;
import com.gw.api.v1.dto.ProcessResponse;
import com.gw.api.v1.dto.ProcessSummary;
import com.gw.api.v1.dto.ProcessUpdateRequest;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStartRequest;
import com.gw.api.v1.dto.RunStartResponse;
import com.gw.database.HistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.Workflow;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.tools.ExecutionTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Agent API process surface: create/edit/run Python, Shell and builtin processes on localhost or a
 * registered SSH host. Jupyter notebook processes are not runnable by Geoweaver's runners.
 */
@Service
public class AgentProcessService {

  private static final Logger logger = LoggerFactory.getLogger(AgentProcessService.class);
  private static final Set<String> ALLOWED_LANGS = Set.of("python", "shell", "builtin");

  private final ProcessRepository processRepository;
  private final HistoryRepository historyRepository;
  private final ProcessTool processTool;
  private final ExecutionTool executionTool;
  private final AgentRunSupport runSupport;
  private final AgentRunIdempotencyStore idempotencyStore;
  private final AgentRunRateLimiter rateLimiter;
  private final WorkspaceEventBroadcaster workspaceEvents;
  private final WorkflowRepository workflowRepository;
  private final ObjectMapper mapper = new ObjectMapper();

  public AgentProcessService(
      ProcessRepository processRepository,
      HistoryRepository historyRepository,
      ProcessTool processTool,
      ExecutionTool executionTool,
      AgentRunSupport runSupport,
      AgentRunIdempotencyStore idempotencyStore,
      AgentRunRateLimiter rateLimiter,
      WorkspaceEventBroadcaster workspaceEvents,
      WorkflowRepository workflowRepository) {
    this.workflowRepository = workflowRepository;
    this.processRepository = processRepository;
    this.historyRepository = historyRepository;
    this.processTool = processTool;
    this.executionTool = executionTool;
    this.runSupport = runSupport;
    this.idempotencyStore = idempotencyStore;
    this.rateLimiter = rateLimiter;
    this.workspaceEvents = workspaceEvents;
  }

  /** @param query optional case-insensitive filter on id, name and description */
  public List<ProcessSummary> listProcesses(String query) {
    List<ProcessSummary> out = new ArrayList<>();
    for (GWProcess p : processRepository.findAll()) {
      String lang = langOf(p);
      if (!ALLOWED_LANGS.contains(normalizeLang(lang))) {
        continue; // notebooks cannot run through Geoweaver's runners
      }
      if (!AgentSearch.matches(query, p.getId(), p.getName(), p.getDescription())) {
        continue;
      }
      out.add(new ProcessSummary(p.getId(), p.getName(), lang, p.getOwner()));
    }
    return out;
  }

  public ProcessResponse getProcess(String id) {
    return toResponse(requireRunnableProcess(id));
  }

  public ProcessResponse createProcess(ProcessCreateRequest req) {
    if (req == null || BaseTool.isNull(req.name()) || BaseTool.isNull(req.code())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and code are required");
    }
    requireSafeName(req.name());
    AgentSearch.requireNoMarkup(req.description(), "description");
    String lang = requireAllowedLang(BaseTool.isNull(req.lang()) ? "python" : req.lang());
    requireValidCode(lang, req.code());
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
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info(
        "Agent API created process id={} lang={} fingerprint={}",
        id,
        lang,
        fp);
    workspaceEvents.publishAgent("process.created", "process", id, p.getName(), null, fp);
    return toResponse(p);
  }

  public ProcessResponse updateProcess(String id, ProcessUpdateRequest req) {
    GWProcess p = requireRunnableProcess(id);
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
    }
    if (!BaseTool.isNull(req.name())) {
      requireSafeName(req.name());
      p.setName(req.name());
    }
    if (!BaseTool.isNull(req.lang())) {
      String lang = requireAllowedLang(req.lang());
      p.setLang(lang);
      p.setDescription(lang);
    }
    if (!BaseTool.isNull(req.code())) {
      p.setCode(req.code());
    }
    if (!BaseTool.isNull(req.code()) || !BaseTool.isNull(req.lang())) {
      requireValidCode(normalizeLang(langOf(p)), p.getCode());
    }
    if (!BaseTool.isNull(req.description())) {
      AgentSearch.requireNoMarkup(req.description(), "description");
      p.setDescription(req.description());
    }
    processTool.update(p);
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info(
        "Agent API updated process id={} fingerprint={}",
        id,
        fp);
    workspaceEvents.publishAgent("process.updated", "process", id, p.getName(), null, fp);
    return toResponse(requireRunnableProcess(id));
  }

  public RunStartResponse startRun(String processId, RunStartRequest req) {
    requireRunnableProcess(processId);
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

    AgentRunSupport.RunTarget target =
        runSupport.resolveRunTarget(
            req == null ? null : req.hostId(), req == null ? null : req.hostPassword());
    Environment env = runSupport.resolveEnvironment(req == null ? null : req.envId(), target.hostId());

    String historyId = new RandomString(12).nextString();
    try {
      executionTool.executeProcess(
          historyId,
          processId,
          target.hostId(),
          target.password(),
          "agent-api",
          false,
          env == null ? null : env.getBin(),
          env == null ? null : env.getPyenv(),
          env == null ? null : env.getBasedir());
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      throw runSupport.mapRunFailure(e, "processId=" + processId, target);
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
        "Agent API started run historyId={} processId={} host={} fingerprint={}",
        historyId,
        processId,
        target.hostId(),
        fp);
    return new RunStartResponse(historyId, status, false);
  }

  /**
   * Deletes a process. Refuses (409) while any workflow node references it so GUI workflows never
   * silently break; history rows are kept.
   */
  public Map<String, Object> deleteProcess(String id) {
    GWProcess p = requireRunnableProcess(id);
    List<String> usedBy = workflowsReferencing(id);
    if (!usedBy.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Process "
              + id
              + " is used by workflow(s) "
              + String.join(", ", usedBy)
              + ". Remove it from those workflows (PUT /api/v1/workflows/{id}) or delete them first.");
    }
    processTool.del(id);
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info("Agent API deleted process id={} fingerprint={}", id, fp);
    workspaceEvents.publishAgent("process.deleted", "process", id, p.getName(), null, fp);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", id);
    out.put("deleted", true);
    return out;
  }

  private List<String> workflowsReferencing(String processId) {
    List<String> out = new ArrayList<>();
    for (Workflow w : workflowRepository.findAll()) {
      if (BaseTool.isNull(w.getNodes())) {
        continue;
      }
      try {
        for (JsonNode n : mapper.readTree(w.getNodes())) {
          if (n.path("id").asText("").split("-")[0].equals(processId)) {
            out.add(w.getId());
            break;
          }
        }
      } catch (Exception e) {
        // Unparseable legacy graph: fall back to a substring check so we never under-report.
        if (w.getNodes().contains("\"" + processId + "-")) {
          out.add(w.getId());
        }
      }
    }
    return out;
  }

  public RunResponse getRun(String historyId, Integer logLimit) {
    return runSupport.getRun(historyId, logLimit);
  }

  public RunResponse getRun(
      String historyId, Integer logLimit, int waitSeconds, boolean includeStepLogs) {
    return runSupport.getRun(historyId, logLimit, waitSeconds, includeStepLogs);
  }

  public List<RunResponse> listRunsForProcess(String processId, Integer logLimit) {
    requireRunnableProcess(processId);
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

  GWProcess requireRunnableProcess(String id) {
    Optional<GWProcess> opt = processRepository.findById(id);
    if (opt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found");
    }
    GWProcess p = opt.get();
    if (!ALLOWED_LANGS.contains(normalizeLang(langOf(p)))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Agent API supports python, shell and builtin processes (this one is " + langOf(p) + ")");
    }
    return p;
  }

  private static String requireAllowedLang(String lang) {
    String n = normalizeLang(lang);
    if (!ALLOWED_LANGS.contains(n)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "lang must be python, shell or builtin");
    }
    return n;
  }

  private static final java.util.regex.Pattern SAFE_NAME =
      java.util.regex.Pattern.compile("^[A-Za-z0-9_][A-Za-z0-9._-]{0,99}$");

  /**
   * Runners use the process name as the script file name (and in the remote shell command line),
   * so only file-name-safe characters are accepted.
   */
  static void requireSafeName(String name) {
    if (name == null || !SAFE_NAME.matcher(name).matches() || name.contains("..")) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "name must be 1-100 characters of letters, digits, . _ - (it becomes the script file"
              + " name on the host), e.g. download_landsat or clean-data.py");
    }
  }

  /** Builtin processes store a JSON object naming the operation, e.g. {"operation":"AWS S3"}. */
  void requireValidCode(String lang, String code) {
    if (!"builtin".equals(lang)) {
      return;
    }
    try {
      JsonNode n = mapper.readTree(code);
      if (n == null || !n.isObject() || !n.path("operation").isTextual()) {
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "builtin code must be a JSON object with an \"operation\" string and optional"
              + " \"params\" array");
    }
  }

  static String normalizeLang(String lang) {
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

}
