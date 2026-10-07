package com.gw.commands;

import com.gw.api.v1.AgentTokenBrowserReveal;
import com.gw.api.v1.AgentTokenRemoteMint;
import com.gw.api.v1.dto.TokenMetadata;
import com.gw.api.v1.service.AgentApiTokenService;
import com.gw.utils.BeanTool;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Issue, list, and revoke Agent API tokens stored in the Geoweaver database. */
@Component
@Command(
    name = "agent-token",
    description = "Manage Geoweaver Agent API tokens (Bearer auth for /api/v1)")
public class AgentTokenCommand implements Runnable {

  @Option(
      names = {"--create"},
      description = "Issue a new token (does not revoke existing tokens)")
  boolean create;

  @Option(
      names = {"--rotate"},
      description = "Issue a new token and revoke every other active token")
  boolean rotate;

  @Option(
      names = {"--ttl-days"},
      description = "Token lifetime in days (1..180 / six months). Default from geoweaver.agent.token-ttl-days.")
  Integer ttlDays;

  @Option(
      names = {"--list"},
      description = "List token fingerprints, expiry, and revoke status (no secrets)")
  boolean list;

  @Option(
      names = {"--revoke"},
      description = "Revoke a token by fingerprint")
  String revokeFingerprint;

  @Option(
      names = {"--show-token"},
      description =
          "Also print the raw token in the terminal (insecure in shared terminals / screen share). "
              + "Default is browser copy page + fingerprint.")
  boolean showToken;

  @Option(
      names = {"--no-browser"},
      description = "Do not open the local Geoweaver browser page to copy the token.")
  boolean noBrowser;

  @Option(
      names = {"--base-url"},
      description =
          "Running Geoweaver URL including /Geoweaver (this computer or another). "
              + "Mints with POST /api/v1/tokens into that server's database. "
              + "Requires the GUI localhost password (--host-password or prompt).")
  String baseUrl;

  @Option(
      names = {"--host-password"},
      description =
          "GUI localhost password for --base-url. Prefer an interactive prompt: this flag can show "
              + "up in process lists. Never pass the Bearer token here.")
  String hostPassword;

