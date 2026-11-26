package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SharedDatabaseCheckTest {

  @TempDir Path dir;

  private File lock(String server, String hostName) throws Exception {
    File f = dir.resolve("gw.lock.db").toFile();
    Files.writeString(
        f.toPath(), "method=FILE\nid=abc\nserver=" + server + "\nhostName=" + hostName + "\n");
    return f;
  }

  @Test
  void lockFileSitsNextToTheDatabase() {
    assertThat(SharedDatabaseCheck.lockFile(new File("/x/h2/gw.mv.db"))).isEqualTo(new File("/x/h2/gw.lock.db"));
  }

  @Test
  void noMessageWithoutALockOrWhenHeldHere() throws Exception {
    assertThat(SharedDatabaseCheck.describeHolder(dir.resolve("none.lock.db").toFile(), Set.of())).isNull();
    assertThat(SharedDatabaseCheck.describeHolder(lock("10.1.2.3:9092", "myhost"), Set.of("myhost"))).isNull();
    Files.writeString(dir.resolve("gw.lock.db"), "method=FILE\nid=abc\n"); // no server: plain lock
    assertThat(SharedDatabaseCheck.describeHolder(dir.resolve("gw.lock.db").toFile(), Set.of())).isNull();
  }

  @Test
  void noMessageWhenTheHolderCanBeReached() throws Exception {
    try (ServerSocket s = new ServerSocket(0)) {
      assertThat(SharedDatabaseCheck.describeHolder(lock("127.0.0.1:" + s.getLocalPort(), "ood-node7"), Set.of()))
          .isNull();
    }
  }

  @Test
  void explainsWhichMachineHoldsTheDatabaseWhenItCannotBeReached() throws Exception {
    // 192.0.2.1 is reserved for documentation: nothing answers there
    String msg = SharedDatabaseCheck.describeHolder(lock("192.0.2.1:9092", "ood-node7"), Set.of("login1"));
    assertThat(msg).contains("ood-node7 (192.0.2.1:9092)").contains("run this command on ood-node7");
  }

  @Test
  void aLoopbackAddressOnAnotherMachineIsNotThisMachine() throws Exception {
    int port;
    try (ServerSocket s = new ServerSocket(0)) {
      port = s.getLocalPort();
    }
    // H2 writes "localhost" for 127.0.0.1; it used to count as "held here"
    String msg = SharedDatabaseCheck.describeHolder(lock("localhost:" + port, "ood-vm"), Set.of("login1", "localhost"));
    assertThat(msg).contains("ood-vm").contains("GEOWEAVER_DB_BIND_ADDRESS");
    assertThat(SharedDatabaseCheck.describeHolder(lock("127.0.1.1:" + port, "ood-vm"), Set.of("login1"))).contains("loopback");
    // the same machine: no message
    assertThat(SharedDatabaseCheck.describeHolder(lock("127.0.1.1:" + port, "login1"), Set.of("login1"))).isNull();
    assertThat(SharedDatabaseCheck.describeHolder(lock("localhost:" + port, ""), Set.of())).isNull();
  }

  @Test
  void picksARegularNetworkAddressForSharing() {
    assertThat(SharedDatabaseCheck.pickAddress(java.util.List.of())).isNull();
    assertThat(
            SharedDatabaseCheck.pickAddress(
                java.util.List.of(
                    new String[] {"docker0", "172.17.0.1", "false"},
                    new String[] {"tun0", "10.8.0.5", "true"},
                    new String[] {"eth0", "10.1.2.3", "false"})))
        .isEqualTo("10.1.2.3");
    assertThat(SharedDatabaseCheck.pickAddress(java.util.List.<String[]>of(new String[] {"tun0", "10.8.0.5", "true"})))
        .isEqualTo("10.8.0.5");
  }

  @Test
  void makesFoldersInsideHomePrivate() throws Exception {
    Path home = dir.resolve("home");
    Path h2 = Files.createDirectories(home.resolve("h2"));
    Path outside = Files.createDirectories(dir.resolve("shared"));
    Files.setPosixFilePermissions(h2, PosixFilePermissions.fromString("rwxr-xr-x"));
    Files.setPosixFilePermissions(outside, PosixFilePermissions.fromString("rwxr-xr-x"));

    assertThat(SharedDatabaseCheck.makePrivate(h2.toFile(), home.toString())).isTrue();
    assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(h2))).isEqualTo("rwx------");
    assertThat(SharedDatabaseCheck.makePrivate(h2.toFile(), home.toString())).isFalse(); // already

    assertThat(SharedDatabaseCheck.makePrivate(outside.toFile(), home.toString())).isFalse();
    assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(outside))).isEqualTo("rwxr-xr-x");
    assertThat(SharedDatabaseCheck.makePrivate(home.toFile(), home.toString())).isFalse(); // never home
  }
}
