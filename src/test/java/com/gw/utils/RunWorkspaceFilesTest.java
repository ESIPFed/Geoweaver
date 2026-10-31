package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RunWorkspaceFilesTest {

  @TempDir Path workspace;

  private File write(File folder, String name, String text) throws Exception {
    folder.mkdirs();
    File f = new File(folder, name);
    Files.writeString(f.toPath(), text);
    return f;
  }

  @Test
  void removesGeneratedFilesAndTheEmptyFolder() throws Exception {
    File run = new File(workspace.toFile(), "runAAA0001");
    File script = write(run, "gw-token-runAAA0001.sh", "echo hi");
    File copy = write(run, "other_process.py", "print(1)");
    RunWorkspaceFiles.recordWritten("runAAA0001", script);
    RunWorkspaceFiles.recordWritten("runAAA0001", copy);

    RunWorkspaceFiles.cleanup("runAAA0001");

    assertThat(run).doesNotExist();
  }

  @Test
  void keepsWhatTheRunWroteOrChanged() throws Exception {
    File run = new File(workspace.toFile(), "runBBB0001");
    File copy = write(run, "train.py", "print(1)");
    File edited = write(run, "helper.py", "x = 1");
    RunWorkspaceFiles.recordWritten("runBBB0001", copy);
    RunWorkspaceFiles.recordWritten("runBBB0001", edited);
    File output = write(run, "result.csv", "a,b\n1,2\n");
    Files.writeString(edited.toPath(), "x = 2 # changed by the run");

    RunWorkspaceFiles.cleanup("runBBB0001");

    assertThat(copy).doesNotExist();
    assertThat(edited).isFile();
    assertThat(output).isFile();
    assertThat(run).isDirectory();
  }

  @Test
  void keepSettingLeavesEverything() throws Exception {
    File run = new File(workspace.toFile(), "runCCC0001");
    File copy = write(run, "a.py", "print(1)");
    RunWorkspaceFiles.recordWritten("runCCC0001", copy);
    System.setProperty("geoweaver.keep.run.files", "true");
    try {
      RunWorkspaceFiles.cleanup("runCCC0001");
    } finally {
      System.clearProperty("geoweaver.keep.run.files");
    }
    assertThat(copy).isFile();
  }

  @Test
  void staleRunsAreCleanedAndForgotten() throws Exception {
    File run = new File(workspace.toFile(), "runDDD0001");
    File copy = write(run, "a.py", "print(1)");
    RunWorkspaceFiles.recordWritten("runDDD0001", copy);
    int before = RunWorkspaceFiles.pendingRuns();

    assertThat(RunWorkspaceFiles.cleanupStale(60_000)).isZero();
    assertThat(copy).isFile();
    assertThat(RunWorkspaceFiles.cleanupStale(-1)).isGreaterThanOrEqualTo(1);
    assertThat(copy).doesNotExist();
    assertThat(RunWorkspaceFiles.pendingRuns()).isLessThan(before);
  }
}