  @Override
  public void run() {
    if (!create && !rotate && !list && (revokeFingerprint == null || revokeFingerprint.isBlank())) {
      printUsage();
      return;
    }

    if (baseUrl != null && !baseUrl.isBlank()) {
      runRemoteHttp();
      return;
    }

    AgentApiTokenService svc = BeanTool.getBean(AgentApiTokenService.class);
    if (svc == null) {
      System.err.println(
          "Failed: AgentApiTokenService is not available in this Geoweaver build. "
              + "Rebuild from a tree that includes com.gw.api.v1 in GeoweaverCLI ComponentScan.");
      throw new IllegalStateException("AgentApiTokenService bean missing");
    }

    if (list) {
      printList(svc);
      return;
    }
    if (revokeFingerprint != null && !revokeFingerprint.isBlank()) {
      boolean ok = svc.revokeByFingerprint(revokeFingerprint.trim());
      if (!ok) {
        System.err.println("Unknown fingerprint: " + revokeFingerprint.trim());
        throw new IllegalStateException("unknown fingerprint");
      }
      System.out.println("Revoked fingerprint: " + revokeFingerprint.trim());
      return;
    }
    try {
      AgentApiTokenService.IssuedToken issued = svc.issue(ttlDays, rotate);
      String fingerprint = issued.fingerprint();
      System.out.println("Stored in: Geoweaver database (hash only; no token file)");
      System.out.println("Fingerprint: " + fingerprint);
      System.out.println("Expires at: " + issued.expiresAt() + " (ttlDays=" + issued.ttlDays() + ")");
      if (rotate) {
        System.out.println("Other active tokens were revoked.");
      }
      System.out.println();

      boolean browserAcked = false;
      if (!noBrowser) {
        browserAcked =
            AgentTokenBrowserReveal.reveal(
                issued.token(), fingerprint, String.valueOf(issued.expiresAt()));
        if (browserAcked) {
          System.out.println(
              "Browser window acknowledged. Keep the token only in a password manager "
                  + "or private secret store — not in environment variables or a file.");
        } else {
          System.out.println(
              "Browser copy step finished or timed out. If you did not copy the token, "
                  + "re-run without --no-browser, or use --show-token once on a private screen.");
        }
        System.out.println();
      }

      if (showToken) {
        System.out.println("SECRET (shown once; do not paste into chat or share screen):");
        System.out.println(issued.token());
        System.out.println(
            "Store this in a password manager or secret store — not in a file or environment variable.");
        System.out.println();
      } else if (noBrowser) {
        System.out.println("Raw token not printed to the terminal (safer default).");
        System.out.println(
            "Re-run without --no-browser to open the Geoweaver copy page, "
                + "or use --show-token once on a private screen.");
        System.out.println();
      }

      printEnableHints();
    } catch (Exception e) {
      System.err.println("Failed: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void runRemoteHttp() {
    if (list || (revokeFingerprint != null && !revokeFingerprint.isBlank())) {
      System.err.println(
          "--list and --revoke over HTTP are not in this CLI path. "
              + "Use Bearer GET/DELETE {base-url}/api/v1/tokens after you have a token.");
      throw new IllegalStateException("base-url list/revoke not supported");
    }
    if (!create && !rotate) {
      printUsage();
      return;
    }
    String password = hostPassword;
    if (password == null || password.isBlank()) {
      password = promptHostPassword();
    }
    try {
      AgentTokenRemoteMint.Result issued =
          AgentTokenRemoteMint.createToken(baseUrl, password, ttlDays, rotate);
      System.out.println("Stored in: remote Geoweaver database at " + AgentTokenRemoteMint.normalizeBaseUrl(baseUrl));
      System.out.println("Fingerprint: " + issued.fingerprint());
      System.out.println("Expires at: " + issued.expiresAt() + " (ttlDays=" + issued.ttlDays() + ")");
      if (rotate) {
        System.out.println("Other active tokens were revoked on that server.");
      }
      System.out.println();
      System.out.println(
          "Token copy web page is loopback-only on the server. On this machine the secret is shown once.");
      System.out.println("SECRET (shown once; do not paste into chat or share screen):");
      System.out.println(issued.token());
      System.out.println(
          "Store this in a password manager or secret store — not in a file or environment variable.");
      System.out.println();
      printEnableHints();
    } catch (Exception e) {
      System.err.println("Failed: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  static String promptHostPassword() {
    Console console = System.console();
    if (console != null) {
      char[] chars = console.readPassword("GUI localhost password (server): ");
      if (chars == null || chars.length == 0) {
        throw new IllegalStateException("host password is required for --base-url");
      }
      return new String(chars);
    }
    System.out.print("GUI localhost password (server, echo may be visible): ");
    System.out.flush();
    try {
      String line = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
      if (line == null || line.isBlank()) {
        throw new IllegalStateException(
            "host password is required for --base-url (pass --host-password in non-interactive use)");
      }
      return line;
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(
          "Could not read password. Re-run with --host-password on a private terminal.", e);
    }
  }

  static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  gw agent-token --create [--ttl-days N]");
    System.out.println("  gw agent-token --rotate [--ttl-days N]   # issue + revoke others");
    System.out.println("  gw agent-token --list");
    System.out.println("  gw agent-token --revoke <fingerprint>");
    System.out.println();
    System.out.println("Another computer (or this host's already-running HTTP):");
    System.out.println("  gw agent-token --create --base-url http://HOST:8070/Geoweaver");
    System.out.println("  Prompts for the GUI localhost password; stores the hash on that server.");
    System.out.println();
    System.out.println("Tokens are stored in the Geoweaver database as SHA-256 hashes only.");
    System.out.println("The raw secret is shown once. Never written to a file.");
    System.out.println("Offline jar CLI mint (no --base-url) uses this process's database.");
    System.out.println("Tokens always expire; maximum lifetime is 180 days (six months).");
  }

  static void printEnableHints() {
    System.out.println("Enable in ~/geoweaver/application.properties or classpath props:");
    System.out.println("  geoweaver.agent.api-enabled=true");
    System.out.println("  geoweaver.agent.allow-localhost-runs=true   # token may run localhost without GUI password");
    System.out.println("  geoweaver.agent.token-ttl-days=30           # default lifetime");
    System.out.println("  geoweaver.agent.token-max-ttl-days=180      # hard max (six months)");
    System.out.println();
    System.out.println("This computer only (other machines cannot connect to the JVM):");
    System.out.println("  server.address=127.0.0.1");
    System.out.println();
    System.out.println("Caller on another computer — pick one:");
    System.out.println("  A) HTTPS reverse proxy → 127.0.0.1:8070  (preferred)");
    System.out.println("  B) JVM listens on all interfaces AND geoweaver.agent.allow-non-loopback=true");
    System.out.println("     (do not set server.address=127.0.0.1)");
  }

  private static void printList(AgentApiTokenService svc) {
    java.util.List<TokenMetadata> rows = svc.listMetadata();
    if (rows.isEmpty()) {
      System.out.println("No Agent API tokens in the database.");
      return;
    }
    System.out.println("fingerprint\texpiresAt\trevokedAt\tttlDays");
    for (TokenMetadata row : rows) {
      String revoked = row.revokedAt() == null ? "-" : row.revokedAt();
      System.out.println(
          row.fingerprint() + "\t" + row.expiresAt() + "\t" + revoked + "\t" + row.ttlDays());
    }
  }
}
