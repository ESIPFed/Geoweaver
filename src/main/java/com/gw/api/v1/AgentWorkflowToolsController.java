package com.gw.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gw.api.v1.dto.WorkflowResponse;
import com.gw.api.v1.service.AgentWorkflowBundleService;
import com.gw.api.v1.service.AgentWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Workflow step skipping, checkpoints (roll back to a past run) and JSON export/import. */
@RestController
@RequestMapping("/api/v1/workflows")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Tag(name = "Agent API: workflow tools")
public class AgentWorkflowToolsController {

  private final AgentWorkflowService workflowService;
  private final AgentWorkflowBundleService bundleService;

  public AgentWorkflowToolsController(
      AgentWorkflowService workflowService, AgentWorkflowBundleService bundleService) {
    this.workflowService = workflowService;
    this.bundleService = bundleService;
  }

  @PutMapping("/{id}/steps/{nodeId}")
  @Operation(summary = "Body {skip:true|false}: skip this node on later runs (shown as Skipped)")
  public WorkflowResponse setStep(
      @PathVariable("id") String id,
      @PathVariable("nodeId") String nodeId,
      @RequestBody Map<String, Boolean> body) {
    return workflowService.setStepSkipped(id, nodeId, body == null ? null : body.get("skip"));
  }

  @GetMapping("/{id}/checkpoints")
  @Operation(summary = "Graph+code snapshots saved at each run, newest first")
  public List<Map<String, Object>> checkpoints(@PathVariable("id") String id) {
    return workflowService.listCheckpoints(id);
  }

  @PostMapping("/{id}/restore")
  @Operation(summary = "Body {runId}: restore the graph and step code used by that run")
  public WorkflowResponse restore(
      @PathVariable("id") String id, @RequestBody Map<String, String> body) {
    return workflowService.restoreCheckpoint(id, body == null ? null : body.get("runId"));
  }

  @GetMapping("/{id}/export")
  @Operation(summary = "JSON bundle: the workflow plus the code of every process it uses")
  public ObjectNode export(@PathVariable("id") String id) {
    return bundleService.export(id);
  }

  @PostMapping("/import")
  @Operation(
      summary =
          "Import a bundle from /export (ids kept). 409 if different content exists unless"
              + " replace=true")
  public Map<String, Object> importBundle(
      @RequestBody JsonNode bundle,
      @RequestParam(value = "replace", required = false, defaultValue = "false") boolean replace) {
    return bundleService.importBundle(bundle, replace);
  }
}
