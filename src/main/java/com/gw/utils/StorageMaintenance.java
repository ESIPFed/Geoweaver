package com.gw.utils;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Keeps what the running server stores on disk bounded, so a server can run for years without a
 * restart on a home folder with a quota. One background thread, every 30 minutes by default:
 *
 * <ul>
 *   <li>compacts the embedded H2 file once it has doubled since the last compaction, or when the
 *       disk is low; the connection pool is paused for the few seconds this takes, and only when no
 *       connection is in use;
 *   <li>keeps the run-log folder under its limit ({@link RunLogStore#enforceLimit});
 *   <li>removes entries in the workspace temp folder (exports, uploads, unpacked imports) older
 *       than a week;
 *   <li>cleans up run folders whose end was never seen;
 *   <li>warns in the log when free disk space is low.
 * </ul>
 *
 * <p>Settings (environment variables or Spring properties): {@code GEOWEAVER_STORAGE_MAINTENANCE}
 * ({@code false} turns all of this off), {@code GEOWEAVER_STORAGE_CHECK_MINUTES} (30),
 * {@code GEOWEAVER_DB_COMPACT_MIN_MB} (64), {@code GEOWEAVER_DB_ONLINE_COMPACT_MAX_MB} (12288,
 * larger files are left to start-up or {@code db-compact}), {@code GEOWEAVER_TEMP_RETENTION_DAYS}
 * (7, 0 = keep), plus the run-log settings of {@link RunLogStore}.
 */
@Component
public class StorageMaintenance implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger log = Logger.getLogger(StorageMaintenance.class);

  /** Longest wait for running database work to finish before compacting. */
  static final long IDLE_WAIT_MS = 20_000;

  /** Compact on low disk only when the file has grown this much since the last compaction. */
  static final double LOW_DISK_GROWTH = 1.25;

  private final ObjectProvider<DataSource> dataSource;
  private final Environment env;

  @Value("${geoweaver.workspace:~/gw-workspace}")
  private String workspace;

  @Value("${geoweaver.upload_file_path:temp}")
  private String tempFolderName;

  private ScheduledExecutorService executor;

  /** How long to wait for running database work; shorter in tests. */
  long idleWaitMs = IDLE_WAIT_MS;

  public StorageMaintenance(ObjectProvider<DataSource> dataSource, Environment env) {
    this.dataSource = dataSource;
    this.env = env;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (!(event.getApplicationContext() instanceof WebServerApplicationContext)) return; // CLI
    if (!env.getProperty("GEOWEAVER_STORAGE_MAINTENANCE", Boolean.class, true)) return;
    synchronized (this) {
      if (executor != null) return;
      long minutes = Math.max(1, env.getProperty("GEOWEAVER_STORAGE_CHECK_MINUTES", Long.class, 30L));
      executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "geoweaver-storage-maintenance");
        t.setDaemon(true);
        return t;
      });
      executor.scheduleWithFixedDelay(this::runOnce, Math.min(minutes, 5), minutes, TimeUnit.MINUTES);
    }
  }

  /** One maintenance pass. Never throws. */
  public void runOnce() {
    try {
      RunLogStore.Pruned pruned = RunLogStore.enforceLimit(RunLogStore.maxTotalBytes());
      if (pruned.files() > 0) {
        log.info("Run logs over their limit: removed the " + pruned.files() + " oldest log files ("
            + H2StorageMaintenance.human(pruned.bytes()) + "); the database keeps the end of each.");
      }
      int days = env.getProperty("GEOWEAVER_TEMP_RETENTION_DAYS", Integer.class, 7);
      File temp = tempFolder();
      if (days > 0 && temp != null) {
        long removed = pruneOlderThan(temp, days * 24L * 3600 * 1000);
        if (removed > 0) log.info("Removed " + removed + " temporary files and folders older than " + days + " days.");
      }
      RunWorkspaceFiles.cleanupStale(7L * 24 * 3600 * 1000);
      compactDatabaseIfNeeded();
      warnIfDiskLow();
    } catch (Throwable t) {
      log.warn("Storage maintenance pass failed: " + t);
    }
  }

  /** The workspace temp folder, or null when no workspace is configured (then nothing is pruned). */
  File tempFolder() {
    if (workspace == null || workspace.isBlank() || tempFolderName == null || tempFolderName.isBlank()) return null;
    return new File(expandHome(workspace), tempFolderName);
  }

  static String expandHome(String path) {
    if (path == null) return System.getProperty("user.home");
    if (path.equals("~") || path.startsWith("~/") || path.startsWith("~\\")) {
      return System.getProperty("user.home") + path.substring(1);
    }
    return path;
  }

  /**
   * Deletes the top-level entries of {@code dir} last modified more than {@code maxAgeMillis} ago
   * (folders with everything in them; links are removed, never followed). Returns how many
   * entries were removed.
   */
  static long pruneOlderThan(File dir, long maxAgeMillis) {
    File[] entries = dir.listFiles();
    if (entries == null) return 0;
    long cutoff = System.currentTimeMillis() - maxAgeMillis;
    long removed = 0;
    for (File e : entries) {
      try {
        BasicFileAttributes a = Files.readAttributes(e.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (a.lastModifiedTime().toMillis() >= cutoff) continue;
        if (a.isDirectory() && newestModified(e.toPath()) >= cutoff) continue; // still being used
        deleteTree(e.toPath());
        removed++;
      } catch (IOException ignored) {
        // in use or not ours to delete: try again next time
      }
    }
    return removed;
  }

  private static long newestModified(Path dir) throws IOException {
    long[] newest = {0};
    Files.walkFileTree(dir, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
        newest[0] = Math.max(newest[0], a.lastModifiedTime().toMillis());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
        newest[0] = Math.max(newest[0], a.lastModifiedTime().toMillis());
        return FileVisitResult.CONTINUE;
      }
    });
    return newest[0];
  }

  private static void deleteTree(Path root) throws IOException {
    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
        Files.deleteIfExists(f);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
        Files.deleteIfExists(d);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /** Compacts the H2 file while the server runs, when it is due. */
  H2StorageMaintenance.Result compactDatabaseIfNeeded() {
    DataSource ds = dataSource.getIfAvailable();
    HikariDataSource hikari = unwrap(ds);
    if (hikari == null) return null;
    String url = hikari.getJdbcUrl();
    File file = H2StorageMaintenance.databaseFile(url);
    if (file == null || !file.isFile()) return null;

    long size = file.length();
    long minBytes = env.getProperty("GEOWEAVER_DB_COMPACT_MIN_MB", Long.class, H2StartupCompaction.DEFAULT_MIN_MB) * 1024 * 1024;
    long maxBytes = env.getProperty("GEOWEAVER_DB_ONLINE_COMPACT_MAX_MB", Long.class, 12288L) * 1024 * 1024;
    long last = H2StorageMaintenance.lastCompactedSize(file);
    boolean grown = size >= minBytes && (last <= 0 || size >= last * H2StorageMaintenance.GROWTH_FACTOR);
    boolean lowDisk = file.getAbsoluteFile().getParentFile().getUsableSpace() < RunLogStore.minFreeBytes()
        && size >= minBytes && (last <= 0 || size >= last * LOW_DISK_GROWTH);
    if (!grown && !lowDisk) return null;
    if (size > maxBytes) {
      log.warn("Database file " + file + " is " + H2StorageMaintenance.human(size)
          + "; too large to compact while running. Restart Geoweaver or run 'db-compact' to shrink it.");
      return null;
    }
    if (!hikari.isAllowPoolSuspension()) {
      log.warn("Database file " + file + " is due for compaction, but pool suspension is off "
          + "(spring.datasource.hikari.allow-pool-suspension); it will be compacted at the next start.");
      return null;
    }
    HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
    if (pool == null) return null;

    // While suspended, the pool makes new requests wait (without the usual connection timeout)
    // until it is resumed; compaction runs at roughly 100 MB of live data per second.
    pool.suspendPool();
    try {
      long deadline = System.currentTimeMillis() + idleWaitMs;
      while (pool.getActiveConnections() > 0) {
        if (System.currentTimeMillis() > deadline) {
          log.info("Database busy; compaction postponed to the next check.");
          return null;
        }
        Thread.sleep(100);
      }
      H2StorageMaintenance.Options opts = lowDisk && !grown
          ? new H2StorageMaintenance.Options(true, 0, false, true)
          : H2StorageMaintenance.Options.automatic(minBytes).inRunningServer();
      H2StorageMaintenance.Result r = H2StorageMaintenance.compact(url, hikari.getUsername(), hikari.getPassword(), opts);
      log.info(r.describe());
      return r;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      pool.softEvictConnections(); // their database session ended with the compaction
      pool.resumePool();
    }
  }

  private static HikariDataSource unwrap(DataSource ds) {
    if (ds == null) return null;
    if (ds instanceof HikariDataSource h) return h;
    try {
      return ds.isWrapperFor(HikariDataSource.class) ? ds.unwrap(HikariDataSource.class) : null;
    } catch (Exception e) {
      return null;
    }
  }

  private void warnIfDiskLow() {
    File home = new File(System.getProperty("user.home"));
    long free = home.getUsableSpace();
    if (free < RunLogStore.minFreeBytes()) {
      log.warn("Only " + H2StorageMaintenance.human(free) + " of disk space is free in " + home
          + ". Geoweaver keeps running, but long logs are now kept only in part. Free some space.");
    }
  }
}
