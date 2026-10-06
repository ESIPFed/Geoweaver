package com.gw.api.v1;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * One-time token copy page on the <strong>already running</strong> Geoweaver web app (default port
 * 8070). Never starts a temporary HTTP server.
 */
public final class AgentTokenBrowserReveal {

  public static final int DEFAULT_GEOWEAVER_PORT = 8070;
  private static final int TIMEOUT_SECONDS = 300;

  private AgentTokenBrowserReveal() {}

  /**
   * Open the reveal page on the running Geoweaver at {@code 127.0.0.1:8070}.
   *
   * @return true if the user acknowledged via POST …/done
   */
  public static boolean reveal(String token, String fingerprint, String expiresAt) {
    return reveal(token, fingerprint, expiresAt, DEFAULT_GEOWEAVER_PORT);
  }

  public static boolean reveal(
      String token, String fingerprint, String expiresAt, int geoweaverPort) {
    if (token == null || !token.startsWith("gwt_")) {
      return false;
    }
    int port = geoweaverPort > 0 ? geoweaverPort : DEFAULT_GEOWEAVER_PORT;
    Boolean viaServer = revealViaRunningGeoweaver(token, fingerprint, expiresAt, port);
    if (viaServer == null) {
      System.err.println(
          "Could not open the token copy page on http://127.0.0.1:"
              + port
              + "/Geoweaver — is Geoweaver already running?");
      System.err.println(
          "Start Geoweaver first, then re-run agent-token --create. "
              + "This command never starts a temporary server.");
      System.err.println("Headless: --no-browser or --show-token on a private screen.");
      return false;
    }
    return viaServer;
  }

