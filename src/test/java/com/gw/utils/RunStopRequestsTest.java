package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RunStopRequestsTest {

  @TempDir Path dir;

  @BeforeEach
  void useTempFolder() {
    System.setProperty("geoweaver.run.control.dir", dir.resolve("run-control").toString());
  }

  @AfterEach
  void reset() {
    System.clearProperty("geoweaver.run.control.dir");
  }

  @Test
  void ownerStopsItsRunAndTheRequestIsRemoved() {
    assertThat(RunStopRequests.request("abc123")).isTrue();
    assertThat(RunStopRequests.request("other9")).isTrue();
    assertThat(RunStopRequests.pending("abc123")).isTrue();

    List<String> stopped = new ArrayList<>();
    int n = RunStopRequests.poll(Set.of("abc123")::contains, id -> stopped.add(id));

    assertThat(n).isEqualTo(1);
    assertThat(stopped).containsExactly("abc123");
    assertThat(RunStopRequests.pending("abc123")).isFalse();
    assertThat(RunStopRequests.pending("other9")).isTrue(); // left for the process that runs it
  }

  @Test
  void requestsNobodyPicksUpAreRemovedAfterADay() {
    RunStopRequests.request("orphan1");
    File f = new File(RunStopRequests.directory(), "orphan1.stop");
    f.setLastModified(System.currentTimeMillis() - RunStopRequests.MAX_AGE_MS - 60_000);

    RunStopRequests.poll(id -> false, id -> true);

    assertThat(f).doesNotExist();
  }

  @Test
  void unsafeIdsAreRejected() {
    assertThat(RunStopRequests.request("../etc/passwd")).isFalse();
    assertThat(RunStopRequests.request("a b")).isFalse();
    assertThat(RunStopRequests.request(null)).isFalse();
    assertThat(RunStopRequests.directory().list()).isNullOrEmpty();
  }

  @Test
  void aFailingStopDoesNotEndTheCheck() {
    RunStopRequests.request("bad1");
    RunStopRequests.request("good1");
    List<String> stopped = new ArrayList<>();
    RunStopRequests.poll(
        id -> true,
        id -> {
          if (id.equals("bad1")) throw new IllegalStateException("boom");
          return stopped.add(id);
        });
    assertThat(stopped).containsExactly("good1");
    assertThat(RunStopRequests.directory().list()).isEmpty();
  }

  @Test
  void finishedStatuses() {
    assertThat(RunStopRequests.finished("Running")).isFalse();
    assertThat(RunStopRequests.finished("Ready")).isFalse();
    assertThat(RunStopRequests.finished("Done")).isTrue();
    assertThat(RunStopRequests.finished("Stopped")).isTrue();
    assertThat(RunStopRequests.finished(null)).isTrue();
  }

  @Test
  void workflowOwnership() {
    RunStopRequests.ownWorkflow("wf1");
    assertThat(RunStopRequests.ownsWorkflow("wf1")).isTrue();
    RunStopRequests.releaseWorkflow("wf1");
    assertThat(RunStopRequests.ownsWorkflow("wf1")).isFalse();
  }

  @Test
  void aRunEndedOnPurposeIsStoppedNotFailed() {
    assertThat(RunStopRequests.endStatus("r1", 0)).isEqualTo("Done");
    assertThat(RunStopRequests.endStatus("r1", 143)).isEqualTo("Failed");
    RunStopRequests.markStopping("r1");
    assertThat(RunStopRequests.endStatus("r1", 143)).isEqualTo("Stopped");
    assertThat(RunStopRequests.endStatus("r1", 0)).isEqualTo("Done");
  }
}
