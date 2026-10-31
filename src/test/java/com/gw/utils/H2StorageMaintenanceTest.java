package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

class H2StorageMaintenanceTest {

  private static final String USER = "test";
  private static final String PASS = "test-only";

  @TempDir Path dir;

  private String url(String name) {
    return "jdbc:h2:file:" + dir.resolve(name).toAbsolutePath() + ";AUTO_SERVER=TRUE;INIT=create domain if not exists jsonb as text";
  }

  /** Rewrites one growing log the way a running process used to: the whole text on every line. */
  private void writeBloatedDatabase(String url, int lines) throws Exception {
    try (Connection c = DriverManager.getConnection(url + ";MAX_COMPACT_TIME=0", USER, PASS);
        Statement st = c.createStatement()) {
      st.execute("CREATE TABLE history(id VARCHAR(20) PRIMARY KEY, output CLOB)");
      st.execute("INSERT INTO history VALUES('h1', '')");
      StringBuilder log = new StringBuilder();
      try (PreparedStatement ps = c.prepareStatement("UPDATE history SET output = ? WHERE id = 'h1'")) {
        for (int i = 0; i < lines; i++) {
          log.append("line ").append(i).append(" of a long running process with some output text\n");
          ps.setString(1, log.toString());
          ps.executeUpdate();
        }
      }
    }
  }

  private long storedLength(String url) throws Exception {
    try (Connection c = DriverManager.getConnection(url, USER, PASS);
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery("SELECT CHAR_LENGTH(output) FROM history WHERE id = 'h1'")) {
      rs.next();
      return rs.getLong(1);
    }
  }

  @Test
  void findsTheFileForEmbeddedUrlsOnly() {
    String home = System.getProperty("user.home");
    assertThat(H2StorageMaintenance.databaseFile("jdbc:h2:file:~/h2/gw;AUTO_SERVER=true"))
        .isEqualTo(new File(home + "/h2/gw.mv.db"));
    assertThat(H2StorageMaintenance.databaseFile("jdbc:h2:/data/gw")).isEqualTo(new File("/data/gw.mv.db"));
    assertThat(H2StorageMaintenance.databaseFile("jdbc:h2:C:/data/gw")).isEqualTo(new File("C:/data/gw.mv.db"));
    assertThat(H2StorageMaintenance.databaseFile("jdbc:h2:mem:test")).isNull();
    assertThat(H2StorageMaintenance.databaseFile("jdbc:h2:nio:/data/gw")).isNull();
    assertThat(H2StorageMaintenance.databaseFile("jdbc:h2:tcp://localhost/~/gw")).isNull();
    assertThat(H2StorageMaintenance.databaseFile("jdbc:postgresql://localhost/gw")).isNull();
    assertThat(H2StorageMaintenance.databaseFile(null)).isNull();
  }

  @Test
  void maintenanceUrlDropsSharingAndStartupSql() {
    String m =
        H2StorageMaintenance.maintenanceUrl(
            "jdbc:h2:file:~/h2/gw;AUTO_SERVER=true;INIT=create domain if not exists jsonb as text;CACHE_SIZE=2048");
    assertThat(m)
        .isEqualTo("jdbc:h2:file:~/h2/gw;CACHE_SIZE=2048;IFEXISTS=TRUE;DB_CLOSE_ON_EXIT=FALSE");
  }

  @Test
  void compactsABloatedFileAndKeepsTheData() throws Exception {
    String url = url("bloated");
    writeBloatedDatabase(url, 1500);
    File file = H2StorageMaintenance.databaseFile(url);
    long expectedChars = storedLength(url);
    long before = file.length();

    H2StorageMaintenance.Result r =
        H2StorageMaintenance.compact(url, USER, PASS, H2StorageMaintenance.Options.automatic(0));

    assertThat(r.outcome()).as(r.describe()).isEqualTo(H2StorageMaintenance.Outcome.COMPACTED);
    assertThat(r.sizeBefore()).isEqualTo(before);
    assertThat(file.length()).isLessThan(before / 5);
    assertThat(new File(file.getPath() + ".tempFile")).doesNotExist();
    assertThat(storedLength(url)).isEqualTo(expectedChars);
    assertThat(H2StorageMaintenance.lastCompactedSize(file)).isEqualTo(r.sizeAfter());

    // Right after a compaction the file has not grown enough to be worth another pass.
    H2StorageMaintenance.Result again =
        H2StorageMaintenance.compact(url, USER, PASS, H2StorageMaintenance.Options.automatic(0));
    assertThat(again.outcome()).as(again.describe()).isEqualTo(H2StorageMaintenance.Outcome.SKIPPED);
  }

