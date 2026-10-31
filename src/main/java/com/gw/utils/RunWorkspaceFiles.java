package com.gw.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Removes the files Geoweaver itself writes into a run's working folder (the script it runs and
 * the copies of the other Python processes it places next to it) once the run has ended.
 *
 * <p>Without this every local run leaves a folder with a copy of every Python process behind, and
 * the workspace grows with the number of runs. Only files recorded here are removed, and only when
 * their size and modification time are unchanged, so anything the process itself wrote or edited
 * stays. The folder is removed only when it is then empty.
 *
 * <p>{@code GEOWEAVER_KEEP_RUN_FILES=true} (or {@code geoweaver.keep.run.files}) keeps everything.
 */
public final class RunWorkspaceFiles {

  private record Written(Path path, long size, long modified) {}

  private static final Map<String, List<Written>> WRITTEN = new ConcurrentHashMap<>();
  private static final Map<String, Path> FOLDERS = new ConcurrentHashMap<>();
  private static final Map<String, Long> RECORDED_AT = new ConcurrentHashMap<>();

  private RunWorkspaceFiles() {}

  /** Records a file Geoweaver has just written for a run (call after the file is complete). */
  public static void recordWritten(String historyId, File file) {
    if (historyId == null || file == null) return;
    try {
      Path p = file.toPath().toAbsolutePath().normalize();
      BasicFileAttributes a = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (!a.isRegularFile()) return;
      RECORDED_AT.put(historyId, System.currentTimeMillis());
      WRITTEN.computeIfAbsent(historyId, k -> new CopyOnWriteArrayList<>())
          .add(new Written(p, a.size(), a.lastModifiedTime().toMillis()));
      Path parent = p.getParent();
      if (parent != null && parent.getFileName() != null && parent.getFileName().toString().equals(historyId)) {
        FOLDERS.putIfAbsent(historyId, parent);
      }
    } catch (IOException ignored) {
      // not recorded means not removed
    }
  }

  /** Number of runs with recorded files not yet cleaned up (for tests and diagnostics). */
  static int pendingRuns() {
    return WRITTEN.size();
  }

  /** Removes the recorded, unchanged files of a finished run, then its folder when empty. */
  public static void cleanup(String historyId) {
    if (historyId == null) return;
    RECORDED_AT.remove(historyId);
    List<Written> files = WRITTEN.remove(historyId);
    Path folder = FOLDERS.remove(historyId);
    if (files == null || keepFiles()) return;
    for (Written w : files) {
      try {
        BasicFileAttributes a = Files.readAttributes(w.path(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (a.isRegularFile() && a.size() == w.size() && a.lastModifiedTime().toMillis() == w.modified()) {
          Files.deleteIfExists(w.path());
        }
      } catch (IOException ignored) {
        // already gone or not readable: leave it
      }
    }
    if (folder != null) {
      try {
        Files.deleteIfExists(folder); // fails, as intended, when the run left files of its own
      } catch (IOException ignored) {
        // not empty
      }
    }
  }

  /**
   * Cleans up runs recorded longer ago than {@code maxAgeMillis} whose end was never seen (for
   * example a run that failed before its output was read). Returns how many runs were cleaned.
   */
  public static int cleanupStale(long maxAgeMillis) {
    long cutoff = System.currentTimeMillis() - maxAgeMillis;
    int n = 0;
    for (Map.Entry<String, Long> e : RECORDED_AT.entrySet()) {
      if (e.getValue() < cutoff) {
        cleanup(e.getKey());
        n++;
      }
    }
    return n;
  }

  private static boolean keepFiles() {
    String v = System.getProperty("geoweaver.keep.run.files");
    if (v == null || v.isBlank()) v = System.getenv("GEOWEAVER_KEEP_RUN_FILES");
    return v != null && v.trim().equalsIgnoreCase("true");
  }
}