  /**
   * @return true/false when session registered; null if Geoweaver is not available on that port
   */
  private static Boolean revealViaRunningGeoweaver(
      String token, String fingerprint, String expiresAt, int port) {
    try {
      String createUrl =
          "http://127.0.0.1:" + port + "/Geoweaver/api/v1/token-reveal/sessions";
      String json =
          "{\"token\":"
              + jsonString(token)
              + ",\"fingerprint\":"
              + jsonString(fingerprint == null ? "" : fingerprint)
              + ",\"expiresAt\":"
              + jsonString(expiresAt == null ? "" : expiresAt)
              + "}";
      HttpURLConnection conn = (HttpURLConnection) URI.create(createUrl).toURL().openConnection();
      conn.setConnectTimeout(1500);
      conn.setReadTimeout(5000);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      byte[] payload = json.getBytes(StandardCharsets.UTF_8);
      conn.setFixedLengthStreamingMode(payload.length);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(payload);
      }
      int code = conn.getResponseCode();
      InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
      String body = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
      conn.disconnect();
      if (code < 200 || code >= 300) {
        return null;
      }
      String revealUrl = extractJsonString(body, "url");
      String nonce = extractJsonString(body, "nonce");
      if (revealUrl == null || nonce == null) {
        return null;
      }
      announce(revealUrl);
      long deadline = System.currentTimeMillis() + TIMEOUT_SECONDS * 1000L;
      while (System.currentTimeMillis() < deadline) {
        String status = pollStatus(port, nonce);
        if ("acked".equals(status)) {
          return true;
        }
        if ("missing".equals(status)) {
          return false;
        }
        Thread.sleep(500);
      }
      return false;
    } catch (Exception e) {
      return null;
    }
  }

  private static String pollStatus(int port, String nonce) throws IOException {
    String statusUrl =
        "http://127.0.0.1:"
            + port
            + "/Geoweaver/api/v1/token-reveal/sessions/"
            + nonce
            + "/status";
    HttpURLConnection conn = (HttpURLConnection) URI.create(statusUrl).toURL().openConnection();
    conn.setConnectTimeout(1500);
    conn.setReadTimeout(3000);
    conn.setRequestMethod("GET");
    int code = conn.getResponseCode();
    InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
    String body = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
    conn.disconnect();
    if (code < 200 || code >= 300) {
      return "missing";
    }
    String status = extractJsonString(body, "status");
    return status == null ? "missing" : status;
  }

  private static void announce(String url) {
    boolean opened = openBrowser(url);
    if (!opened) {
      System.out.println("Open this URL in a browser to copy the token once:");
      System.out.println("  " + url);
    } else {
      System.out.println(
          "Opened the running Geoweaver token page so you can copy it into a password manager.");
      System.out.println("After copying, click \"I copied the token…\" and close the window.");
    }
  }

  private static boolean openBrowser(String url) {
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI.create(url));
        return true;
      }
    } catch (Exception ignored) {
      // fall through
    }
    String os = System.getProperty("os.name", "").toLowerCase();
    String[] cmd;
    if (os.contains("mac")) {
      cmd = new String[] {"open", url};
    } else if (os.contains("win")) {
      cmd = new String[] {"rundll32", "url.dll,FileProtocolHandler", url};
    } else {
      cmd = new String[] {"xdg-open", url};
    }
    try {
      new ProcessBuilder(cmd).start();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /** Shared HTML for the Spring {@code /agent-token-reveal/{nonce}} page. */
  public static String pageHtml(String token, String fingerprint, String expiresAt, String nonce) {
    String tokenJs = jsonString(token == null ? "" : token);
    String fp = escapeHtml(fingerprint == null ? "(unknown)" : fingerprint);
    String exp = escapeHtml(expiresAt == null ? "(see token file)" : expiresAt);
    return "<!DOCTYPE html>\n"
        + "<html lang=\"en\"><head><meta charset=\"utf-8\"/>"
        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
        + "<title>Geoweaver — copy Agent API token</title>\n"
        + "<style>\n"
        + ":root{--bg:#f4f7f5;--card:#fff;--ink:#1e2a24;--muted:#5a6b62;--accent:#2f6f4e;--warn:#8a4b16;--border:#d5e0d9;}\n"
        + "*{box-sizing:border-box}body{margin:0;min-height:100vh;font-family:Segoe UI,system-ui,sans-serif;"
        + "background:linear-gradient(160deg,#e8f0eb 0%,var(--bg) 45%,#eef3f0 100%);color:var(--ink);"
        + "display:flex;align-items:center;justify-content:center;padding:24px}\n"
        + "main{width:min(640px,100%);background:var(--card);border:1px solid var(--border);border-radius:12px;"
        + "padding:28px;box-shadow:0 12px 40px rgba(30,42,36,.08)}\n"
        + "h1{margin:0 0 6px;font-size:1.35rem}.brand{color:var(--accent);font-weight:700}\n"
        + "p{margin:0 0 12px;line-height:1.45;color:var(--muted);font-size:.95rem}\n"
        + ".warn{background:#fff7ed;border:1px solid #f0d2a8;color:var(--warn);border-radius:8px;padding:12px 14px;"
        + "margin:14px 0 18px;font-size:.9rem}\n"
        + ".meta{font-size:.85rem;margin-bottom:10px}label{display:block;font-size:.8rem;font-weight:600;margin-bottom:6px}\n"
        + ".row{display:flex;gap:8px}input#token{flex:1;font-family:ui-monospace,Menlo,Consolas,monospace;font-size:.85rem;"
        + "padding:12px;border:1px solid var(--border);border-radius:8px;background:#fafcfb}\n"
        + "button{border:0;border-radius:8px;cursor:pointer;font-weight:600;font-size:.9rem;padding:0 16px}\n"
        + "#copy{background:var(--accent);color:#fff}#done{width:100%;margin-top:16px;padding:12px 16px;background:var(--ink);color:#fff}\n"
        + "#done:disabled{opacity:.55;cursor:not-allowed}#status{margin-top:10px;font-size:.85rem;min-height:1.2em}.ok{color:var(--accent)}\n"
        + "</style></head><body><main>\n"
        + "<h1><span class=\"brand\">Geoweaver</span> Agent API token</h1>\n"
        + "<p>Copy this token into a password manager or other secure secret store. "
        + "Then click the button below and close this window.</p>\n"
        + "<div class=\"warn\">Do <strong>not</strong> save this token in an environment variable, shell profile, "
        + "chat, ticket, or screen share. Env vars and shell history are easy to leak.</div>\n"
        + "<div class=\"meta\">Fingerprint: <code>"
        + fp
        + "</code> · Expires: <code>"
        + exp
        + "</code></div>\n"
        + "<label for=\"token\">Bearer token (shown once)</label>\n"
        + "<div class=\"row\"><input id=\"token\" type=\"text\" readonly spellcheck=\"false\" autocomplete=\"off\"/>"
        + "<button type=\"button\" id=\"copy\">Copy</button></div>\n"
        + "<div id=\"status\"></div>\n"
        + "<button type=\"button\" id=\"done\" disabled>"
        + "I copied the token to a safe place — close this window</button>\n"
        + "</main><script>\n"
        + "(function(){var TOKEN="
        + tokenJs
        + ";var input=document.getElementById('token');var status=document.getElementById('status');"
        + "var copyBtn=document.getElementById('copy');var doneBtn=document.getElementById('done');"
        + "input.value=TOKEN;"
        + "function setStatus(m,ok){status.textContent=m;status.className=ok?'ok':'';}"
        + "copyBtn.onclick=function(){function en(){doneBtn.disabled=false;setStatus('Copied. Store it securely, then close this window.',true);}"
        + "if(navigator.clipboard&&navigator.clipboard.writeText){navigator.clipboard.writeText(TOKEN).then(en).catch(function(){input.select();try{document.execCommand('copy');en();}catch(e){setStatus('Copy manually (Ctrl/Cmd+C).',false);doneBtn.disabled=false;}});}"
        + "else{input.select();try{document.execCommand('copy');en();}catch(e){setStatus('Copy manually (Ctrl/Cmd+C).',false);doneBtn.disabled=false;}}};"
        + "doneBtn.onclick=function(){doneBtn.disabled=true;"
        + "var doneUrl=location.pathname.replace(/\\/?$/,'')+'/done';"
        + "fetch(doneUrl,{method:'POST'})"
        + ".then(function(){input.value='';TOKEN='';setStatus('You can close this window now.',true);try{window.close();}catch(e){}})"
        + ".catch(function(){setStatus('Acknowledged. You may close this window.',true);try{window.close();}catch(e){}});};"
        + "setTimeout(function(){doneBtn.disabled=false;},1500);})();\n"
        + "</script></body></html>";
  }

  private static String escapeHtml(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  static String jsonString(String s) {
    if (s == null) {
      s = "";
    }
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
        case '"':
          sb.append('\\').append(c);
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }

  static String extractJsonString(String json, String field) {
    if (json == null || field == null) {
      return null;
    }
    String needle = "\"" + field + "\"";
    int i = json.indexOf(needle);
    if (i < 0) {
      return null;
    }
    int colon = json.indexOf(':', i + needle.length());
    if (colon < 0) {
      return null;
    }
    int start = json.indexOf('"', colon + 1);
    if (start < 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (int p = start + 1; p < json.length(); p++) {
      char c = json.charAt(p);
      if (c == '\\' && p + 1 < json.length()) {
        sb.append(json.charAt(p + 1));
        p++;
        continue;
      }
      if (c == '"') {
        return sb.toString();
      }
      sb.append(c);
    }
    return null;
  }
}
