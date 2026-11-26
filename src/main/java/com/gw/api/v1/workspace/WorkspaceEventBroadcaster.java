package com.gw.api.v1.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.api.v1.AgentApiProperties;
import com.gw.server.WorkspaceServlet;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Publishes workspace change events to all GUI sessions via WebSocket <em>and</em> the dedicated
 * workspace long-poll hub (required because the GUI defaults to HTTP polling). Failures never roll
 * back the underlying save.
 */
@Component
public class WorkspaceEventBroadcaster {

  private static final Logger logger = LoggerFactory.getLogger(WorkspaceEventBroadcaster.class);

  private final AgentApiProperties properties;
  private final AgentPresenceTracker presenceTracker;
  private final WorkspaceLongPollHub longPollHub;
  private final ObjectMapper objectMapper;
  private final AtomicLong revision = new AtomicLong(0);

  /** Shares this server's changes with other Geoweaver processes on the same database. */
  private volatile java.util.function.Consumer<WorkspaceEvent> sharer;

  public WorkspaceEventBroadcaster(
      AgentApiProperties properties,
      AgentPresenceTracker presenceTracker,
      WorkspaceLongPollHub longPollHub,
      ObjectMapper objectMapper) {
    this.properties = properties;
    this.presenceTracker = presenceTracker;
    this.longPollHub = longPollHub;
    this.objectMapper = objectMapper;
  }

  public void setSharer(java.util.function.Consumer<WorkspaceEvent> sharer) {
    this.sharer = sharer;
  }

  /**
   * Passes an event from another Geoweaver process (or a run seen in the database) on to this
   * server's browsers, with this server's next revision. Not shared again.
   */
  public void relay(WorkspaceEvent event) {
    if (event == null || !properties.isWorkspaceEventsEnabled()) {
      return;
    }
    publish(event.withRevision(revision.incrementAndGet()));
  }

  private void share(WorkspaceEvent event) {
    java.util.function.Consumer<WorkspaceEvent> s = sharer;
    if (s == null) return;
    try {
      s.accept(event);
    } catch (RuntimeException e) {
      logger.debug("Sharing workspace event failed: {}", e.toString());
    }
  }

  public long currentRevision() {
    return revision.get();
  }

  public void publishAgent(
      String event,
      String objectKind,
      String objectId,
      String objectName,
      String workflowId,
      String tokenFingerprint) {
    if (!properties.isWorkspaceEventsEnabled()) {
      return;
    }
    long rev = revision.incrementAndGet();
    WorkspaceEvent e =
        WorkspaceEvent.agent(event, objectKind, objectId, objectName, workflowId, tokenFingerprint, rev);
    publish(e);
    share(e);
  }

  public void publishHuman(
      String event, String objectKind, String objectId, String objectName, String workflowId) {
    if (!properties.isWorkspaceEventsEnabled()) {
      return;
    }
    long rev = revision.incrementAndGet();
    WorkspaceEvent e = WorkspaceEvent.human(event, objectKind, objectId, objectName, workflowId, rev);
    publish(e);
    share(e);
  }

  public void publish(WorkspaceEvent event) {
    if (event == null || !properties.isWorkspaceEventsEnabled()) {
      return;
    }
    try {
      if (!"run".equals(event.objectKind())) { // runs are activity, not someone editing
        presenceTracker.touch(event.objectKind(), event.objectId(), event.tokenFingerprint(), event.actor());
        if (event.workflowId() != null && !"workflow".equals(event.objectKind())) {
          presenceTracker.touch("workflow", event.workflowId(), event.tokenFingerprint(), event.actor());
        }
      }
      String json = objectMapper.writeValueAsString(event);
      WorkspaceServlet.broadcast(json);
      longPollHub.broadcast(json);
      logger.debug(
          "Workspace event broadcast event={} revision={} actor={}",
          event.event(),
          event.revision(),
          event.actor());
    } catch (Exception e) {
      logger.warn(
          "Workspace event broadcast failed (save already succeeded): {}", e.toString());
    }
  }
}
