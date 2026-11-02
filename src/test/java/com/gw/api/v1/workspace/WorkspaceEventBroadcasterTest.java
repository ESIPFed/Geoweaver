package com.gw.api.v1.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.api.v1.AgentApiProperties;
import com.gw.server.WorkspaceServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

class WorkspaceEventBroadcasterTest {

  @AfterEach
  void clear() {
    WorkspaceServlet.clearPeersForTests();
  }

  private WorkspaceEventBroadcaster broadcaster(boolean enabled, WorkspaceLongPollHub hub) {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.isWorkspaceEventsEnabled()).thenReturn(enabled);
    return new WorkspaceEventBroadcaster(
        props, new AgentPresenceTracker(), hub, new ObjectMapper());
  }

  @Test
  void killSwitchSkipsBroadcast() {
    WorkspaceLongPollHub hub = new WorkspaceLongPollHub();
    WorkspaceEventBroadcaster b = broadcaster(false, hub);
    b.publishAgent("process.created", "process", "abc", "n", null, "fp12");
    assertThat(b.currentRevision()).isEqualTo(0);
    assertThat(hub.subscriberCount()).isZero();
  }

  @Test
  void publishIncrementsRevisionAndFansOutLongPoll() throws Exception {
    WorkspaceLongPollHub hub = new WorkspaceLongPollHub();
    hub.subscribe("client-token-1");
    DeferredResult<ResponseEntity<?>> poll = hub.poll("client-token-1");
    WorkspaceEventBroadcaster b = broadcaster(true, hub);
    b.publishAgent("process.created", "process", "abc", "Demo", null, "fp12");
    assertThat(b.currentRevision()).isEqualTo(1);
    assertThat(poll.getResult()).isNotNull();
    @SuppressWarnings("unchecked")
    ResponseEntity<Object> entity = (ResponseEntity<Object>) poll.getResult();
    assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
    Object body = entity.getBody();
    assertThat(body).isInstanceOf(String[].class);
    String json = ((String[]) body)[0];
    assertThat(json).contains("process.created").contains("\"revision\":1").contains("agent");
  }

  @Test
  void humanPublishUsesHumanActor() throws Exception {
    WorkspaceLongPollHub hub = new WorkspaceLongPollHub();
    hub.subscribe("t2");
    DeferredResult<ResponseEntity<?>> poll = hub.poll("t2");
    WorkspaceEventBroadcaster b = broadcaster(true, hub);
    b.publishHuman("workflow.updated", "workflow", "wf1", "Stock", "wf1");
    @SuppressWarnings("unchecked")
    ResponseEntity<Object> entity = (ResponseEntity<Object>) poll.getResult();
    String json = ((String[]) entity.getBody())[0];
    assertThat(json).contains("\"actor\":\"human\"").contains("workflow.updated");
  }

  @Test
  void localChangesAreSharedAndRelayedChangesAreNotSharedAgain() {
    WorkspaceLongPollHub hub = new WorkspaceLongPollHub();
    WorkspaceEventBroadcaster b = broadcaster(true, hub);
    java.util.List<WorkspaceEvent> shared = new java.util.ArrayList<>();
    b.setSharer(shared::add);
    b.publishAgent("process.created", "process", "abc", "n", null, "fp12");
    b.publishHuman("workflow.updated", "workflow", "w1", "w", "w1");
    b.relay(WorkspaceEvent.agent("process.updated", "process", "x", "n", null, "fp", 0));
    assertThat(shared).extracting(WorkspaceEvent::event).containsExactly("process.created", "workflow.updated");
    assertThat(b.currentRevision()).isEqualTo(3);
  }

  @Test
  void runsAreNotShownAsSomeoneEditing() {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.isWorkspaceEventsEnabled()).thenReturn(true);
    AgentPresenceTracker presence = new AgentPresenceTracker();
    WorkspaceEventBroadcaster b =
        new WorkspaceEventBroadcaster(props, presence, new WorkspaceLongPollHub(), new ObjectMapper());
    b.relay(WorkspaceEvent.run("run.started", "h1", "w1", "flow", "w1", "Running"));
    assertThat(presence.snapshotLive()).isEmpty();
    assertThat(b.currentRevision()).isEqualTo(1);
  }

  @Test
  void aPersonsEditIsShownAsAPerson() {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.isWorkspaceEventsEnabled()).thenReturn(true);
    AgentPresenceTracker presence = new AgentPresenceTracker();
    WorkspaceEventBroadcaster b =
        new WorkspaceEventBroadcaster(props, presence, new WorkspaceLongPollHub(), new ObjectMapper());
    b.publishHuman("workflow.updated", "workflow", "w1", "flow", "w1");
    assertThat(presence.getIfLive("workflow", "w1").actor()).isEqualTo("human");
    b.publishAgent("process.updated", "process", "p1", "n", null, "fp12");
    assertThat(presence.getIfLive("process", "p1").actor()).isEqualTo("agent");
    assertThat(presence.getIfLive("process", "p1").tokenFingerprint()).isEqualTo("fp12");
  }
}
