package com.gw.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionLogBrokerTest {

  private ExecutionLogBroker broker;

  @BeforeEach
  void setUp() {
    broker = new ExecutionLogBroker();
  }

  @Test
  void subscribeAndGetSubscribers() {
    broker.subscribe("hist1", "token-a");
    broker.subscribe("hist1", "token-b");
    broker.subscribe("hist2", "token-c");

    Set<String> subs = broker.getSubscribers("hist1");
    assertEquals(2, subs.size());
    assertTrue(subs.contains("token-a"));
    assertTrue(subs.contains("token-b"));
    assertEquals(1, broker.getSubscribers("hist2").size());
  }

  @Test
  void unsubscribeRemovesToken() {
    broker.subscribe("hist1", "token-a");
    broker.subscribe("hist1", "token-b");
    broker.unsubscribe("hist1", "token-a");

    Set<String> subs = broker.getSubscribers("hist1");
    assertEquals(1, subs.size());
    assertTrue(subs.contains("token-b"));
  }

  @Test
  void extractHistoryIdFromLogLine() {
    assertEquals("abc123", ExecutionLogBroker.extractHistoryId("abc123*_*hello"));
    assertNull(ExecutionLogBroker.extractHistoryId("no-separator"));
    assertNull(ExecutionLogBroker.extractHistoryId(null));
  }
}
