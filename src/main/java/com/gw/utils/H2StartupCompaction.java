package com.gw.utils;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Compacts the embedded H2 database before the server opens it, when the file has grown well
 * beyond its live data. Runs once at start-up, before the connection pool exists, so the database
 * is not in use. Any problem is logged and start-up continues normally.
 *
 * <p>Settings (environment variables or Spring properties):
 *
 * <ul>
 *   <li>{@code GEOWEAVER_DB_AUTO_COMPACT} – {@code false} turns this off (default {@code true}).
 *   <li>{@code GEOWEAVER_DB_COMPACT_MIN_MB} – smallest file worth checking (default 512).
 * </ul>
 */
public class H2StartupCompaction implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  static final long DEFAULT_MIN_MB = 512;

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    try {
      run(event.getEnvironment());
    } catch (Throwable t) {
      System.out.println("Database compaction skipped: " + t);
    }
  }

  void run(ConfigurableEnvironment env) {
    if (!env.getProperty("GEOWEAVER_DB_AUTO_COMPACT", Boolean.class, true)) return;
    String url = env.getProperty("spring.datasource.url");
    if (H2StorageMaintenance.databaseFile(url) == null) return;

    long minMb = env.getProperty("GEOWEAVER_DB_COMPACT_MIN_MB", Long.class, DEFAULT_MIN_MB);
    java.io.File file = H2StorageMaintenance.databaseFile(url);
    if (!file.isFile() || file.length() < minMb * 1024 * 1024) return;

    System.out.println(
        "Checking database file " + file + " (" + H2StorageMaintenance.human(file.length())
            + "); compacting a large file can take a few minutes...");
    H2StorageMaintenance.Result result =
        H2StorageMaintenance.compact(
            url,
            env.getProperty("spring.datasource.username", ""),
            env.getProperty("spring.datasource.password", ""),
            H2StorageMaintenance.Options.automatic(minMb * 1024 * 1024));
    System.out.println(result.describe());
  }
}
