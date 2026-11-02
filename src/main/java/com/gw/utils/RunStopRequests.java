package com.gw.utils;

import com.gw.jpa.ExecutionStatus;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * Lets one Geoweaver stop a run that another Geoweaver on the same database is executing, for
 * example Stop pressed in the browser for a workflow started with {@code geoweaver run workflow}
 * in an SSH session. The process that runs a step is the only one that can end it, so a Stop for
 * a run this process does not execute leaves a small request file in {@code
 * ~/geoweaver/run-control/}; every Geoweaver process checks that folder once a second and stops
 * the runs it owns. A shared home folder makes this work across machines too (for example a
 * login node and an Open OnDemand session).
 *
 * <p>Folder: {@code GEOWEAVER_RUN_CONTROL_DIR} / {@code geoweaver.run.control.dir}. Requests that
 * nobody picks up within a day (the run ended or its process was killed) are removed.
 */
public final class RunStopRequests {

  private static final Logger log = Logger.getLogger(RunStopRequests.class);

  private static final Pattern ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
  private static final String SUFFIX = ".stop";
  static final long MAX_AGE_MS = 24L * 3600 * 1000;
  private static final int MAX_OWNED = 10_000;

  /** Workflow runs started by this process (process runs are known from the session maps). */
  private static final Set<String> OWNED_WORKFLOWS =
      Collections.synchronizedSet(
          Collections.newSetFromMap(
              new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> e) {
                  return size() > MAX_OWNED;
                }
              }));

  /** Runs this process is stopping on purpose, so their end is recorded as Stopped, not Failed. */
  private static final Set<String> STOPPING =
      Collections.synchronizedSet(
          Collections.newSetFromMap(
              new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> e) {
                  return size() > MAX_OWNED;
                }
              }));

  private static Thread watcher;

  private RunStopRequests() {}

  public static File directory() {
    String dir = System.getProperty("geoweaver.run.control.dir");
    if (dir == null || dir.isBlank()) dir = System.getenv("GEOWEAVER_RUN_CONTROL_DIR");
    if (dir == null || dir.isBlank()) {
      dir = System.getProperty("user.home") + File.separator + "geoweaver" + File.separator + "run-control";
    }
    return new File(dir);
  }

  static boolean validId(String id) {
    return id != null && ID.matcher(id).matches();
  }

  /** Asks whichever Geoweaver process runs {@code historyId} to stop it. */
  public static boolean request(String historyId) {
    if (!validId(historyId)) return false;
    File dir = directory();
    try {
      if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory()) return false;
      File f = new File(dir, historyId + SUFFIX);
      if (!f.createNewFile()) f.setLastModified(System.currentTimeMillis());
      return true;
    } catch (IOException | SecurityException e) {
      log.warn("Could not leave a stop request for run " + historyId + ": " + e);
      return false;
    }
  }

  /** True for a status that means the run has ended (or never started). */
  public static boolean finished(String indicator) {
    return indicator == null
        || ExecutionStatus.DONE.equals(indicator)
        || ExecutionStatus.FAILED.equals(indicator)
        || ExecutionStatus.STOPPED.equals(indicator)
        || ExecutionStatus.SKIPPED.equals(indicator)
        || ExecutionStatus.UNKOWN.equals(indicator);
  }

  static boolean pending(String historyId) {
    return validId(historyId) && new File(directory(), historyId + SUFFIX).isFile();
  }

  /** Call before ending a run's process on purpose. */
  public static void markStopping(String historyId) {
    if (historyId != null) STOPPING.add(historyId);
  }

  /** The status to record for a run that ended with a non-zero exit code. */
  public static String endStatus(String historyId, int exitCode) {
    if (exitCode == 0) return ExecutionStatus.DONE;
    return historyId != null && STOPPING.contains(historyId) ? ExecutionStatus.STOPPED : ExecutionStatus.FAILED;
  }

  public static void ownWorkflow(String historyId) {
    if (validId(historyId)) OWNED_WORKFLOWS.add(historyId);
  }

  public static boolean ownsWorkflow(String historyId) {
    return OWNED_WORKFLOWS.contains(historyId);
  }

  public static void releaseWorkflow(String historyId) {
    OWNED_WORKFLOWS.remove(historyId);
  }

  /**
   * One pass over the request folder: for each request whose run {@code owns} accepts, calls
   * {@code stop} and removes the request; requests older than a day are removed. Returns how many
   * runs were stopped.
   */
  static int poll(Predicate<String> owns, Predicate<String> stop) {
    File[] files = directory().listFiles((d, n) -> n.endsWith(SUFFIX));
    if (files == null || files.length == 0) return 0;
    int stopped = 0;
    long now = System.currentTimeMillis();
    for (File f : files) {
      String id = f.getName().substring(0, f.getName().length() - SUFFIX.length());
      if (!validId(id)) {
        f.delete();
        continue;
      }
      if (owns.test(id)) {
        try {
          if (stop.test(id)) stopped++;
        } catch (RuntimeException e) {
          log.warn("Stopping run " + id + " on request failed: " + e);
        }
        f.delete();
      } else if (now - f.lastModified() > MAX_AGE_MS) {
        f.delete();
      }
    }
    return stopped;
  }

  /** Starts the once-a-second check in this process (idempotent). */
  public static synchronized void startWatching(Predicate<String> owns, Predicate<String> stop) {
    if (watcher != null) return;
    watcher =
        new Thread(
            () -> {
              while (!Thread.currentThread().isInterrupted()) {
                try {
                  int n = poll(owns, stop);
                  if (n > 0) log.info("Stopped " + n + " run(s) on request from another Geoweaver.");
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  return;
                } catch (Throwable t) {
                  log.debug("Run stop check failed: " + t);
                }
              }
            },
            "geoweaver-run-stop-watcher");
    watcher.setDaemon(true);
    watcher.start();
  }
}
