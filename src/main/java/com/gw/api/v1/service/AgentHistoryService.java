package com.gw.api.v1.service;

import com.gw.utils.RunLogStore;
import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.dto.RunResponse;
import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.utils.BaseTool;
import com.gw.utils.ProcessStatusCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Agent API run history across processes and workflows: recent/active runs, notes, cleanup. */
@Service
public class AgentHistoryService {

  private static final Logger logger = LoggerFactory.getLogger(AgentHistoryService.class);
  static final int MAX_LIST = 500;
  private static final int MAX_NOTES = 10_000;

  private final HistoryRepository historyRepository;
  private final AgentRunSupport runSupport;
  private final ProcessStatusCache processStatusCache;

  public AgentHistoryService(
      HistoryRepository historyRepository,
      AgentRunSupport runSupport,
      ProcessStatusCache processStatusCache) {
    this.historyRepository = historyRepository;
    this.runSupport = runSupport;
    this.processStatusCache = processStatusCache;
  }

  /**
   * Newest runs first. {@code status} filters by normalized status (e.g. Running, Failed); logs are
   * omitted unless {@code logLimit} is set (capped at 4000 characters per run).
   */
  public List<RunResponse> listRuns(Integer limit, String status, Integer logLimit) {
    int n = limit == null ? 50 : Math.max(1, Math.min(MAX_LIST, limit));
    List<History> rows;
    if (BaseTool.isNull(status)) {
      rows = historyRepository.findLatest(n);
    } else {
      if (!AgentExecutionStatuses.isKnown(status)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unknown status " + status + ". See GET /api/v1/capabilities");
      }
      String wanted = AgentExecutionStatuses.normalize(status.trim());
      rows = historyRepository.findLatestByIndicator(wanted, n);
    }
    int log = logLimit == null ? 0 : Math.max(0, Math.min(4000, logLimit));
    List<RunResponse> out = new ArrayList<>();
    for (History h : rows) {
      out.add(runSupport.toRunResponse(h, log, false));
    }
    return out;
  }

  /** Deletes a finished run; a workflow run also removes its step runs. */
  @Transactional
  public Map<String, Object> deleteRun(String historyId) {
    History h =
        historyRepository
            .findById(historyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    RunResponse r = runSupport.toRunResponse(runSupport.requireHistory(historyId), 0, false);
    if (!r.terminal()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Run " + historyId + " is " + r.status() + "; stop it first (POST /api/v1/runs/" + historyId + "/stop)");
    }
    List<String> deleted = new ArrayList<>();
    if (r.workflowRun() && !BaseTool.isNull(h.getHistory_output())) {
      for (String child : Arrays.asList(h.getHistory_output().split(";"))) {
        String id = child.trim();
        if (!id.isEmpty() && historyRepository.existsById(id)) {
          historyRepository.deleteById(id);
          processStatusCache.removeStatus(id);
          deleted.add(id);
        }
      }
    }
    historyRepository.deleteById(historyId);
    processStatusCache.removeStatus(historyId);
    logger.info(
        "Agent API deleted run {} (+{} steps) fingerprint={}",
        historyId,
        deleted.size(),
        AgentApiAuthContext.getTokenFingerprint());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("historyId", historyId);
    out.put("deleted", true);
    out.put("deletedSteps", deleted);
    return out;
  }

  /** Bulk cleanup of Failed or Skipped runs, optionally for one process. */
  @Transactional
  public Map<String, Object> deleteRuns(String status, String processId) {
    String wanted = BaseTool.isNull(status) ? "" : AgentExecutionStatuses.normalize(status.trim());
    if (ExecutionStatus.FAILED.equals(wanted)) {
      List<String> ids;
      if (BaseTool.isNull(processId)) {
        ids = historyRepository.findIdsByIndicator(ExecutionStatus.FAILED);
        historyRepository.deleteByIndicator(ExecutionStatus.FAILED);
      } else {
        ids = historyRepository.findIdsByProcessAndIndicator(processId, ExecutionStatus.FAILED);
        historyRepository.deleteByProcessAndIndicator(processId, ExecutionStatus.FAILED);
      }
      ids.forEach(RunLogStore::delete);
    } else if (ExecutionStatus.SKIPPED.equals(wanted)) {
      if (BaseTool.isNull(processId)) {
        historyRepository.deleteSkippedHistory();
      } else {
        historyRepository.deleteSkippedHistoryByProcess(processId);
      }
    } else {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "status must be Failed or Skipped (bulk delete of other runs is not offered)");
    }
    logger.info(
        "Agent API deleted {} runs processId={} fingerprint={}",
        wanted,
        processId,
        AgentApiAuthContext.getTokenFingerprint());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("status", wanted);
    out.put("processId", processId);
    out.put("deleted", true);
    return out;
  }

  public RunResponse setNotes(String historyId, String notes) {
    History h =
        historyRepository
            .findById(historyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    AgentSearch.requireNoMarkup(notes, "notes");
    if (notes != null && notes.length() > MAX_NOTES) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "notes must be at most " + MAX_NOTES + " characters");
    }
    h.setHistory_notes(notes == null ? "" : notes);
    historyRepository.save(h);
    return runSupport.toRunResponse(runSupport.requireHistory(historyId), 0, false);
  }
}
