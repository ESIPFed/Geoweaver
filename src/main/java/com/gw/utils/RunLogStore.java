package com.gw.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Keeps large process logs in files instead of the database.
 *
 * <p>A log stays in the database while it is small. Once it grows past the limit (64 KB by
 * default) the whole log is written to {@code <dir>/<first two id chars>/<history id>.log}, new
 * lines are appended to that file, and the database keeps only a short header plus the last part
 * of the log. When the run ends the file is compressed to {@code .log.gz}. This keeps the
 * database small and its size proportional to the number of runs, not to how much they print,
 * and keeps memory per running process bounded.
 *
 * <p>Settings (environment variables or system properties):
 *
 * <ul>
 *   <li>{@code GEOWEAVER_RUN_LOG_DIR} / {@code geoweaver.run.log.dir} – where log files go
 *       (default {@code ~/geoweaver/run-logs}); any mounted disk works.
 *   <li>{@code GEOWEAVER_RUN_LOG_DB_LIMIT_KB} / {@code geoweaver.run.log.db.limit.kb} – largest
 *       log kept entirely in the database (default 64).
 *   <li>{@code GEOWEAVER_RUN_LOG_MAX_MB} / {@code geoweaver.run.log.max.mb} – most disk space all
 *       log files may use together (default 2048; 0 = no limit). The oldest files are removed
 *       first; the database still has the end of each of those logs.
 *   <li>{@code GEOWEAVER_MIN_FREE_MB} / {@code geoweaver.min.free.mb} – when less disk space than
 *       this is free (default 512), no new log file is started and only the end of the log is kept.
 * </ul>
 */
public final class RunLogStore {

  public static final int DEFAULT_DB_LIMIT_KB = 64;

  public static final long DEFAULT_MAX_MB = 2048;

  public static final long DEFAULT_MIN_FREE_MB = 512;

  /** Most log text sent to the browser for one run (the end of the log). */
  public static final int UI_LOG_LIMIT_CHARS = 5 * 1024 * 1024;

  /** First line of a database copy whose full text is in a file. */
  static final String HEADER_PREFIX = "[Geoweaver: showing the last ";

  private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");

  private RunLogStore() {}

  public static File directory() {
    String dir = setting("geoweaver.run.log.dir", "GEOWEAVER_RUN_LOG_DIR");
    if (dir == null) dir = System.getProperty("user.home") + File.separator + "geoweaver" + File.separator + "run-logs";
    return new File(dir);
  }

  /** Largest number of characters kept in the database for one log. */
  public static int dbLimitChars() {
    String v = setting("geoweaver.run.log.db.limit.kb", "GEOWEAVER_RUN_LOG_DB_LIMIT_KB");
    int kb = DEFAULT_DB_LIMIT_KB;
    if (v != null) {
      try {
        kb = Math.max(1, Integer.parseInt(v.trim()));
      } catch (NumberFormatException ignored) {
        // keep default
      }
    }
    return kb * 1024;
  }

  /** Most bytes all log files may use together, or 0 for no limit. */
  public static long maxTotalBytes() {
    return megabytes("geoweaver.run.log.max.mb", "GEOWEAVER_RUN_LOG_MAX_MB", DEFAULT_MAX_MB);
  }

  /** Free disk space below which no new log file is started. */
  public static long minFreeBytes() {
    return megabytes("geoweaver.min.free.mb", "GEOWEAVER_MIN_FREE_MB", DEFAULT_MIN_FREE_MB);
  }

  static long megabytes(String property, String env, long defaultMb) {
    String v = setting(property, env);
    long mb = defaultMb;
    if (v != null) {
      try {
        mb = Math.max(0, Long.parseLong(v.trim()));
      } catch (NumberFormatException ignored) {
        // keep default
      }
    }
    return mb * 1024 * 1024;
  }

  /** Free space on the disk holding the log folder (or its nearest existing parent). */
  static long freeSpace() {
    File d = directory().getAbsoluteFile();
    while (d != null && !d.exists()) d = d.getParentFile();
    return d == null ? Long.MAX_VALUE : d.getUsableSpace();
  }

  private static final java.util.concurrent.atomic.AtomicLong lastLimitCheck = new java.util.concurrent.atomic.AtomicLong();

  /** Applies the size limit after a log file is finished, at most once a minute. */
  static void enforceLimitSoon() {
    long now = System.currentTimeMillis();
    long last = lastLimitCheck.get();
    if (now - last < 60_000 || !lastLimitCheck.compareAndSet(last, now)) return;
    try {
      enforceLimit(maxTotalBytes());
    } catch (RuntimeException ignored) {
      // the periodic check tries again
    }
  }

  /** What {@link #enforceLimit} removed. */
  public record Pruned(int files, long bytes, long remainingBytes) {}

