package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.api.v1.service.AgentRunIdempotencyStore;
import org.junit.jupiter.api.Test;

class AgentRunIdempotencyStoreTest {

  @Test
  void sameKeyReturnsSameHistory() {
    AgentRunIdempotencyStore store = new AgentRunIdempotencyStore();
    assertThat(store.lookup("fp1", null)).isNull();
    store.put("fp1", "job-a", "hist1");
    assertThat(store.lookup("fp1", "job-a")).isEqualTo("hist1");
    assertThat(store.lookup("fp2", "job-a")).isNull();
  }
}
