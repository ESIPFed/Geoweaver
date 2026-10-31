package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;

class StorageMaintenanceTest {

  @TempDir Path dir;

  private HikariDataSource ds;

  @AfterEach
  void close() {
    if (ds != null) ds.close();
  }

  private StorageMaintenance maintenance(DataSource source) {
    MockEnvironment env = new MockEnvironment().withProperty("GEOWEAVER_DB_COMPACT_MIN_MB", "1");
    return new StorageMaintenance(new StaticListableBeanFactory(Map.of("ds", source)).getBeanProvider(DataSource.class), env);
  }

  private HikariDataSource pool(boolean suspendable) {
    HikariDataSource h = new HikariDataSource();
    h.setJdbcUrl("jdbc:h2:file:" + dir + "/gw;AUTO_SERVER=true;INIT=create domain if not exists jsonb as text;COMPRESS=TRUE");
    h.setUsername("geoweaver");
    h.setPassword("test");
    h.setAllowPoolSuspension(suspendable);
    h.setMaximumPoolSize(4);
    return h;
  }

  /** Writes and rewrites large values so the file is far larger than its data. */
  private void bloat(DataSource source) throws Exception {
    try (Connection c = source.getConnection(); Statement s = c.createStatement()) {
      s.execute("create table history(id varchar(20) primary key, output clob)");
      try (PreparedStatement ins = c.prepareStatement("merge into history values(?, ?)")) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
          text.append("epoch ").append(i).append(" loss=").append(Math.random()).append('\n');
          ins.setString(1, "h" + (i % 20));
          ins.setString(2, text.toString());
          ins.executeUpdate();
        }
      }
    }
  }

  private long rows(DataSource source) throws Exception {
    try (Connection c = source.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("select count(*) from history")) {
      r.next();
      return r.getLong(1);
    }
  }

  @Test
  void compactsTheOpenDatabaseAndThePoolKeepsWorking() throws Exception {
    ds = pool(true);
    bloat(ds);
    File file = new File(dir.toFile(), "gw.mv.db");
    long before = file.length();
    assertThat(before).isGreaterThan(1024 * 1024);

    H2StorageMaintenance.Result r = maintenance(ds).compactDatabaseIfNeeded();

    assertThat(r).isNotNull();
    assertThat(r.outcome()).isEqualTo(H2StorageMaintenance.Outcome.COMPACTED);
    assertThat(file.length()).isLessThan(before);
    assertThat(rows(ds)).isEqualTo(20);
    try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
      s.executeUpdate("insert into history values('after', 'still writable')");
    }
    assertThat(rows(ds)).isEqualTo(21);
    // not due again until the file doubles
    assertThat(maintenance(ds).compactDatabaseIfNeeded()).isNull();
  }

  @Test
  void requestsDuringCompactionWaitInsteadOfFailing() throws Exception {
    ds = pool(true);
    ds.setConnectionTimeout(250); // far shorter than the compaction below: waiting must not time out
    bloat(ds);
    // enough poorly compressible live data that compacting takes well over 250 ms
    try (Connection c = ds.getConnection(); PreparedStatement p = c.prepareStatement("insert into history values(?, ?)")) {
      java.util.Random rnd = new java.util.Random(1);
      for (int i = 0; i < 1500; i++) {
        StringBuilder b = new StringBuilder();
        while (b.length() < 40_000) b.append(Long.toHexString(rnd.nextLong()));
        p.setString(1, "big" + i);
        p.setString(2, b.toString());
        p.executeUpdate();
      }
    }
    java.util.concurrent.atomic.AtomicInteger ok = new java.util.concurrent.atomic.AtomicInteger();
    java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
    java.util.concurrent.atomic.AtomicBoolean stop = new java.util.concurrent.atomic.AtomicBoolean();
    Thread client = new Thread(() -> {
      while (!stop.get()) {
        try {
          rows(ds);
          ok.incrementAndGet();
        } catch (Throwable t) {
          error.compareAndSet(null, t);
        }
      }
    });
    client.start();
    Thread.sleep(50);
    long started = System.currentTimeMillis();
    H2StorageMaintenance.Result r = maintenance(ds).compactDatabaseIfNeeded();
    long took = System.currentTimeMillis() - started;
    Thread.sleep(50);
    stop.set(true);
    client.join(10_000);

    assertThat(r.outcome()).isEqualTo(H2StorageMaintenance.Outcome.COMPACTED);
    assertThat(took).isGreaterThan(250);
    assertThat(error.get()).isNull();
    assertThat(ok.get()).isPositive();
    assertThat(ds.getHikariConfigMXBean().getConnectionTimeout()).isEqualTo(250);
    assertThat(rows(ds)).isEqualTo(1520);
  }

  @Test
  void waitsForRunningWorkAndPostpones() throws Exception {
    ds = pool(true);
    bloat(ds);
    long before = new File(dir.toFile(), "gw.mv.db").length();
    StorageMaintenance m = maintenance(ds);
    m.idleWaitMs = 300;
    try (Connection busy = ds.getConnection()) {
      assertThat(m.compactDatabaseIfNeeded()).isNull();
      assertThat(busy.isValid(1)).isTrue();
    }
    assertThat(new File(dir.toFile(), "gw.mv.db.compacted")).doesNotExist(); // never compacted
    assertThat(new File(dir.toFile(), "gw.mv.db").length()).isGreaterThanOrEqualTo(before);
    assertThat(rows(ds)).isEqualTo(20); // pool resumed
  }

  @Test
  void doesNothingWithoutPoolSuspension() throws Exception {
    ds = pool(false);
    bloat(ds);
    assertThat(maintenance(ds).compactDatabaseIfNeeded()).isNull();
    assertThat(rows(ds)).isEqualTo(20);
  }

  @Test
  void prunesOnlyOldTempEntries() throws Exception {
    File temp = dir.resolve("temp").toFile();
    File oldZip = new File(temp, "export-old.zip");
    File oldFolder = new File(temp, "import-old");
    File freshFolder = new File(temp, "import-fresh");
    File inUse = new File(temp, "import-in-use");
    for (File f : new File[] {oldFolder, freshFolder, inUse}) f.mkdirs();
    Files.writeString(oldZip.toPath(), "zip");
    Files.writeString(new File(oldFolder, "a.txt").toPath(), "a");
    Files.writeString(new File(inUse, "new.txt").toPath(), "b");
    long old = System.currentTimeMillis() - 10L * 24 * 3600 * 1000;
    oldZip.setLastModified(old);
    new File(oldFolder, "a.txt").setLastModified(old);
    oldFolder.setLastModified(old);
    inUse.setLastModified(old); // folder is old but holds a recent file

    long removed = StorageMaintenance.pruneOlderThan(temp, 7L * 24 * 3600 * 1000);

    assertThat(removed).isEqualTo(2);
    assertThat(oldZip).doesNotExist();
    assertThat(oldFolder).doesNotExist();
    assertThat(freshFolder).isDirectory();
    assertThat(inUse).isDirectory();
    assertThat(StorageMaintenance.pruneOlderThan(new File(temp, "missing"), 1)).isZero();
  }
}
