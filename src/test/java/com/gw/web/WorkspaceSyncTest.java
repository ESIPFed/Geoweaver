package com.gw.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.api.v1.workspace.WorkspaceEvent;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.database.WorkspaceFeedRepository;
import com.gw.jpa.History;
import com.gw.jpa.WorkspaceFeedEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;

class WorkspaceSyncTest {

  private WorkspaceFeedRepository feed;
  private HistoryRepository history;
  private WorkflowRepository workflows;
  private ProcessRepository processes;
  private WorkspaceEventBroadcaster broadcaster;
  private final ObjectMapper mapper = new ObjectMapper();
  private WorkspaceSync sync;
  private final List<WorkspaceEvent> relayed = new ArrayList<>();
  private List<Object[]> activeRuns;

  @BeforeEach
  void setUp() {
    feed = mock(WorkspaceFeedRepository.class);
    history = mock(HistoryRepository.class);
    workflows = mock(WorkflowRepository.class);
    processes = mock(ProcessRepository.class);
    broadcaster = mock(WorkspaceEventBroadcaster.class);
    org.mockito.Mockito.doAnswer(i -> relayed.add(i.getArgument(0))).when(broadcaster).relay(any());
    activeRuns = new ArrayList<>();
    when(history.findActiveRunHeads()).thenAnswer(i -> new ArrayList<>(activeRuns));
    when(feed.maxId()).thenReturn(10L);
    when(feed.findTop200ByIdGreaterThanOrderByIdAsc(anyLong())).thenReturn(List.of());
    sync = new WorkspaceSync(feed, history, workflows, processes, broadcaster, mapper, new MockEnvironment(), new com.gw.api.v1.workspace.WorkspaceFeedWriter(feed));
  }

  private WorkspaceFeedEvent row(long id, String origin, WorkspaceEvent e) throws Exception {
    WorkspaceFeedEvent r = new WorkspaceFeedEvent();
    r.setId(id);
    r.setOrigin(origin);
    r.setCreatedAt(new Date());
    r.setPayload(mapper.writeValueAsString(e));
    return r;
  }

  @Test
  void changesFromOtherGeoweaverProcessesReachThisServersBrowsers() throws Exception {
    sync.tick(); // first look: remembers where the feed is
    assertThat(sync.started()).isTrue();
    WorkspaceEvent fromAgent = WorkspaceEvent.agent("workflow.updated", "workflow", "w1", "flow", "w1", "fp", 7);
    WorkspaceEvent mine = WorkspaceEvent.human("process.updated", "process", "p1", "p", null, 3);
    when(feed.findTop200ByIdGreaterThanOrderByIdAsc(10L))
        .thenReturn(List.of(row(11, "other-process", fromAgent), row(12, WorkspaceSync.ORIGIN, mine)));
    when(feed.findTop200ByIdGreaterThanOrderByIdAsc(12L)).thenReturn(List.of());

    sync.tick();

    assertThat(relayed).hasSize(1);
    assertThat(relayed.get(0).event()).isEqualTo("workflow.updated");
    assertThat(relayed.get(0).objectId()).isEqualTo("w1");
    assertThat(relayed.get(0).actor()).isEqualTo("agent");

    relayed.clear();
    sync.tick(); // nothing new: nothing passed on twice
    assertThat(relayed).isEmpty();
  }

