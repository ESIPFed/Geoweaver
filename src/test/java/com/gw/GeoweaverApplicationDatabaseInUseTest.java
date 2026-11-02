package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeoweaverApplicationDatabaseInUseTest {

  @Test
  void explainsWhenAnotherGeoweaverHoldsTheDatabase() {
    RuntimeException locked =
        new RuntimeException("wrapper", new IllegalStateException("Database may be already in use: \"Locked by another process\""));
    assertThat(GeoweaverApplication.databaseInUse(locked)).contains("another Geoweaver process holds it");

    RuntimeException unreachable =
        new RuntimeException(
            "x", new RuntimeException("Connection is broken: \"java.net.SocketTimeoutException: Connect timed out: 192.0.2.1:9092\""));
    assertThat(GeoweaverApplication.databaseInUse(unreachable)).contains("docs/ssh-and-gui.md");

    assertThat(GeoweaverApplication.databaseInUse(new RuntimeException("Table not found"))).isNull();
  }
}