  /**
   * Removes the oldest finished log files until all log files together use at most
   * {@code maxBytes}. Logs of running processes are never removed. Never throws.
   */
  public static Pruned enforceLimit(long maxBytes) {
    if (maxBytes <= 0) return new Pruned(0, 0, -1);
    java.util.List<File> finished = new java.util.ArrayList<>();
    long total = 0;
    File[] subdirs = directory().listFiles(File::isDirectory);
    if (subdirs == null) return new Pruned(0, 0, 0);
    for (File sub : subdirs) {
      File[] files = sub.listFiles(File::isFile);
      if (files == null) continue;
      for (File f : files) {
        total += f.length();
        if (f.getName().endsWith(".log.gz")) finished.add(f);
      }
    }
    if (total <= maxBytes) return new Pruned(0, 0, total);
    finished.sort(java.util.Comparator.comparingLong(File::lastModified));
    int removed = 0;
    long removedBytes = 0;
    for (File f : finished) {
      if (total <= maxBytes) break;
      long len = f.length();
      if (f.delete()) {
        total -= len;
        removed++;
        removedBytes += len;
      }
    }
    return new Pruned(removed, removedBytes, total);
  }

  private static String setting(String property, String env) {
    String v = System.getProperty(property);
    if (v == null || v.isBlank()) v = System.getenv(env);
    return v == null || v.isBlank() ? null : v.trim();
  }

  static boolean isSafeId(String historyId) {
    return historyId != null && SAFE_ID.matcher(historyId).matches();
  }

  /** File used while the run is going. */
  static File activeFile(String historyId) {
    return new File(new File(directory(), historyId.substring(0, Math.min(2, historyId.length()))), historyId + ".log");
  }

  /** File kept after the run finished. */
  static File finishedFile(String historyId) {
    File active = activeFile(historyId);
    return new File(active.getParentFile(), historyId + ".log.gz");
  }

  /** Starts collecting the log of one run. */
  public static RunLog open(String historyId) {
    return new RunLog(historyId, dbLimitChars());
  }

  /** True when the database copy of a log is only its tail and the full text is in a file. */
  public static boolean isTruncatedCopy(String dbText) {
    return dbText != null && dbText.startsWith(HEADER_PREFIX);
  }

  /**
   * Returns up to the last {@code maxChars} characters of a run's log: from its file when there is
   * one, otherwise the database copy. Never throws.
   */
  public static String readTail(String historyId, String dbText, int maxChars) {
    if (isSafeId(historyId) && isTruncatedCopy(dbText)) {
      try {
        File gz = finishedFile(historyId);
        if (gz.isFile()) return tailOf(new InputStreamReader(new GZIPInputStream(new FileInputStream(gz)), StandardCharsets.UTF_8), maxChars, gz.length() * 8);
        File active = activeFile(historyId);
        if (active.isFile()) return tailOfPlainFile(active, maxChars);
      } catch (IOException e) {
        // fall back to the database copy
      }
    }
    if (dbText == null) return null;
    return dbText.length() <= maxChars ? dbText : dbText.substring(dbText.length() - maxChars);
  }

  /** Full log text (use for exports and command-line output; may be large). */
  public static String readFull(String historyId, String dbText) {
    return readTail(historyId, dbText, Integer.MAX_VALUE);
  }

  /** Removes the files of a deleted run. */
  public static void delete(String historyId) {
    if (!isSafeId(historyId)) return;
    deleteQuietly(activeFile(historyId));
    deleteQuietly(finishedFile(historyId));
  }

  private static void deleteQuietly(File f) {
    try {
      Files.deleteIfExists(f.toPath());
    } catch (IOException ignored) {
      // a leftover file only costs disk space
    }
  }

  private static String tailOf(Reader reader, int maxChars, long sizeHint) throws IOException {
    try (BufferedReader r = new BufferedReader(reader)) {
      if (maxChars == Integer.MAX_VALUE) {
        StringBuilder all = new StringBuilder((int) Math.min(Integer.MAX_VALUE - 8, Math.max(16, sizeHint)));
        char[] buf = new char[8192];
        int n;
        while ((n = r.read(buf)) > 0) all.append(buf, 0, n);
        return all.toString();
      }
      // keep a sliding window of at most 2 x maxChars
      StringBuilder window = new StringBuilder();
      char[] buf = new char[8192];
      int n;
      while ((n = r.read(buf)) > 0) {
        window.append(buf, 0, n);
        if (window.length() > 2 * maxChars) window.delete(0, window.length() - maxChars);
      }
      return window.length() <= maxChars ? window.toString() : window.substring(window.length() - maxChars);
    }
  }

