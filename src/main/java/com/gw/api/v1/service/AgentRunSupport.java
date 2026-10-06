package com.gw.api.v1.service;

import com.gw.GeoweaverApplication;
import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.dto.RunResponse;
import com.gw.database.HistoryRepository;
import com.gw.jpa.History;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.ProcessStatusCache;
import java.text.SimpleDateFormat;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Shared localhost gate + history mapping for Agent API process/workflow runs. */
@Component
public class AgentRunSupport {

  public static final String LOCALHOST_ID = "100001";
  private static final int DEFAULT_LOG_LIMIT = 200_000;

  private final AgentApiProperties properties;
  private final HistoryRepository historyRepository;
  private final HistoryTool historyTool;
  private final ProcessTool processTool;
  private final ProcessStatusCache processStatusCache;

  public AgentRunSupport(
      AgentApiProperties properties,
      HistoryRepository historyRepository,
      HistoryTool historyTool,
      ProcessTool processTool,
      ProcessStatusCache processStatusCache) {
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
    String raw = h.getIndicator();
    String status = AgentExecutionStatuses.normalize(raw);
    String log = h.getHistory_output() == null ? "" : h.getHistory_output();
    int max = logLimit == null ? DEFAULT_LOG_LIMIT : Math.max(0, logLimit);
    boolean truncated = false;
    if (max == 0) {
      log = "";
    } else if (log.length() > max) {
      log = log.substring(log.length() - max);
      truncated = true;
    }
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    String begin = h.getHistory_begin_time() == null ? null : fmt.format(h.getHistory_begin_time());
    String end = h.getHistory_end_time() == null ? null : fmt.format(h.getHistory_end_time());
    return new RunResponse(
        h.getHistory_id(),
        h.getHistory_process(),
        h.getHost_id(),
        status,
        raw,
        AgentExecutionStatuses.isTerminal(status),
        begin,
        end,
        log,
        truncated);
  }

  public RunResponse getRun(String historyId, Integer logLimit) {
    return toRunResponse(requireHistory(historyId), logLimit);
  }

  public RunResponse stopRun(String historyId) {
    requireHistory(historyId);
    processTool.stop(historyId);
    return toRunResponse(requireHistory(historyId), 0);
  }
}
