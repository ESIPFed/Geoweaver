package com.gw.api.v1.service;

import com.gw.utils.RunLogStore;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStep;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Host;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.InternalRunCredential;
import com.gw.utils.ProcessStatusCache;
import jakarta.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Shared host resolution, error mapping and history mapping for Agent API runs. */
@Component
public class AgentRunSupport {

  private static final Logger logger = LoggerFactory.getLogger(AgentRunSupport.class);

  public static final String LOCALHOST_ID = "100001";
  private static final int DEFAULT_LOG_LIMIT = 200_000;
  private static final int STEP_LOG_LIMIT = 20_000;
  public static final int MAX_WAIT_SECONDS = 60;
  private static final long WAIT_POLL_MILLIS = 750;
  private static final long STEP_SETTLE_MILLIS = 5_000;
  static final long ROW_FINISH_GRACE_MILLIS = 10_000;

  private final AgentApiProperties properties;
  private final HistoryRepository historyRepository;
  private final HistoryTool historyTool;
  private final ProcessTool processTool;
  private final ProcessStatusCache processStatusCache;
  private final WorkflowRepository workflowRepository;
  private final EntityManager entityManager;
  private final HostRepository hostRepository;
  private final EnvironmentRepository environmentRepository;
  // historyId -> when the cache first reported a terminal status the row did not have yet.
  private final Map<String, Long> cacheAheadSince = new ConcurrentHashMap<>();

  public AgentRunSupport(
      AgentApiProperties properties,
      HistoryRepository historyRepository,
      HistoryTool historyTool,
      ProcessTool processTool,
      ProcessStatusCache processStatusCache,
      WorkflowRepository workflowRepository,
      EntityManager entityManager,
      HostRepository hostRepository,
      EnvironmentRepository environmentRepository) {
    this.hostRepository = hostRepository;
    this.environmentRepository = environmentRepository;
    this.workflowRepository = workflowRepository;
    this.entityManager = entityManager;
    this.properties = properties;
    this.historyRepository = historyRepository;
    this.historyTool = historyTool;
    this.processTool = processTool;
    this.processStatusCache = processStatusCache;
  }

  /** Where a run executes and the password Geoweaver's runners should receive. */
  public record RunTarget(String hostId, String password, boolean local, String address) {}

