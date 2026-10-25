package com.gw.api.v1;

import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.service.AgentHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Run history across all processes and workflows (detail, wait and stop live on /runs/{id}). */
@RestController
@RequestMapping("/api/v1/runs")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Tag(name = "Agent API: run history")
public class AgentRunHistoryController {

  private final AgentHistoryService historyService;
  private final AgentApiProperties properties;

  public AgentRunHistoryController(AgentHistoryService historyService, AgentApiProperties properties) {
    this.historyService = historyService;
    this.properties = properties;
  }

  @GetMapping
  @Operation(
      summary =
          "Newest runs first. status=Running lists active runs; limit 1..500 (default 50);"
              + " logLimit adds up to 4000 log characters per run")
  public List<RunResponse> list(
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "logLimit", required = false) Integer logLimit) {
    return historyService.listRuns(limit, status, logLimit);
  }

  @DeleteMapping("/{historyId}")
  @Operation(summary = "Delete a finished run (a workflow run also removes its step runs)")
  public Map<String, Object> delete(@PathVariable("historyId") String historyId) {
    AgentApiController.requireDeleteAllowed(properties);
    return historyService.deleteRun(historyId);
  }

  @DeleteMapping
  @Operation(summary = "Bulk delete: status=Failed or Skipped, optionally processId=...")
  public Map<String, Object> deleteMany(
      @RequestParam("status") String status,
      @RequestParam(value = "processId", required = false) String processId) {
    AgentApiController.requireDeleteAllowed(properties);
    return historyService.deleteRuns(status, processId);
  }

  @PutMapping("/{historyId}/notes")
  @Operation(summary = "Set the run's notes (shown in the GUI history). Body {notes}")
  public RunResponse notes(
      @PathVariable("historyId") String historyId, @RequestBody Map<String, String> body) {
    return historyService.setNotes(historyId, body == null ? null : body.get("notes"));
  }
}
