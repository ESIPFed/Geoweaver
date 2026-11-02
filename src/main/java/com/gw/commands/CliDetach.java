package com.gw.commands;

import com.gw.GeoweaverApplication;
import com.gw.database.HistoryRepository;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Starts this same run command again as a separate process that keeps running after the command
 * returns, the SSH session closes, or the program that called it (a coding agent's shell) is
 * stopped. The new process gets its own session, so hang-up signals and process-group kills aimed
 * at the caller do not reach it. Its output goes to {@code ~/geoweaver/cli-runs/<run id>.log}.
 *
 * <p>Passwords are handed over in the environment, which only the owner can read, never on the
 * command line, which every user of a shared login node can see with {@code ps}.
 */
final class CliDetach {

  static final String PASSWORDS_ENV = "GEOWEAVER_CLI_PASSWORDS";
  static final Pattern RUN_ID = Pattern.compile("[A-Za-z0-9]{6,40}");
  private static final long KEEP_LOGS_MS = 7L * 24 * 3600 * 1000;
  private static final long START_TIMEOUT_MS = 90_000;

  private CliDetach() {}

  static File logDir() {
    return new File(System.getProperty("user.home"), "geoweaver/cli-runs");
  }

  /** Passwords handed over by the command that detached this one, or null. */
  static String[] inheritedPasswords() {
    String v = System.getenv(PASSWORDS_ENV);
    if (v == null || v.isEmpty()) return null;
    try {
      return CliOutput.mapper().readValue(v, String[].class);
    } catch (Exception e) {
      return null;
    }
  }

  /** The command line for the detached copy: same arguments without passwords and --detach. */
  static List<String> childArgs(String[] args, String runId) {
    List<String> out = new ArrayList<>();
    for (int i = 0; i < args.length; i++) {
      String a = args[i];
      if ("--detach".equals(a)) continue;
      if (a.equals("-p") || a.equals("--password") || a.equals("--passwords")) {
        i++; // skip its value
        continue;
      }
      if (a.startsWith("--password=") || a.startsWith("--passwords=") || (a.startsWith("-p") && a.length() > 2 && !a.startsWith("--"))) continue;
      if ("--run-id".equals(a)) {
        i++;
        continue;
      }
      out.add(a);
    }
    out.add("--run-id");
    out.add(runId);
    return out;
  }

  /** How this JVM was started: java, its options, and -jar or the class path and main class. */
  static List<String> javaCommand() {
    List<String> cmd = new ArrayList<>();
    cmd.add(ProcessHandle.current().info().command().orElse(
        System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"));
    for (String opt : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
      if (opt.startsWith("-agentlib") || opt.startsWith("-javaagent") || opt.startsWith("-Xrunjdwp")) continue;
      cmd.add(opt);
    }
    String launched = System.getProperty("sun.java.command", "");
    String first = launched.split(" ")[0];
    if (first.endsWith(".jar")) {
      cmd.add("-jar");
      cmd.add(new File(first).getAbsolutePath());
    } else {
      cmd.add("-cp");
      cmd.add(System.getProperty("java.class.path"));
      cmd.add(first.isEmpty() ? GeoweaverApplication.class.getName() : first);
    }
    return cmd;
  }

  /** A way to start the child in a new session, or nothing when the system has none. */
  static List<String> newSessionPrefix() {
    for (String p : new String[] {"/usr/bin/setsid", "/bin/setsid", "/usr/local/bin/setsid"}) {
      if (new File(p).canExecute()) return List.of(p);
    }
    for (String p : new String[] {"/usr/bin/perl", "/bin/perl"}) {
      if (new File(p).canExecute()) {
        return List.of(p, "-MPOSIX", "-e", "POSIX::setsid(); exec @ARGV or die $!");
      }
    }
    return List.of();
  }

  /**
   * Starts the detached copy, waits until its run is in the database, and prints {@code {runId,
   * status, detached, pid, log}}.
   */
  static void launch(String[] passwords) {
    String runId = new RandomString(18).nextString();
    try {
      File dir = logDir();
      dir.mkdirs();
      cleanOldLogs(dir);
      File log = new File(dir, runId + ".log");
      List<String> cmd = new ArrayList<>(newSessionPrefix());
      cmd.addAll(javaCommand());
      cmd.addAll(childArgs(GeoweaverApplication.cliArgs, runId));
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.redirectErrorStream(true);
      pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
      File devNull = new File(System.getProperty("os.name").toLowerCase().contains("win") ? "NUL" : "/dev/null");
      pb.redirectInput(ProcessBuilder.Redirect.from(devNull));
      if (passwords != null) {
        pb.environment().put(PASSWORDS_ENV, CliOutput.mapper().writeValueAsString(passwords));
      } else {
        pb.environment().remove(PASSWORDS_ENV);
      }
      Process child = pb.start();

      HistoryRepository repo = BeanTool.getBean(HistoryRepository.class);
      long deadline = System.currentTimeMillis() + START_TIMEOUT_MS;
      String status = null;
      while (System.currentTimeMillis() < deadline) {
        var h = repo.findById(runId);
        if (h.isPresent()) {
          String ind = h.get().getIndicator();
          status = ind == null || ind.isBlank() ? "Running" : ind; // row first, status a moment later
          break;
        }
        if (!child.isAlive()) break;
        Thread.sleep(500);
      }
      if (status == null) {
        String tail = tail(log, 2000);
        CliOutput.fail(
            (child.isAlive() ? "The run did not start within 90 s" : "The run could not start")
                + ". Its output (" + log + "):\n" + tail,
            1);
        return;
      }
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("runId", runId);
      out.put("status", status);
      out.put("detached", true);
      out.put("pid", child.pid());
      out.put("log", log.getAbsolutePath());
      out.put("next", "geoweaver run status " + runId + " --wait 60 --json");
      CliOutput.print(out);
    } catch (Exception e) {
      CliOutput.fail(e);
    }
  }

  private static String tail(File f, int chars) {
    try {
      String s = Files.readString(f.toPath());
      return s.length() > chars ? s.substring(s.length() - chars) : s;
    } catch (Exception e) {
      return "";
    }
  }

  private static void cleanOldLogs(File dir) {
    File[] files = dir.listFiles((d, n) -> n.endsWith(".log"));
    if (files == null) return;
    long cutoff = System.currentTimeMillis() - KEEP_LOGS_MS;
    for (File f : files) {
      if (f.lastModified() < cutoff) f.delete();
    }
  }
}