  @Test
  void runsStartedAnywhereAreReportedWithTheirWorkflow() {
    sync.tick(); // first look, nothing running
    when(workflows.existsById("wf1")).thenReturn(true);
    History wfRow = new History();
    wfRow.setHistory_id("run1");
    wfRow.setHistory_output("step1;step2;");
    when(history.findById("run1")).thenReturn(Optional.of(wfRow));

    activeRuns.add(new Object[] {"run1", "wf1", "Running"});
    activeRuns.add(new Object[] {"step1", "procA", "Running"});
    sync.tick();
    assertThat(relayed).extracting(WorkspaceEvent::event).containsOnly("run.started");
    assertThat(relayed).extracting(WorkspaceEvent::objectId).containsExactlyInAnyOrder("run1", "step1");
    assertThat(relayed).allMatch(e -> "wf1".equals(e.workflowId()) && "run".equals(e.objectKind()));

    relayed.clear();
    activeRuns.remove(1);
    activeRuns.add(new Object[] {"step2", "procB", "Ready"});
    when(history.findStatusByIds(anyCollection())).thenReturn(List.<Object[]>of(new Object[] {"step1", "Done"}));
    sync.tick();
    assertThat(relayed)
        .anyMatch(e -> e.event().equals("run.finished") && e.objectId().equals("step1") && e.status().equals("Done") && "wf1".equals(e.workflowId()))
        .anyMatch(e -> e.event().equals("run.started") && e.objectId().equals("step2") && "wf1".equals(e.workflowId()));

    relayed.clear();
    activeRuns.set(1, new Object[] {"step2", "procB", "Running"});
    sync.tick();
    assertThat(relayed).singleElement().satisfies(e -> {
      assertThat(e.event()).isEqualTo("run.updated");
      assertThat(e.status()).isEqualTo("Running");
    });
  }

  @Test
  void aProcessRunStartedOnTheCommandLineIsReported() {
    sync.tick();
    activeRuns.add(new Object[] {"h9", "procX", "Running"});
    sync.tick();
    assertThat(relayed).singleElement().satisfies(e -> {
      assertThat(e.event()).isEqualTo("run.started");
      assertThat(e.target()).isEqualTo("procX");
      assertThat(e.objectName()).isEqualTo("procX"); // no such process: the id is shown
      assertThat(e.workflowId()).isNull();
    });
    relayed.clear();
    activeRuns.clear();
    when(history.findStatusByIds(anyCollection())).thenReturn(List.<Object[]>of(new Object[] {"h9", "Stopped"}));
    sync.tick();
    assertThat(relayed).singleElement().satisfies(e -> {
      assertThat(e.event()).isEqualTo("run.finished");
      assertThat(e.status()).isEqualTo("Stopped");
    });
  }

  @Test
  void runsAlreadyGoingWhenTheServerStartsAreNotReportedAsNew() {
    activeRuns.add(new Object[] {"old", "procX", "Running"});
    sync.tick();
    sync.tick();
    assertThat(relayed).isEmpty();
  }

  @Test
  void thisServersChangesAreWrittenToTheSharedFeed() throws Exception {
    sync.share(WorkspaceEvent.agent("process.created", "process", "p1", "name", null, "fp", 5));
    ArgumentCaptor<WorkspaceFeedEvent> saved = ArgumentCaptor.forClass(WorkspaceFeedEvent.class);
    verify(feed).save(saved.capture());
    assertThat(saved.getValue().getOrigin()).isEqualTo(WorkspaceSync.ORIGIN);
    WorkspaceEvent back = mapper.readValue(saved.getValue().getPayload(), WorkspaceEvent.class);
    assertThat(back.event()).isEqualTo("process.created");
    assertThat(back.objectId()).isEqualTo("p1");
  }

  @Test
  void aFailingDatabaseNeverStopsTheChecks() {
    when(feed.maxId()).thenThrow(new RuntimeException("db closed"));
    when(history.findActiveRunHeads()).thenThrow(new RuntimeException("db closed"));
    sync.tick(); // must not throw
    assertThat(relayed).isEmpty();
  }

  @Test
  void runNoticesCarryTheNameOfWhatRuns() {
    com.gw.jpa.GWProcess p = new com.gw.jpa.GWProcess();
    p.setId("procN");
    p.setName("download-data");
    when(processes.findById("procN")).thenReturn(Optional.of(p));
    when(workflows.findById("procN")).thenReturn(Optional.empty());
    sync.tick();
    activeRuns.add(new Object[] {"h5", "procN", "Running"});
    sync.tick();
    assertThat(relayed).singleElement().satisfies(e -> {
      assertThat(e.target()).isEqualTo("procN");
      assertThat(e.objectName()).isEqualTo("download-data");
    });
  }
}
