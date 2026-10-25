package com.gw.api.v1.service;

import com.gw.GeoweaverApplication;
import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStep;
import com.gw.database.HistoryRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.ProcessStatusCache;
import jakarta.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Shared localhost gate + history mapping for Agent API process/workflow runs. */
@Component
public class AgentRunSupport {

  public static final String LOCALHOST_ID = "100001";
  private static final int DEFAULT_LOG_LIMIT = 200_000;
  private static final int STEP_LOG_LIMIT = 20_000;
  public static final int MAX_WAIT_SECONDS = 60;
  private static final long WAIT_POLL_MILLIS = 750;
  private static final long STEP_SETTLE_MILLIS = 5_000;

  private final AgentApiProperties properties;
  private final HistoryRepository historyRepository;
  private final HistoryTool historyTool;
  private final ProcessTool processTool;
  private final ProcessStatusCache processStatusCache;
  private final WorkflowRepository workflowRepository;
  private final EntityManager entityManager;

  public AgentRunSupport(
      AgentApiProperties properties,
      HistoryRepository historyRepository,
      HistoryTool historyTool,
      ProcessTool processTool,
      ProcessStatusCache processStatusCache,
      WorkflowRepository workflowRepository,
      EntityManager entityManager) {
    this.workflowRepository = workflowRepository;
    this.entityManager = entityManager;
    this.properties = properties;
    this.historyRepository = historyRepository;
    this.historyTool = historyTool;
    this.processTool = processTool;
    this.processStatusCache = processStatusCache;
  }

  /** MVP: only localhost. Rejects any other host id. */
  public String requireLocalhostHostId(String hostId) {
    if (BaseTool.isNull(hostId)
        || LOCALHOST_ID.equals(hostId.trim())
        || "localhost".equalsIgnoreCase(hostId.trim())) {
      return LOCALHOST_ID;
    }
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Agent API runs localhost only (hostId=100001). Remote SSH hosts are not exposed on /api/v1.");
  }

  /**
   * Ensures localhost host row exists and applies password policy. Caller must clear skip flag in
   * finally.
   */
  public String prepareLocalhostPassword(String hostPassword) {
    GeoweaverApplication.addLocalhost();
    boolean bypass = properties.isAllowLocalhostRuns();
    if (!bypass && BaseTool.isNull(hostPassword)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "localhost run requires hostPassword, or set geoweaver.agent.allow-localhost-runs=true");
    }
    AgentApiAuthContext.setSkipLocalhostPassword(bypass);
    return hostPassword == null ? "" : hostPassword;
  }

  public void clearLocalhostBypass() {
    AgentApiAuthContext.setSkipLocalhostPassword(false);
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
      return historyTool.getHistoryById(historyId);
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
    String log = h.getHistory_output() == null ? "" : h.getHistory_output();
    int max = logLimit == null ? DEFAULT_LOG_LIMIT : Math.max(0, logLimit);
    String[] tail = tail(log, max);
    boolean workflowRun =
        !BaseTool.isNull(h.getHistory_process())
            && workflowRepository.existsById(h.getHistory_process());
    List<RunStep> steps = workflowRun ? steps(h, includeStepLogs) : null;
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
        steps);
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
      // is flushed (see CHG-0004), so it is fresher while a step finishes.
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
        stepLog = tail(child.getHistory_output(), STEP_LOG_LIMIT)[0];
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
