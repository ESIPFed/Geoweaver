package com.gw.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
 * Shrinks the embedded H2 database file by running H2's own {@code SHUTDOWN COMPACT}.
 *
 * <p>H2 keeps old versions of updated rows in the file and reclaims them only partly while the
 * database is open, so a file can grow far beyond the size of its data. A full compaction copies
 * the live data, compressed, into {@code <name>.mv.db.tempFile} and then atomically replaces the
 * original. If the process is killed part way, the original file is untouched and H2 deletes the
 * leftover temp file the next time the database is opened.
 *
 * <p>H2's space statistics do not reliably show this waste (a file that compacts from 49 MB to 20
 * KB can report 75% live data), so automatic compaction is driven by growth instead: the size
 * after each compaction is remembered in {@code <name>.mv.db.compacted}, and the file is compacted
 * again once it has grown to twice that size.
 *
 * <p>The database must not be in use by another process. Nothing is done for non-file H2 URLs
 * (memory, TCP server) or for other databases.
 */
public final class H2StorageMaintenance {

  /** Free disk space always left over, on top of the expected size of the compacted copy. */
  static final long DISK_MARGIN_BYTES = 64L * 1024 * 1024;

  /** Compact again when the file has grown to this many times its size after the last compaction. */
  static final int GROWTH_FACTOR = 2;

  /** H2 error code for "Database is already closed", reported after a successful SHUTDOWN. */
  private static final int DATABASE_CALLED_AT_SHUTDOWN = 90121;

  public enum Outcome {
    COMPACTED,
    SKIPPED,
    WOULD_COMPACT
  }

  /** What happened, with file sizes in bytes. {@code sizeAfter} equals {@code sizeBefore} unless compacted. */
  public record Result(Outcome outcome, String reason, long sizeBefore, long sizeAfter, long elapsedMs) {

    public String describe() {
      switch (outcome) {
        case COMPACTED:
          return String.format(
              Locale.ROOT, "Database compacted from %s to %s in %.1f s.",
              human(sizeBefore), human(sizeAfter), elapsedMs / 1000.0);
        case WOULD_COMPACT:
          return "Database file is " + human(sizeBefore) + "; it would be compacted (" + reason + ").";
        default:
          return "Database compaction skipped: " + reason;
      }
    }
  }

  /**
   * When to compact. {@code inProcess} means the database is already open in this JVM (the running
   * server); the original URL is then used so the connection joins that database.
   */
  public record Options(boolean force, long minFileBytes, boolean dryRun, boolean inProcess) {

    /**
     * Compact when the file is at least {@code minFileBytes} and has doubled since the last
     * compaction (or was never compacted by Geoweaver).
     */
    public static Options automatic(long minFileBytes) {
      return new Options(false, minFileBytes, false, false);
    }

    /** Compact regardless of size and growth. */
    public static Options always(boolean dryRun) {
      return new Options(true, 0, dryRun, false);
    }

    /** Same as this, for a database this JVM has open. */
    public Options inRunningServer() {
      return new Options(force, minFileBytes, dryRun, true);
    }
  }

  private H2StorageMaintenance() {}

  /**
   * Returns the {@code .mv.db} file for an embedded H2 URL such as {@code jdbc:h2:file:~/h2/gw;...}
   * or {@code jdbc:h2:~/h2/gw}, or null when the URL is not an embedded H2 file database.
   */
  public static File databaseFile(String jdbcUrl) {
    if (jdbcUrl == null) return null;
    String url = jdbcUrl.trim();
    if (!url.regionMatches(true, 0, "jdbc:h2:", 0, 8)) return null;
    String rest = url.substring(8);
    int semicolon = rest.indexOf(';');
    String path = (semicolon >= 0 ? rest.substring(0, semicolon) : rest).trim();
    String lower = path.toLowerCase(Locale.ROOT);
    if (lower.startsWith("file:")) {
      path = path.substring(5);
    } else if (lower.matches("^[a-z][a-z0-9]+:.*") && !lower.matches("^[a-z]:[\\\\/].*")) {
      // mem:, tcp:, ssl:, zip:, nio:, split:, async: and other non-file stores (but not C:\ paths)
      return null;
    }
    if (path.isEmpty()) return null;
    if (path.equals("~") || path.startsWith("~/") || path.startsWith("~\\")) {
      path = System.getProperty("user.home") + path.substring(1);
    }
    return new File(path + ".mv.db");
  }

  /**
   * Room the compacted copy needs while it is written next to the original: twice the last
   * compacted size (the live data can at most have doubled before the next compaction), or half
   * the file when it was never compacted. Never more than the file itself.
   */
  static long expectedCompactedSize(long fileSize, long lastCompacted) {
    long estimate = lastCompacted > 0 ? lastCompacted * GROWTH_FACTOR : fileSize / 2;
    return Math.min(fileSize, estimate);
  }

  /** Where the size after the last compaction is remembered. */
  static File markerFile(File databaseFile) {
    return new File(databaseFile.getPath() + ".compacted");
  }

