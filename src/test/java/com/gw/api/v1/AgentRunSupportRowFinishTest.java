package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gw.api.v1.service.AgentRunSupport;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.ProcessStatusCache;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The cache can say Done before LocalSessionOutput saves the log; runs must not look done early. */
class AgentRunSupportRowFinishTest {

  private HistoryRepository historyRepository;
  private HistoryTool historyTool;
  private WorkflowRepository workflowRepository;
  private AgentRunSupport support;
  private History row;

  @BeforeEach
  void setUp() {
    historyRepository = mock(HistoryRepository.class);
    historyTool = mock(HistoryTool.class);
    workflowRepository = mock(WorkflowRepository.class);
    support =
        new AgentRunSupport(
            mock(AgentApiProperties.class),
            historyRepository,
            historyTool,
            mock(ProcessTool.class),
            mock(ProcessStatusCache.class),
            workflowRepository,
            mock(EntityManager.class),
            mock(HostRepository.class),
            mock(EnvironmentRepository.class));
    row = new History();
    row.setHistory_id("h1");
    row.setHistory_process("p1");
    when(historyRepository.findById("h1")).thenReturn(Optional.of(row));
  }

  /** getHistoryById overlays the cached status onto the same entity, like the real HistoryTool. */
  private void cacheSays(String status) {
    when(historyTool.getHistoryById("h1"))
        .thenAnswer(
            inv -> {
              row.setIndicator(status);
              return row;
            });
  }

  @Test
  @DisplayName("cache Done while the row is still Running and has no log -> report Running")
  void cacheAheadOfRowIsNotTerminal() {
    row.setIndicator(ExecutionStatus.RUNNING);
    cacheSays(ExecutionStatus.DONE);

    assertThat(support.getRun("h1", null).terminal()).isFalse();
  }

  @Test
  @DisplayName("row saved as Done with its log -> report Done")
  void rowFinishedIsTerminal() {
    row.setIndicator(ExecutionStatus.DONE);
    row.setHistory_output("agent-py-v2");
    cacheSays(ExecutionStatus.DONE);

    assertThat(support.getRun("h1", null).status()).isEqualTo("Done");
    assertThat(support.getRun("h1", null).log()).isEqualTo("agent-py-v2");
  }

  @Test
  @DisplayName("workflow parents keep the cached status (their row is aggregated separately)")
  void workflowRunUsesCache() {
    when(workflowRepository.existsById("p1")).thenReturn(true);
    row.setIndicator(ExecutionStatus.RUNNING);
    cacheSays(ExecutionStatus.DONE);

    assertThat(support.getRun("h1", null).terminal()).isTrue();
  }

  @Test
  @DisplayName("workflow marked Done while a step's row says Failed -> report Failed")
  void workflowWithFailedStepIsFailed() {
    when(workflowRepository.existsById("p1")).thenReturn(true);
    row.setHistory_input("bad-f1;");
    row.setHistory_output("c1;");
    row.setIndicator(ExecutionStatus.DONE);
    cacheSays(ExecutionStatus.DONE);
    History step = new History();
    step.setHistory_id("c1");
    step.setIndicator(ExecutionStatus.FAILED);
    when(historyRepository.findById("c1")).thenReturn(Optional.of(step));

    assertThat(support.getRun("h1", null).status()).isEqualTo("Failed");
    assertThat(support.getRun("h1", null).terminal()).isTrue();
  }
}
