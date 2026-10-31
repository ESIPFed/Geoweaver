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
    System.clearProperty("geoweaver.min.free.mb");
  }

  @Test
  void lowDiskKeepsOnlyTheEndOfTheLogInTheDatabaseAndNoFile() {
    System.setProperty("geoweaver.min.free.mb", Long.toString(Long.MAX_VALUE / (2L << 20)));
    RunLogStore.RunLog log = RunLogStore.open("lowDisk001");
    for (int i = 0; i < 20000; i++) log.appendLine("line " + i + " of output");
    log.close();
    String db = log.databaseText();
    assertThat(log.isSpilled()).isFalse();
    assertThat(RunLogStore.activeFile("lowDisk001")).doesNotExist();
    assertThat(RunLogStore.finishedFile("lowDisk001")).doesNotExist();
    assertThat(db).contains("the full log was not saved (low disk space");
    assertThat(db.length()).isLessThan(1024 + 300);
    assertThat(db).endsWith("line 19999 of output\n");
    assertThat(RunLogStore.readTail("lowDisk001", db, 100)).endsWith("line 19999 of output\n");
  }

  @Test
  void limitRemovesTheOldestFinishedLogsButNeverRunningOnes() throws Exception {
    File sub = new File(dir.toFile(), "ab");
    sub.mkdirs();
    long now = System.currentTimeMillis();
    for (int i = 0; i < 5; i++) {
      File f = new File(sub, "abRun" + i + ".log.gz");
      java.nio.file.Files.write(f.toPath(), new byte[1000]);
      f.setLastModified(now - (10 - i) * 60_000L); // abRun0 oldest
    }
    File running = new File(sub, "abRunning.log");
    java.nio.file.Files.write(running.toPath(), new byte[3000]);
    running.setLastModified(now - 3_600_000L); // oldest of all, but still running

    RunLogStore.Pruned p = RunLogStore.enforceLimit(5000);
    assertThat(p.files()).isEqualTo(3);
    assertThat(p.remainingBytes()).isEqualTo(5000);
    assertThat(running).isFile();
    assertThat(new File(sub, "abRun0.log.gz")).doesNotExist();
    assertThat(new File(sub, "abRun2.log.gz")).doesNotExist();
    assertThat(new File(sub, "abRun3.log.gz")).isFile();
    assertThat(RunLogStore.enforceLimit(0).files()).isZero();
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