  @Test
  void compactsAgainOnceTheFileHasDoubled() throws Exception {
    String url = url("regrow");
    writeBloatedDatabase(url, 1500);
    File file = H2StorageMaintenance.databaseFile(url);
    // Pretend an earlier compaction left a file far smaller than today's.
    java.nio.file.Files.writeString(
        H2StorageMaintenance.markerFile(file).toPath(), Long.toString(file.length() / 3));
    H2StorageMaintenance.Result r =
        H2StorageMaintenance.compact(url, USER, PASS, H2StorageMaintenance.Options.automatic(0));
    assertThat(r.outcome()).as(r.describe()).isEqualTo(H2StorageMaintenance.Outcome.COMPACTED);
    assertThat(r.reason()).startsWith("grown from");
  }

  @Test
  void dryRunAndThresholdsLeaveTheFileAlone() throws Exception {
    String url = url("dry");
    writeBloatedDatabase(url, 400);
    File file = H2StorageMaintenance.databaseFile(url);
    long before = file.length();

    H2StorageMaintenance.Result dry =
        H2StorageMaintenance.compact(url, USER, PASS, H2StorageMaintenance.Options.always(true));
    assertThat(dry.outcome()).isEqualTo(H2StorageMaintenance.Outcome.WOULD_COMPACT);

    H2StorageMaintenance.Result small =
        H2StorageMaintenance.compact(url, USER, PASS, H2StorageMaintenance.Options.automatic(before * 10));
    assertThat(small.outcome()).isEqualTo(H2StorageMaintenance.Outcome.SKIPPED);

    assertThat(file.length()).isGreaterThan(before / 2);
  }

  @Test
  void missingDatabaseIsNotCreated() {
    String url = url("absent");
    H2StorageMaintenance.Result r =
        H2StorageMaintenance.compact(url, USER, PASS, H2StorageMaintenance.Options.always(false));
    assertThat(r.outcome()).isEqualTo(H2StorageMaintenance.Outcome.SKIPPED);
    assertThat(H2StorageMaintenance.databaseFile(url)).doesNotExist();
  }

  @Test
  void wrongPasswordIsSkippedWithoutDamage() throws Exception {
    String url = url("locked");
    writeBloatedDatabase(url, 200);
    File file = H2StorageMaintenance.databaseFile(url);
    long before = file.length();
    H2StorageMaintenance.Result r =
        H2StorageMaintenance.compact(url, USER, "wrong", H2StorageMaintenance.Options.always(false));
    assertThat(r.outcome()).isEqualTo(H2StorageMaintenance.Outcome.SKIPPED);
    assertThat(file.length()).isGreaterThanOrEqualTo(before);
    assertThat(storedLength(url)).isPositive();
  }

  @Test
  void startupListenerCompactsWhenEnabledAndRespectsTheSwitch() throws Exception {
    String url = url("startup");
    writeBloatedDatabase(url, 1500);
    File file = H2StorageMaintenance.databaseFile(url);
    long before = file.length();

    MockEnvironment off = new MockEnvironment()
        .withProperty("spring.datasource.url", url)
        .withProperty("spring.datasource.username", USER)
        .withProperty("spring.datasource.password", PASS)
        .withProperty("GEOWEAVER_DB_COMPACT_MIN_MB", "0")
        .withProperty("GEOWEAVER_DB_AUTO_COMPACT", "false");
    new H2StartupCompaction().run(off);
    assertThat(file.length()).isGreaterThanOrEqualTo(before);

    MockEnvironment on = new MockEnvironment()
        .withProperty("spring.datasource.url", url)
        .withProperty("spring.datasource.username", USER)
        .withProperty("spring.datasource.password", PASS)
        .withProperty("GEOWEAVER_DB_COMPACT_MIN_MB", "0");
    new H2StartupCompaction().run(on);
    assertThat(file.length()).isLessThan(before / 5);
  }

  @Test
  void compactionNeedsRoomForTheCompactedCopyOnly() {
    long mb = 1024L * 1024;
    assertThat(H2StorageMaintenance.expectedCompactedSize(1000 * mb, -1)).isEqualTo(500 * mb);
    assertThat(H2StorageMaintenance.expectedCompactedSize(1000 * mb, 100 * mb)).isEqualTo(200 * mb);
    assertThat(H2StorageMaintenance.expectedCompactedSize(150 * mb, 100 * mb)).isEqualTo(150 * mb);
  }

  @Test
  void compressionIsAddedToEmbeddedFileUrlsOnly() {
    assertThat(H2StartupCompaction.withCompression("jdbc:h2:file:~/h2/gw;AUTO_SERVER=true"))
        .isEqualTo("jdbc:h2:file:~/h2/gw;AUTO_SERVER=true;COMPRESS=TRUE");
    assertThat(H2StartupCompaction.withCompression("jdbc:h2:file:~/h2/gw;compress=false"))
        .isEqualTo("jdbc:h2:file:~/h2/gw;compress=false");
    assertThat(H2StartupCompaction.withCompression("jdbc:h2:mem:testdb")).isEqualTo("jdbc:h2:mem:testdb");
    assertThat(H2StartupCompaction.withCompression("jdbc:postgresql://x/db")).isEqualTo("jdbc:postgresql://x/db");
    assertThat(H2StartupCompaction.withCompression(null)).isNull();
  }
}