  /** Size recorded after the last compaction, or -1 when there is none. */
  static long lastCompactedSize(File databaseFile) {
    try {
      return Long.parseLong(
          Files.readString(markerFile(databaseFile).toPath(), StandardCharsets.UTF_8).trim());
    } catch (IOException | NumberFormatException e) {
      return -1;
    }
  }

  private static void recordCompactedSize(File databaseFile, long size) {
    try {
      Files.writeString(markerFile(databaseFile).toPath(), Long.toString(size), StandardCharsets.UTF_8);
    } catch (IOException ignored) {
      // only means the next start-up checks again
    }
  }

  /**
   * The URL used for maintenance: same database and options, but without settings that would let
   * another process share the file while it is being compacted or that run start-up SQL, and
   * never creating a new database.
   */
  static String maintenanceUrl(String jdbcUrl) {
    String[] parts = jdbcUrl.trim().split(";");
    StringBuilder sb = new StringBuilder(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i].trim();
      if (part.isEmpty()) continue;
      String key = part.split("=", 2)[0].trim().toUpperCase(Locale.ROOT);
      switch (key) {
        case "AUTO_SERVER":
        case "AUTO_SERVER_PORT":
        case "INIT":
        case "IFEXISTS":
        case "DB_CLOSE_ON_EXIT":
        case "ACCESS_MODE_DATA":
          continue;
        default:
          sb.append(';').append(part);
      }
    }
    return sb.append(";IFEXISTS=TRUE;DB_CLOSE_ON_EXIT=FALSE").toString();
  }

  /** Decides whether the database needs compacting and does it. Never throws. */
  public static Result compact(String jdbcUrl, String user, String password, Options options) {
    long start = System.currentTimeMillis();
    File file = databaseFile(jdbcUrl);
    if (file == null) return skipped("not an embedded H2 file database", 0, start);
    if (!file.isFile()) return skipped("no database file at " + file, 0, start);

    long sizeBefore = file.length();
    long last = lastCompactedSize(file);
    String why;
    if (options.force()) {
      why = "requested";
    } else if (sizeBefore < options.minFileBytes()) {
      return skipped("file is only " + human(sizeBefore), sizeBefore, start);
    } else if (last > 0 && sizeBefore < last * GROWTH_FACTOR) {
      return skipped(
          "file is " + human(sizeBefore) + ", last compacted to " + human(last), sizeBefore, start);
    } else {
      why = last > 0 ? "grown from " + human(last) + " since the last compaction" : "never compacted";
    }

    long needed = expectedCompactedSize(sizeBefore, last) + DISK_MARGIN_BYTES;
    long free = file.getAbsoluteFile().getParentFile().getUsableSpace();
    if (free < needed) {
      return skipped(
          "keeps at least " + human(needed) + " of disk free, only " + human(free) + " available",
          sizeBefore, start);
    }
    if (options.dryRun()) {
      return new Result(Outcome.WOULD_COMPACT, why, sizeBefore, sizeBefore, elapsed(start));
    }

    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      return skipped("H2 driver not on the classpath", sizeBefore, start);
    }

    File temp = new File(file.getPath() + ".tempFile");
    String url = options.inProcess() ? jdbcUrl : maintenanceUrl(jdbcUrl);
    try (Connection conn = DriverManager.getConnection(url, user, password);
        Statement st = conn.createStatement()) {
      st.execute("SHUTDOWN COMPACT");
    } catch (SQLException e) {
      if (e.getErrorCode() != DATABASE_CALLED_AT_SHUTDOWN) {
        // Locked by a running Geoweaver or H2 console, wrong password, or out of disk space part
        // way. The original file is intact; a partial copy is removed.
        boolean started = temp.exists();
        if (started && !temp.delete()) temp.deleteOnExit();
        if (started) {
          // The copy itself failed (usually the disk filled up). Do not retry on every start-up;
          // wait until the file has doubled again, or until db-compact is run by hand.
          recordCompactedSize(file, sizeBefore);
        }
        return skipped("could not compact (" + firstLine(e) + ")", sizeBefore, start);
      }
    }
    long sizeAfter = file.length();
    recordCompactedSize(file, sizeAfter);
    return new Result(Outcome.COMPACTED, why, sizeBefore, sizeAfter, elapsed(start));
  }

  private static Result skipped(String reason, long size, long start) {
    return new Result(Outcome.SKIPPED, reason, size, size, elapsed(start));
  }

  private static long elapsed(long start) {
    return System.currentTimeMillis() - start;
  }

  private static String firstLine(Exception e) {
    String m = String.valueOf(e.getMessage());
    int nl = m.indexOf('\n');
    return nl >= 0 ? m.substring(0, nl) : m;
  }

  static String human(long bytes) {
    if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
    if (bytes < 1024L * 1024 * 1024) return String.format(Locale.ROOT, "%.0f MB", bytes / 1048576.0);
    return String.format(Locale.ROOT, "%.2f GB", bytes / 1073741824.0);
  }
}
