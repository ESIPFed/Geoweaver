package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.jpa.ExecutionStatus;
import org.junit.jupiter.api.Test;

class AgentExecutionStatusesTest {

  @Test
  void normalizeMapsAllKnownStatuses() {
    assertThat(AgentExecutionStatuses.normalize("Done")).isEqualTo(ExecutionStatus.DONE);
    assertThat(AgentExecutionStatuses.normalize("Failed")).isEqualTo(ExecutionStatus.FAILED);
    assertThat(AgentExecutionStatuses.normalize("Running")).isEqualTo(ExecutionStatus.RUNNING);
    assertThat(AgentExecutionStatuses.normalize("Unknown")).isEqualTo(ExecutionStatus.UNKOWN);
    assertThat(AgentExecutionStatuses.normalize("Stopped")).isEqualTo(ExecutionStatus.STOPPED);
    assertThat(AgentExecutionStatuses.normalize("Ready")).isEqualTo(ExecutionStatus.READY);
    assertThat(AgentExecutionStatuses.normalize("Skipped")).isEqualTo(ExecutionStatus.SKIPPED);
    assertThat(AgentExecutionStatuses.normalize(null)).isEqualTo(ExecutionStatus.UNKOWN);
    assertThat(AgentExecutionStatuses.normalize("weird")).isEqualTo(ExecutionStatus.UNKOWN);
  }

  @Test
  void terminalSetMatchesPlan() {
    assertThat(AgentExecutionStatuses.isTerminal("Done")).isTrue();
    assertThat(AgentExecutionStatuses.isTerminal("Failed")).isTrue();
    assertThat(AgentExecutionStatuses.isTerminal("Stopped")).isTrue();
    assertThat(AgentExecutionStatuses.isTerminal("Skipped")).isTrue();
    assertThat(AgentExecutionStatuses.isTerminal("Running")).isFalse();
    assertThat(AgentExecutionStatuses.isTerminal("Ready")).isFalse();
    assertThat(AgentExecutionStatuses.isTerminal("Unknown")).isFalse();
  }
}
