package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RunLogStoreTest {

  @TempDir Path dir;

  @BeforeEach
  void useTempDir() {
    System.setProperty("geoweaver.run.log.dir", dir.toString());
    System.setProperty("geoweaver.run.log.db.limit.kb", "1");
  }

  @AfterEach
  void reset() {
    System.clearProperty("geoweaver.run.log.dir");
    System.clearProperty("geoweaver.run.log.db.limit.kb");
  }

  @Test
  void smallLogStaysInTheDatabaseOnly() {
    RunLogStore.RunLog log = RunLogStore.open("smallRun01");
    log.appendLine("hello").appendLine("world");
    log.close();
    assertThat(log.isSpilled()).isFalse();
    assertThat(log.databaseText()).isEqualTo("hello\nworld\n");
    assertThat(dir.toFile().list()).isEmpty();
  }

  @Test
  void largeLogGoesToACompressedFileAndTheDatabaseKeepsTheTail() {
    RunLogStore.RunLog log = RunLogStore.open("bigRun0001");
    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < 5000; i++) {
      String line = "line " + i + " of output";
      log.appendLine(line);
      expected.append(line).append('\n');
    }
    String running = log.databaseText();
    assertThat(RunLogStore.activeFile("bigRun0001")).isFile();
    assertThat(RunLogStore.readFull("bigRun0001", running)).isEqualTo(expected.toString());

    log.close();
    String db = log.databaseText();
    assertThat(log.isSpilled()).isTrue();
    assertThat(RunLogStore.isTruncatedCopy(db)).isTrue();
    assertThat(db.length()).isLessThan(1024 + 300);
    assertThat(db).endsWith("line 4999 of output\n");
    assertThat(RunLogStore.activeFile("bigRun0001")).doesNotExist();
    assertThat(RunLogStore.finishedFile("bigRun0001")).isFile();

    assertThat(RunLogStore.readFull("bigRun0001", db)).isEqualTo(expected.toString());
    assertThat(RunLogStore.readTail("bigRun0001", db, 20)).isEqualTo(expected.substring(expected.length() - 20));

    RunLogStore.delete("bigRun0001");
    assertThat(RunLogStore.finishedFile("bigRun0001")).doesNotExist();
    // file gone: readers fall back to the database copy
    assertThat(RunLogStore.readFull("bigRun0001", db)).isEqualTo(db);
  }

  @Test
  void memoryStaysBoundedForHugeLogs() {
    RunLogStore.RunLog log = RunLogStore.open("hugeRun001");
    String line = "x".repeat(200);
    for (int i = 0; i < 50_000; i++) log.appendLine(line); // ~10 MB
    assertThat(log.totalChars()).isEqualTo(50_000L * 201);
    assertThat(log.databaseText().length()).isLessThan(2 * 1024 + 300);
    log.close();
    assertThat(RunLogStore.finishedFile("hugeRun001").length()).isLessThan(1024 * 1024);
  }

  @Test
  void unsafeIdsNeverBecomePaths() {
    RunLogStore.RunLog log = RunLogStore.open("../../etc/x");
    for (int i = 0; i < 500; i++) log.appendLine("line " + i);
    log.close();
    assertThat(log.isSpilled()).isFalse();
    assertThat(log.databaseText()).contains("line 499");
    assertThat(new File(dir.toFile(), "..").list()).doesNotContain("x.log");
    RunLogStore.delete("../../etc/passwd"); // no-op
  }

  @Test
  void workflowChildIdListsAreReturnedUnchanged() {
    String ids = "abc;def;ghi;";
    // not a truncated copy, so the text is the database value itself
    assertThat(RunLogStore.readFull("wf000000001", ids)).isEqualTo(ids);
  }
}