  /**
   * Resolves the target host. Localhost (blank, {@code 100001} or {@code localhost}) runs with the
   * in-process credential when {@code allow-localhost-runs=true}, otherwise with the GUI localhost
   * password. A registered SSH host needs its SSH password; it is used for this run only.
   */
  public RunTarget resolveRunTarget(String hostId, String hostPassword) {
    String id = BaseTool.isNull(hostId) ? LOCALHOST_ID : hostId.trim();
    if (LOCALHOST_ID.equals(id) || "localhost".equalsIgnoreCase(id)) {
      GeoweaverApplication.addLocalhost();
      if (!BaseTool.isNull(hostPassword)) {
        return new RunTarget(LOCALHOST_ID, hostPassword, true, "127.0.0.1");
      }
      if (!properties.isAllowLocalhostRuns()) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "localhost run requires hostPassword, or set geoweaver.agent.allow-localhost-runs=true");
      }
      return new RunTarget(LOCALHOST_ID, InternalRunCredential.get(), true, "127.0.0.1");
    }
    if (!properties.isAllowRemoteHosts()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Remote hosts are disabled for the Agent API (geoweaver.agent.allow-remote-hosts=false)");
    }
    Host host =
        hostRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Host " + id + " not found. See GET /api/v1/hosts"));
    if ("127.0.0.1".equals(host.getIp()) || "localhost".equalsIgnoreCase(host.getIp())) {
      // Geoweaver runs hosts with a loopback ip through the local runner (GUI password rules).
      if (BaseTool.isNull(hostPassword) && !properties.isAllowLocalhostRuns()) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "localhost run requires hostPassword, or set geoweaver.agent.allow-localhost-runs=true");
      }
      return new RunTarget(
          id,
          BaseTool.isNull(hostPassword) ? InternalRunCredential.get() : hostPassword,
          true,
          host.getIp());
    }
    if (!BaseTool.isNull(host.getType()) && !"ssh".equalsIgnoreCase(host.getType())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Host " + id + " is type " + host.getType() + "; processes run on ssh hosts only");
    }
    if (BaseTool.isNull(hostPassword)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "hostPassword (SSH password for "
              + host.getUsername()
              + "@"
              + host.getIp()
              + ") is required for host "
              + id
              + "; it is used for this run only and never stored");
    }
    return new RunTarget(id, hostPassword, false, host.getIp());
  }

  /** Validates that {@code envId} (optional) is a Python environment of {@code hostId}. */
  public Environment resolveEnvironment(String envId, String hostId) {
    if (BaseTool.isNull(envId)) {
      return null;
    }
    Environment env =
        environmentRepository
            .findById(envId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Environment " + envId + " not found. See GET /api/v1/hosts/{id}/environments"));
    String envHost = env.getHostobj() == null ? null : env.getHostobj().getId();
    if (envHost != null && !envHost.equals(hostId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Environment " + envId + " belongs to host " + envHost);
    }
    return env;
  }

  /**
   * Maps a runner exception to a JSON error. The password is scrubbed from the message, and SSH
   * failures are reported by category so callers can tell bad credentials from an unreachable host.
   */
  public ResponseStatusException mapRunFailure(Exception e, String target, RunTarget run) {
    String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    if (run != null && !BaseTool.isNull(run.password())) {
      msg = msg.replace(run.password(), "***");
    }
    logger.warn(
        "Agent API run failed {} host={} fingerprint={} msg={}",
        target,
        run == null ? null : run.hostId(),
        AgentApiAuthContext.getTokenFingerprint(),
        msg);
    String lower = msg.toLowerCase(Locale.ROOT);
    boolean local = run == null || run.local();
    if (lower.contains("authentication") || lower.contains("wrong password") || lower.contains("auth fail")) {
      return new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          local ? "Localhost password authentication failed" : "SSH authentication failed for host " + run.hostId());
    }
    if (!local
        && (lower.contains("connect")
            || lower.contains("refused")
            || lower.contains("timed out")
            || lower.contains("unknownhost")
            || lower.contains("unknown host")
            || lower.contains("no route")
            // UnknownHostException's message is just the host name
            || (run.address() != null && lower.trim().equals(run.address().toLowerCase(Locale.ROOT))))) {
      return new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Could not reach SSH host " + run.hostId() + ": " + msg);
    }
    return new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start run: " + msg);
  }

  /**
   * Load a real history row (or cache-only mid-run). Missing ids return 404 — unlike {@link
   * HistoryTool#getHistoryById} which invents empty rows.
   */
  public History requireHistory(String historyId) {
    if (BaseTool.isNull(historyId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found");
    }
    Optional<History> row = historyRepository.findById(historyId);
    if (row.isPresent()) {
      // Read before getHistoryById: it overlays the cached status onto this same entity.
      String rowStatus = row.get().getIndicator();
      return holdUntilRowFinishes(historyTool.getHistoryById(historyId), rowStatus);
    }
    String cached = processStatusCache.getStatus(historyId);
    if (cached != null) {
      History h = new History();
      h.setHistory_id(historyId);
      h.setIndicator(cached);
      return h;
    }
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found");
  }

  /**
   * GeoweaverProcessTask marks a process Done in the cache when the command returns, while
   * LocalSessionOutput may still be saving the log. Until the row is final, report the row's
   * status so an agent never sees Done with an empty log. After {@link #ROW_FINISH_GRACE_MILLIS}
   * trust the cache, so a runner that died before saving cannot leave the run Running forever.
   */
  History holdUntilRowFinishes(History h, String rowStatus) {
    String id = h.getHistory_id();
    boolean cacheTerminal =
        AgentExecutionStatuses.isTerminal(AgentExecutionStatuses.normalize(h.getIndicator()));
    boolean rowTerminal =
        AgentExecutionStatuses.isTerminal(AgentExecutionStatuses.normalize(rowStatus));
    if (!cacheTerminal || rowTerminal || isWorkflowRun(h)) {
      cacheAheadSince.remove(id);
      return h;
    }
    long now = System.currentTimeMillis();
    long since = cacheAheadSince.computeIfAbsent(id, k -> now);
    if (now - since < ROW_FINISH_GRACE_MILLIS) {
      h.setIndicator(BaseTool.isNull(rowStatus) ? ExecutionStatus.RUNNING : rowStatus);
    }
    return h;
  }

  private boolean isWorkflowRun(History h) {
    return !BaseTool.isNull(h.getHistory_process())
        && workflowRepository.existsById(h.getHistory_process());
  }

  public RunResponse toRunResponse(History h, Integer logLimit) {
    return toRunResponse(h, logLimit, false);
  }

  /**
   * Maps a history row. Workflow runs also get {@code steps}: the parent row stores node ids in
   * {@code history_input} and child history ids in {@code history_output}, both ';'-separated.
   */
  public RunResponse toRunResponse(History h, Integer logLimit, boolean includeStepLogs) {
    String raw = h.getIndicator();
    String status = AgentExecutionStatuses.normalize(raw);
    int max = logLimit == null ? DEFAULT_LOG_LIMIT : Math.max(0, logLimit);
    // Large logs live in files; read one character more than needed so tail() can flag truncation.
    String log = RunLogStore.readTail(h.getHistory_id(), h.getHistory_output(), max == Integer.MAX_VALUE ? max : max + 1);
    if (log == null) log = "";
    String[] tail = tail(log, max);
    boolean workflowRun = isWorkflowRun(h);
    List<RunStep> steps = workflowRun ? steps(h, includeStepLogs) : null;
    // The engine can mark a workflow Done from a step's early Done before that step's exit code
    // is saved as Failed; a failed step always means a failed run.
    if (steps != null
        && ExecutionStatus.DONE.equals(status)
        && steps.stream().anyMatch(s -> ExecutionStatus.FAILED.equals(s.status()))) {
      status = ExecutionStatus.FAILED;
    }
    String hostId = h.getHost_id();
    if (hostId != null && hostId.endsWith(";")) {
      hostId = hostId.substring(0, hostId.length() - 1);
    }
    return new RunResponse(
        h.getHistory_id(),
        h.getHistory_process(),
        hostId,
        status,
        raw,
        AgentExecutionStatuses.isTerminal(status),
        format(h.getHistory_begin_time()),
        format(h.getHistory_end_time()),
        tail[0],
        tail[1] != null,
        workflowRun,
        steps,
        BaseTool.isNull(h.getHistory_notes()) ? null : h.getHistory_notes());
  }

  private List<RunStep> steps(History parent, boolean includeLogs) {
    String[] nodeIds = splitIds(parent.getHistory_input());
    String[] childIds = splitIds(parent.getHistory_output());
    List<RunStep> out = new ArrayList<>();
    for (int i = 0; i < nodeIds.length; i++) {
      String nodeId = nodeIds[i];
      String childId = i < childIds.length ? childIds[i] : null;
      String processId = nodeId.split("-")[0];
      History child = childId == null ? null : historyRepository.findById(childId).orElse(null);
      // A terminal row is final. Otherwise prefer the status cache: it is written before the row
      // is flushed, so it is fresher while a step finishes.
      String childStatus = child == null ? null : child.getIndicator();
      if (childId != null
          && !AgentExecutionStatuses.isTerminal(AgentExecutionStatuses.normalize(childStatus))) {
        String cached = processStatusCache.getStatus(childId);
        childStatus = BaseTool.isNull(cached) ? childStatus : cached;
      }
      String status =
          BaseTool.isNull(childStatus)
              ? ExecutionStatus.READY
              : AgentExecutionStatuses.normalize(childStatus);
      String stepLog = null;
      if (includeLogs && child != null && child.getHistory_output() != null) {
        stepLog =
            tail(RunLogStore.readTail(child.getHistory_id(), child.getHistory_output(), STEP_LOG_LIMIT + 1), STEP_LOG_LIMIT)[0];
      }
      out.add(
          new RunStep(
              nodeId,
              processId,
              childId,
              status,
              AgentExecutionStatuses.isTerminal(status),
              child == null ? null : format(child.getHistory_begin_time()),
              child == null ? null : format(child.getHistory_end_time()),
              stepLog));
    }
    return out;
  }

  private static String[] splitIds(String joined) {
    if (BaseTool.isNull(joined)) {
      return new String[0];
    }
    return Arrays.stream(joined.split(";"))
        .map(String::trim)
        .filter(x -> !x.isEmpty())
        .toArray(String[]::new);
  }

  /** Returns {tail, truncatedMarker}; marker non-null when the log was cut. */
  private static String[] tail(String log, int max) {
    if (max == 0) {
      return new String[] {"", null};
    }
    if (log.length() > max) {
      return new String[] {log.substring(log.length() - max), "truncated"};
    }
    return new String[] {log, null};
  }

  private static String format(Date d) {
    return d == null ? null : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(d);
  }

  public RunResponse getRun(String historyId, Integer logLimit) {
    return getRun(historyId, logLimit, 0, false);
  }

  /**
   * Run detail; when {@code waitSeconds > 0} blocks (capped at {@link #MAX_WAIT_SECONDS}) until the
   * run is terminal or the wait elapses, so agents need fewer polling calls.
   */
  public RunResponse getRun(
      String historyId, Integer logLimit, int waitSeconds, boolean includeStepLogs) {
    int wait = Math.max(0, Math.min(MAX_WAIT_SECONDS, waitSeconds));
    long deadline = System.currentTimeMillis() + wait * 1000L;
    long terminalSince = -1;
    while (true) {
      RunResponse r = toRunResponse(requireHistory(historyId), logLimit, includeStepLogs);
      if (r.terminal() && terminalSince < 0) {
        terminalSince = System.currentTimeMillis();
      }
      // Step rows lag the parent by about a second; steps that never started (stopped workflow)
      // never get a final row, so stop waiting for them shortly after the parent is final.
      boolean settled =
          r.terminal()
              && (wait == 0
                  || stepRowsSettled(r)
                  || System.currentTimeMillis() - terminalSince >= STEP_SETTLE_MILLIS);
      if (settled || System.currentTimeMillis() >= deadline) {
        return r;
      }
      try {
        Thread.sleep(WAIT_POLL_MILLIS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return r;
      }
      // Open-session-in-view keeps one persistence context per request; without clearing it,
      // re-reads return the first snapshot and the run never looks finished. GET-only: no writes.
      entityManager.clear();
    }
  }

  /** True when every child history row of a workflow run has a terminal status in the database. */
  private boolean stepRowsSettled(RunResponse r) {
    if (r.steps() == null) {
      return true;
    }
    for (RunStep step : r.steps()) {
      if (step.historyId() == null) {
        return false;
      }
      History row = historyRepository.findById(step.historyId()).orElse(null);
      if (row == null
          || !AgentExecutionStatuses.isTerminal(
              AgentExecutionStatuses.normalize(row.getIndicator()))) {
        return false;
      }
    }
    return true;
  }

  public RunResponse stopRun(String historyId) {
    requireHistory(historyId);
    processTool.stop(historyId);
    return toRunResponse(requireHistory(historyId), 0);
  }
}