  private static String tailOfPlainFile(File f, int maxChars) throws IOException {
    long len = f.length();
    if (maxChars == Integer.MAX_VALUE || len <= (long) maxChars) {
      return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
    // read the last maxChars bytes (at least maxChars/4 characters of UTF-8) and trim to a char boundary
    try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
      long start = Math.max(0, len - maxChars);
      byte[] b = new byte[(int) (len - start)];
      raf.seek(start);
      raf.readFully(b);
      int i = 0;
      while (i < b.length && (b[i] & 0xC0) == 0x80) i++; // skip a split UTF-8 sequence
      return new String(b, i, b.length - i, StandardCharsets.UTF_8);
    }
  }

  static String human(long chars) {
    if (chars < 1024 * 1024) return (chars / 1024) + " KB";
    return String.format(Locale.ROOT, "%.1f MB", chars / 1048576.0);
  }

  /** Log of one running process. Not thread-safe; used by the single thread reading the output. */
  public static final class RunLog {

    private final String historyId;
    private final int limit;
    private final StringBuilder buffer = new StringBuilder();
    private long totalChars;
    private BufferedWriter writer;
    private boolean spilled;
    private boolean failedToSpill;
    private String notSavedReason;

    RunLog(String historyId, int limit) {
      this.historyId = historyId;
      this.limit = limit;
    }

    /** Adds one line of output. */
    public RunLog appendLine(String line) {
      String s = (line == null ? "null" : line) + "\n";
      totalChars += s.length();
      buffer.append(s);
      if (spilled) {
        write(s);
        if (buffer.length() > 2 * limit) buffer.delete(0, buffer.length() - limit);
      } else if (buffer.length() > limit && !failedToSpill) {
        spill();
      } else if (failedToSpill && buffer.length() > 2 * limit) {
        // no file: keep only the end so neither memory nor the database grows with the output
        buffer.delete(0, buffer.length() - limit);
      }
      return this;
    }

    public boolean isSpilled() {
      return spilled;
    }

    public long totalChars() {
      return totalChars;
    }

    /** Text to store in the database: the whole log while small, otherwise a header and the tail. */
    public String databaseText() {
      flush();
      if (!spilled && totalChars <= limit) return buffer.toString();
      String tail = buffer.length() <= limit ? buffer.toString() : buffer.substring(buffer.length() - limit);
      int nl = tail.indexOf('\n');
      if (nl >= 0 && nl < tail.length() - 1 && buffer.length() > limit) tail = tail.substring(nl + 1);
      String where = spilled
          ? "the full log is saved to " + finishedFile(historyId).getPath()
          : "the full log was not saved (" + notSavedReason + ")";
      return HEADER_PREFIX + human(tail.length()) + " of a " + human(totalChars) + " log; " + where + "]\n" + tail;
    }

    /** Ends the run: closes and compresses the log file. Safe to call more than once. */
    public void close() {
      if (writer == null) return;
      try {
        writer.close();
      } catch (IOException ignored) {
        // compress what was written
      }
      writer = null;
      File active = activeFile(historyId);
      File gz = finishedFile(historyId);
      File tmp = new File(gz.getPath() + ".tmp");
      try (InputStream in = new FileInputStream(active);
          OutputStream out = new GZIPOutputStream(new FileOutputStream(tmp))) {
        in.transferTo(out);
      } catch (IOException e) {
        deleteQuietly(tmp);
        return; // keep the plain file; readers find it
      }
      try {
        Files.move(tmp.toPath(), gz.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        deleteQuietly(active);
      } catch (IOException e) {
        deleteQuietly(tmp);
      }
      enforceLimitSoon();
    }

    private void spill() {
      if (!isSafeId(historyId)) {
        failedToSpill = true;
        notSavedReason = "unusual run id";
        return;
      }
      long free = freeSpace();
      if (free < minFreeBytes()) {
        failedToSpill = true;
        notSavedReason = "low disk space: " + human(free) + " free";
        return;
      }
      File active = activeFile(historyId);
      try {
        Files.createDirectories(active.getParentFile().toPath());
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(active, false), StandardCharsets.UTF_8));
        writer.write(buffer.toString());
        spilled = true;
        if (buffer.length() > 2 * limit) buffer.delete(0, buffer.length() - limit);
      } catch (IOException e) {
        // disk problem: keep the end of the log in the database
        failedToSpill = true;
        notSavedReason = "could not write the log file: " + e.getMessage();
        writer = null;
      }
    }

    private void write(String s) {
      try {
        if (writer != null) writer.write(s);
      } catch (IOException e) {
        // the database tail still has the latest output
      }
    }

    private void flush() {
      try {
        if (writer != null) writer.flush();
      } catch (IOException ignored) {
        // see write()
      }
    }
  }
}
