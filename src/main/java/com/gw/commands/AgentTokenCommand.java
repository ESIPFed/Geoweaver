package com.gw.commands;

import com.gw.api.v1.AgentTokenBrowserReveal;
import com.gw.api.v1.dto.TokenMetadata;
import com.gw.api.v1.service.AgentApiTokenService;
import com.gw.utils.BeanTool;
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

  @Override
  public void run() {
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
    if (!create && !rotate) {
      System.out.println("Usage:");
      System.out.println("  gw agent-token --create [--ttl-days N]");
      System.out.println("  gw agent-token --rotate [--ttl-days N]   # issue + revoke others");
      System.out.println("  gw agent-token --list");
      System.out.println("  gw agent-token --revoke <fingerprint>");
      System.out.println();
      System.out.println("Tokens are stored in the Geoweaver database as SHA-256 hashes only.");
      System.out.println("The raw secret is shown once (browser copy page). Never written to a file.");
      System.out.println("If Geoweaver HTTP is already running, prefer:");
      System.out.println("  gw agent-token --create --base-url http://127.0.0.1:8070/Geoweaver");
      System.out.println("so the token is minted in the running server's database.");
      System.out.println("Offline jar CLI mint uses this process's database (same working directory).");
      System.out.println("Tokens always expire; maximum lifetime is 180 days (six months).");
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

      System.out.println("Enable in ~/geoweaver/application.properties or classpath props:");
      System.out.println("  geoweaver.agent.api-enabled=true");
      System.out.println("  geoweaver.agent.allow-localhost-runs=true   # token may run localhost without GUI password");
      System.out.println("  geoweaver.agent.token-ttl-days=30           # default lifetime");
      System.out.println("  geoweaver.agent.token-max-ttl-days=180      # hard max (six months)");
      System.out.println("  server.address=127.0.0.1                   # recommended");
      System.out.println();
      System.out.println("Remote mint (any machine) with GUI localhost password:");
      System.out.println("  POST {GEOWEAVER_BASE_URL}/api/v1/tokens  {\"hostPassword\":\"...\",\"ttlDays\":30}");
      System.out.println("  or: gw agent-token --create --base-url https://host/Geoweaver");
    } catch (Exception e) {
      System.err.println("Failed: " + e.getMessage());
      throw new RuntimeException(e);
    }
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
