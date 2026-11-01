package com.gw.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Runs before the database is opened, for the server and for every command-line call.
 *
 * <p>Several Geoweaver processes can use one database at the same time: the server, commands run
 * in an SSH session, a second server in an Open OnDemand session sharing the home folder. H2's
 * {@code AUTO_SERVER} mode makes the first process that opens the file serve it to the others
 * over TCP; its address is in the {@code .lock.db} file next to the database. When that process
 * runs on another machine that this one cannot reach, H2 only reports "Database may be already in
 * use"; this check says which machine holds the database and what to do instead.
 *
 * <p>It also makes the database folder and {@code ~/geoweaver} private to the user (when they are
 * inside the home folder). The database password in the default settings is public and the
 * {@code AUTO_SERVER} port accepts connections from other machines, so the key in the lock file
 * and the file itself must not be readable by other users of a shared system. {@code
 * GEOWEAVER_PRIVATE_FILES=false} leaves permissions alone.
 */
public class SharedDatabaseCheck implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  static final int CONNECT_TIMEOUT_MS = 3000;

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    try {
      run(event.getEnvironment());
    } catch (Throwable t) {
      // never block start-up
    }
  }

  void run(ConfigurableEnvironment env) {
    File db = H2StorageMaintenance.databaseFile(env.getProperty("spring.datasource.url"));
    if (env.getProperty("GEOWEAVER_PRIVATE_FILES", Boolean.class, true)) {
      String home = System.getProperty("user.home");
      if (db != null) makePrivate(db.getAbsoluteFile().getParentFile(), home);
      makePrivate(new File(home, "geoweaver"), home);
    }
    if (db == null) return;
    String note = describeHolder(lockFile(db), localHostNames());
    if (note != null) System.out.println(note);
  }

  static File lockFile(File mvDb) {
    String p = mvDb.getPath();
    String base = p.endsWith(".mv.db") ? p.substring(0, p.length() - ".mv.db".length()) : p;
    return new File(base + ".lock.db");
  }

  /**
   * Returns a message when the database is held by a Geoweaver on another machine that cannot be
   * reached from here, otherwise null (no lock, held here, reachable, or a stale lock that H2 will
   * take over).
   */
  static String describeHolder(File lock, Set<String> localNames) {
    if (!lock.isFile()) return null;
    Properties p = new Properties();
    try (InputStream in = new FileInputStream(lock)) {
      p.load(in);
    } catch (IOException e) {
      return null;
    }
    String server = p.getProperty("server");
    if (server == null || server.isBlank()) return null;
    String hostName = p.getProperty("hostName", "");
    if (localNames.contains(hostName) || localNames.contains(hostOf(server))) return null; // here
    if (reachable(server)) return null;
    String where = hostName.isBlank() ? server : hostName + " (" + server + ")";
    return String.join(
        System.lineSeparator(),
        "The Geoweaver database " + lock.getParent() + " is held by a Geoweaver process on " + where + ",",
        "which cannot be reached from this machine. Geoweaver processes share one database through",
        "the first one that opens it. If that Geoweaver is still running, either",
        "  - use it instead: its browser page or its Agent API (see docs/ssh-and-gui.md), or",
        "  - run this command on " + (hostName.isBlank() ? "that machine" : hostName)
            + ", or stop that Geoweaver first.",
        "If it is no longer running, H2 takes the database over in a few seconds.");
  }

  static String hostOf(String server) {
    int colon = server.lastIndexOf(':');
    String h = colon > 0 ? server.substring(0, colon) : server;
    return h.startsWith("[") && h.endsWith("]") ? h.substring(1, h.length() - 1) : h;
  }

  static boolean reachable(String server) {
    int colon = server.lastIndexOf(':');
    if (colon <= 0) return false;
    try (Socket s = new Socket()) {
      s.connect(
          new InetSocketAddress(hostOf(server), Integer.parseInt(server.substring(colon + 1))),
          CONNECT_TIMEOUT_MS);
      return true;
    } catch (IOException | RuntimeException e) {
      return false;
    }
  }

  static Set<String> localHostNames() {
    Set<String> names = new java.util.HashSet<>();
    names.add("localhost");
    names.add("127.0.0.1");
    try {
      java.net.InetAddress local = java.net.InetAddress.getLocalHost();
      names.add(local.getHostName());
      names.add(local.getHostAddress());
      names.add(local.getCanonicalHostName());
    } catch (IOException e) {
      // unknown host name: compare by address only
    }
    try {
      for (java.net.NetworkInterface ni :
          java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
        for (java.net.InetAddress a : java.util.Collections.list(ni.getInetAddresses())) {
          String h = a.getHostAddress();
          int pct = h.indexOf('%');
          names.add(pct > 0 ? h.substring(0, pct) : h);
        }
      }
    } catch (IOException | RuntimeException e) {
      // keep what we have
    }
    return names;
  }

  /**
   * Removes group and other access from {@code dir} when it is inside {@code home}, exists, and
   * is owned by this user. Returns true when permissions were changed.
   */
  static boolean makePrivate(File dir, String home) {
    if (dir == null || home == null || !dir.isDirectory()) return false;
    try {
      Path p = dir.toPath().toRealPath();
      Path h = new File(home).toPath().toRealPath();
      if (!p.startsWith(h) || p.equals(h)) return false;
      if (!Files.getOwner(p).equals(Files.getOwner(h))) return false;
      Set<PosixFilePermission> perms = Files.getPosixFilePermissions(p);
      Set<PosixFilePermission> open =
          EnumSet.of(
              PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE,
              PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
              PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OTHERS_EXECUTE);
      if (perms.stream().noneMatch(open::contains)) return false;
      Files.setPosixFilePermissions(
          p,
          EnumSet.of(
              PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
              PosixFilePermission.OWNER_EXECUTE));
      System.out.println("Made " + p + " private to its owner (it holds the Geoweaver database or secrets).");
      return true;
    } catch (UnsupportedOperationException | IOException | SecurityException e) {
      return false; // not a POSIX file system (Windows) or not ours
    }
  }
}
