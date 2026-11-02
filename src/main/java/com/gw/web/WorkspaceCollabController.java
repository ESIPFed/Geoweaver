package com.gw.web;

import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.workspace.AgentPresenceTracker;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.api.v1.workspace.WorkspaceLongPollHub;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.service.AgentRunSupport;
import com.gw.database.HistoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Google-Doc-style workspace collaboration transport for open GUI browsers: subscribe, long-poll
 * events, revision clock, and soft presence heartbeats. No secrets in responses.
 */
@RestController
@RequestMapping("/api/workspace")
public class WorkspaceCollabController {

  private final WorkspaceLongPollHub hub;
  private final WorkspaceEventBroadcaster broadcaster;
  private final AgentPresenceTracker presenceTracker;
  private final AgentApiProperties properties;
  private final AgentRunSupport runSupport;
  private final HistoryRepository historyRepository;

  public WorkspaceCollabController(
      WorkspaceLongPollHub hub,
      WorkspaceEventBroadcaster broadcaster,
      AgentPresenceTracker presenceTracker,
      AgentApiProperties properties,
      AgentRunSupport runSupport,
      HistoryRepository historyRepository) {
    this.hub = hub;
    this.broadcaster = broadcaster;
    this.presenceTracker = presenceTracker;
    this.properties = properties;
    this.runSupport = runSupport;
    this.historyRepository = historyRepository;
  }

  /**
   * Status of one run for the browser (no log text); for a workflow run, each step's status by
   * node id, so the canvas can show a run started by another Geoweaver (for example the command
   * line over SSH).
   */
  @GetMapping("/runs/{historyId}")
  public RunResponse run(@PathVariable String historyId) {
    return runSupport.toRunResponse(runSupport.requireHistory(historyId), 0);
  }

  /** Runs of a workflow or process that have not ended, whoever started them. */
  @GetMapping("/runs")
  public List<Map<String, String>> activeRuns(
      @RequestParam(value = "target", required = false) String target) {
    List<Map<String, String>> out = new ArrayList<>();
    for (Object[] r : historyRepository.findActiveRunHeads()) {
      if (target != null && !target.equals(r[1])) continue;
      Map<String, String> m = new HashMap<>();
      m.put("historyId", (String) r[0]);
      m.put("target", (String) r[1]);
      m.put("status", (String) r[2]);
      out.add(m);
    }
    return out;
  }

  public record SubscribeRequest(String clientToken) {}

  public record PresenceRequest(
      String clientToken, String objectKind, String objectId, String actor) {}

  @PostMapping("/subscribe")
  public ResponseEntity<Map<String, Object>> subscribe(@RequestBody SubscribeRequest body) {
    if (body == null || body.clientToken() == null || body.clientToken().isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "clientToken required"));
    }
    hub.subscribe(body.clientToken().trim());
    Map<String, Object> out = new HashMap<>();
    out.put("ok", true);
    out.put("revision", broadcaster.currentRevision());
    out.put("workspaceEventsEnabled", properties.isWorkspaceEventsEnabled());
    return ResponseEntity.ok(out);
  }

  @GetMapping("/poll/{clientToken}")
  public DeferredResult<ResponseEntity<?>> poll(@PathVariable String clientToken) {
    return hub.poll(clientToken);
  }

  /** Cheap reconciliation clock for clients (belt-and-suspenders if a poll is missed). */
  @GetMapping("/revision")
  public Map<String, Object> revision() {
    Map<String, Object> out = new HashMap<>();
    out.put("revision", broadcaster.currentRevision());
    out.put("workspaceEventsEnabled", properties.isWorkspaceEventsEnabled());
    out.put("presence", presenceTracker.snapshotLive());
    return out;
  }

  @PostMapping("/presence")
  public ResponseEntity<Map<String, Object>> presence(@RequestBody PresenceRequest body) {
    if (body == null || body.objectKind() == null || body.objectId() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "objectKind and objectId required"));
    }
    String actor = body.actor() == null || body.actor().isBlank() ? "human" : body.actor();
    presenceTracker.touch(body.objectKind(), body.objectId(), null, actor);
    if (body.clientToken() != null) {
      hub.subscribe(body.clientToken());
    }
    return ResponseEntity.ok(Map.of("ok", true, "revision", broadcaster.currentRevision()));
  }
}
