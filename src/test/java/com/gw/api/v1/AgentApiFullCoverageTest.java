package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import com.gw.tools.LocalhostTool;
import com.gw.utils.BaseTool;
import com.gw.utils.InternalRunCredential;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * GUI functions reachable through the Agent API (hosts, environments, remote-run validation,
 * history, step skipping, checkpoints, export/import, results) and the security rules around
 * them.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiFullCoverageTest {

  @TempDir static Path tempDir;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-localhost-runs", () -> "true");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.acknowledge-network-risk", () -> "true");
    registry.add(
        "geoweaver.agent.api-token-file", () -> tempDir.resolve(".agent_api_token").toString());
    registry.add("geoweaver.workspace", () -> tempDir.resolve("ws").toString());
    registry.add("geoweaver.login_required", () -> "true");
    registry.add("server.address", () -> "127.0.0.1");
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  @Autowired AgentApiTokenService tokenService;
  @Autowired ObjectMapper mapper;
  @Autowired LocalhostTool localhostTool;
  @Autowired BaseTool baseTool;

  String token;
  String base;

  @BeforeEach
  void setUp() throws Exception {
    Files.createDirectories(tempDir.resolve("ws").resolve("temp"));
    base = "http://127.0.0.1:" + port + "/Geoweaver/api/v1";
    token = tokenService.createOrRotate().token();
    GeoweaverApplication.addLocalhost();
  }

  private HttpHeaders authJson() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(token);
    return h;
  }

  private ResponseEntity<String> exchange(HttpMethod method, String path, Object body) {
    return rest.exchange(base + path, method, new HttpEntity<>(body, authJson()), String.class);
  }

  private JsonNode json(ResponseEntity<String> res) throws Exception {
    return mapper.readTree(res.getBody());
  }

  private JsonNode ok(ResponseEntity<String> res) throws Exception {
    assertThat(res.getStatusCode()).as(res.getBody()).isEqualTo(HttpStatus.OK);
    return json(res);
  }

  private void assertJsonError(ResponseEntity<String> res, HttpStatus status, String code)
      throws Exception {
    assertThat(res.getStatusCode()).as(res.getBody()).isEqualTo(status);
    assertThat(json(res).path("error").asText()).isEqualTo(code);
  }

  private String createProcess(String name, String lang, String code) throws Exception {
    return ok(exchange(HttpMethod.POST, "/processes", Map.of("name", name, "lang", lang, "code", code)))
        .get("id")
        .asText();
  }

  private JsonNode waitRun(String historyId) throws Exception {
    return ok(exchange(HttpMethod.GET, "/runs/" + historyId + "?waitSeconds=60&includeStepLogs=true", null));
  }

  @Test
  @DisplayName("hosts: list localhost, add/edit/delete an ssh host, validate fields, protect localhost")
  void hostLifecycle() throws Exception {
    JsonNode hosts = ok(exchange(HttpMethod.GET, "/hosts", null));
    assertThat(hosts.toString()).contains("\"id\":\"100001\"").contains("\"local\":true");

    assertJsonError(
        exchange(HttpMethod.POST, "/hosts", Map.of("name", "x", "ip", "a;rm -rf", "username", "u")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(
            HttpMethod.POST,
            "/hosts",
            Map.of("name", "x", "ip", "example.org", "port", "70000", "username", "u")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(HttpMethod.POST, "/hosts", Map.of("name", "x", "type", "jupyter", "url", "file:///etc")),
        HttpStatus.BAD_REQUEST,
        "bad_request");

    JsonNode created =
        ok(
            exchange(
                HttpMethod.POST,
                "/hosts",
                Map.of("name", "cluster-a", "ip", "gw-test.invalid", "username", "alice")));
    String id = created.get("id").asText();
    assertThat(created.get("port").asText()).isEqualTo("22");
    assertThat(created.get("local").asBoolean()).isFalse();
    assertThat(created.has("password")).isFalse();

    assertThat(ok(exchange(HttpMethod.GET, "/hosts?q=cluster-a", null)).size()).isEqualTo(1);
    JsonNode edited = ok(exchange(HttpMethod.PUT, "/hosts/" + id, Map.of("port", "2222")));
    assertThat(edited.get("port").asText()).isEqualTo("2222");
    assertThat(edited.get("username").asText()).isEqualTo("alice");
    assertThat(ok(exchange(HttpMethod.GET, "/hosts/" + id + "/environments", null)).isArray()).isTrue();

    assertJsonError(exchange(HttpMethod.DELETE, "/hosts/100001", null), HttpStatus.FORBIDDEN, "forbidden");
    assertThat(ok(exchange(HttpMethod.DELETE, "/hosts/" + id, null)).get("deleted").asBoolean()).isTrue();
    assertJsonError(exchange(HttpMethod.GET, "/hosts/" + id, null), HttpStatus.NOT_FOUND, "not_found");
  }

  @Test
  @DisplayName("remote runs need the SSH password, and a failure never echoes it")
  void remoteRunValidation() throws Exception {
    String hostId =
        ok(
                exchange(
                    HttpMethod.POST,
                    "/hosts",
                    Map.of("name", "unreachable", "ip", "gw-test.invalid", "username", "bob")))
            .get("id")
            .asText();
    createProcess("remote_helper", "python", "print('helper')");
    String pid = createProcess("remote-echo", "shell", "echo remote");

    ResponseEntity<String> noPw =
        exchange(HttpMethod.POST, "/processes/" + pid + "/runs", Map.of("hostId", hostId));
    assertJsonError(noPw, HttpStatus.BAD_REQUEST, "bad_request");
    assertThat(json(noPw).get("message").asText()).contains("hostPassword");

    String secret = "S3cret-" + System.nanoTime();
    ResponseEntity<String> run =
        exchange(
            HttpMethod.POST,
            "/processes/" + pid + "/runs",
            Map.of("hostId", hostId, "hostPassword", secret));
    assertThat(run.getBody()).doesNotContain(secret);
    if (run.getStatusCode() == HttpStatus.OK) {
      JsonNode done = waitRun(json(run).get("historyId").asText());
      assertThat(done.get("status").asText()).isIn("Failed", "Stopped");
      assertThat(done.toString()).doesNotContain(secret);
    } else {
      assertJsonError(run, HttpStatus.BAD_GATEWAY, "host_unreachable");
    }

    String wf =
        ok(
                exchange(
                    HttpMethod.POST,
                    "/workflows",
                    Map.of("name", "wf-hosts", "nodes", List.of(Map.of("id", pid + "-a")))))
            .get("id")
            .asText();
    assertJsonError(
        exchange(
            HttpMethod.POST,
            "/workflows/" + wf + "/runs",
            Map.of("nodeHosts", Map.of("nosuch-node", Map.of("hostId", hostId)))),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(
            HttpMethod.POST,
            "/workflows/" + wf + "/runs",
            Map.of("nodeHosts", Map.of(pid + "-a", Map.of("hostId", "nohost")))),
        HttpStatus.NOT_FOUND,
        "not_found");
    exchange(HttpMethod.DELETE, "/workflows/" + wf, null);
    exchange(HttpMethod.DELETE, "/hosts/" + hostId, null);
  }

  @Test
  @DisplayName("process names that would escape the script folder or the shell are refused")
  void unsafeProcessNames() throws Exception {
    for (String bad : List.of("../../x.py", "a b", "x;rm -rf ~", "", "$(id)")) {
      assertJsonError(
          exchange(HttpMethod.POST, "/processes", Map.of("name", bad, "lang", "python", "code", "print(1)")),
          HttpStatus.BAD_REQUEST,
          "bad_request");
    }
  }

  @Test
  @DisplayName("markup in names, notes, descriptions and node titles is refused (GUI renders HTML)")
  void markupRefused() throws Exception {
    String pid = createProcess("markup_check", "shell", "echo ok");
    assertJsonError(
        exchange(HttpMethod.POST, "/hosts", Map.of("name", "<img src=x onerror=alert(1)>", "ip", "h.invalid", "username", "u")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(HttpMethod.POST, "/workflows", Map.of("name", "x\"onmouseover=1", "nodes", List.of(Map.of("id", pid + "-1")))),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(
            HttpMethod.POST,
            "/workflows",
            Map.of("name", "ok", "nodes", List.of(Map.of("id", pid + "-1", "title", "<script>")))),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(HttpMethod.PUT, "/processes/" + pid, Map.of("description", "<b>x</b>")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    String hid =
        ok(exchange(HttpMethod.POST, "/processes/" + pid + "/runs", Map.of())).get("historyId").asText();
    waitRun(hid);
    assertJsonError(
        exchange(HttpMethod.PUT, "/runs/" + hid + "/notes", Map.of("notes", "<script>x</script>")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertThat(ok(exchange(HttpMethod.PUT, "/runs/" + hid + "/notes", Map.of("notes", "it's \"fine\" & ok"))).get("notes").asText())
        .isEqualTo("it's \"fine\" & ok");
  }

  @Test
  @DisplayName("builtin processes are validated JSON; q searches processes")
  void builtinAndSearch() throws Exception {
    assertJsonError(
        exchange(HttpMethod.POST, "/processes", Map.of("name", "b", "lang", "builtin", "code", "not json")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    String id =
        createProcess("search-me-builtin", "builtin", "{\"operation\":\"AWS S3\",\"params\":[]}");
    JsonNode found = ok(exchange(HttpMethod.GET, "/processes?q=SEARCH-ME", null));
    assertThat(found.toString()).contains(id);
    assertThat(ok(exchange(HttpMethod.GET, "/processes?q=zzz-no-match-zzz", null)).size()).isZero();
  }

  @Test
  @DisplayName("history: recent runs, status filter, notes, delete one, bulk delete")
  void runHistory() throws Exception {
    String pid = createProcess("hist-echo", "shell", "echo history");
    String hid =
        ok(exchange(HttpMethod.POST, "/processes/" + pid + "/runs", Map.of()))
            .get("historyId")
            .asText();
    assertThat(waitRun(hid).get("status").asText()).isEqualTo("Done");

    assertThat(ok(exchange(HttpMethod.GET, "/runs?limit=20", null)).toString()).contains(hid);
    assertThat(ok(exchange(HttpMethod.GET, "/runs?status=Done&limit=500", null)).toString())
        .contains(hid);
    assertJsonError(exchange(HttpMethod.GET, "/runs?status=Sleeping", null), HttpStatus.BAD_REQUEST, "bad_request");

    JsonNode noted = ok(exchange(HttpMethod.PUT, "/runs/" + hid + "/notes", Map.of("notes", "baseline")));
    assertThat(noted.get("notes").asText()).isEqualTo("baseline");

    assertJsonError(
        exchange(HttpMethod.DELETE, "/runs?status=Done", null), HttpStatus.BAD_REQUEST, "bad_request");
    assertThat(ok(exchange(HttpMethod.DELETE, "/runs?status=Failed&processId=" + pid, null)).get("deleted").asBoolean())
        .isTrue();
    assertThat(ok(exchange(HttpMethod.DELETE, "/runs/" + hid, null)).get("deleted").asBoolean()).isTrue();
    assertJsonError(exchange(HttpMethod.GET, "/runs/" + hid, null), HttpStatus.NOT_FOUND, "not_found");
  }

  @Test
  @DisplayName("workflow: skip a step, checkpoint per run, restore, export and import")
  void workflowTools() throws Exception {
    String a = createProcess("wt-a", "shell", "echo step-a");
    String b = createProcess("wt-b", "shell", "echo step-b");
    String wf =
        ok(
                exchange(
                    HttpMethod.POST,
                    "/workflows",
                    Map.of(
                        "name", "wt-flow",
                        "nodes", List.of(Map.of("id", a + "-1"), Map.of("id", b + "-2")),
                        "edges", List.of(Map.of("source", a + "-1", "target", b + "-2")))))
            .get("id")
            .asText();

    assertJsonError(
        exchange(HttpMethod.PUT, "/workflows/" + wf + "/steps/nope-9", Map.of("skip", true)),
        HttpStatus.NOT_FOUND,
        "not_found");
    ok(exchange(HttpMethod.PUT, "/workflows/" + wf + "/steps/" + b + "-2", Map.of("skip", true)));

    String run =
        ok(exchange(HttpMethod.POST, "/workflows/" + wf + "/runs", Map.of())).get("historyId").asText();
    JsonNode done = waitRun(run);
    assertThat(done.get("steps").get(0).get("status").asText()).isEqualTo("Done");
    assertThat(done.get("steps").get(1).get("status").asText()).isEqualTo("Skipped");

    JsonNode checkpoints = ok(exchange(HttpMethod.GET, "/workflows/" + wf + "/checkpoints", null));
    assertThat(checkpoints.toString()).contains(run);

    ok(
        exchange(
            HttpMethod.PUT,
            "/workflows/" + wf,
            Map.of("nodes", List.of(Map.of("id", a + "-1")), "edges", List.of())));
    JsonNode restored =
        ok(exchange(HttpMethod.POST, "/workflows/" + wf + "/restore", Map.of("runId", run)));
    assertThat(restored.get("nodes").asText()).contains(b + "-2");
    assertJsonError(
        exchange(HttpMethod.POST, "/workflows/" + wf + "/restore", Map.of("runId", "nope")),
        HttpStatus.NOT_FOUND,
        "not_found");

    JsonNode bundle = ok(exchange(HttpMethod.GET, "/workflows/" + wf + "/export", null));
    assertThat(bundle.get("format").asText()).isEqualTo("geoweaver.workflow.v1");
    assertThat(bundle.get("processes").size()).isEqualTo(2);
    assertThat(bundle.toString()).contains("echo step-a");

    JsonNode same = ok(exchange(HttpMethod.POST, "/workflows/import?replace=true", bundle));
    assertThat(same.get("processes").get("unchanged").size()).isEqualTo(2);

    ObjectNode changed = bundle.deepCopy();
    ((ObjectNode) changed.get("processes").get(0)).put("code", "echo changed");
    assertJsonError(exchange(HttpMethod.POST, "/workflows/import", changed), HttpStatus.CONFLICT, "conflict");
    assertThat(ok(exchange(HttpMethod.POST, "/workflows/import?replace=true", changed)).get("processes").get("updated").size())
        .isEqualTo(1);

    ObjectNode evil = bundle.deepCopy();
    ((ObjectNode) evil.get("processes").get(0)).put("id", "../../etc");
    assertJsonError(exchange(HttpMethod.POST, "/workflows/import", evil), HttpStatus.BAD_REQUEST, "bad_request");
    assertJsonError(
        exchange(HttpMethod.POST, "/workflows/import", Map.of("format", "zip")), HttpStatus.BAD_REQUEST, "bad_request");

    ok(exchange(HttpMethod.DELETE, "/workflows/" + wf, null));
    JsonNode reimported = ok(exchange(HttpMethod.POST, "/workflows/import", changed));
    assertThat(reimported.get("workflow").get("id").asText()).isEqualTo(wf);
  }

  @Test
  @DisplayName("results: list and download inside the results folder only")
  void results() throws Exception {
    Path root = Paths.get(baseTool.getResultsFolder());
    Files.createDirectories(root.resolve("sub"));
    Files.writeString(root.resolve("sub/out.txt"), "result-42");

    assertThat(ok(exchange(HttpMethod.GET, "/results", null)).toString()).contains("\"sub\"");
    JsonNode sub = ok(exchange(HttpMethod.GET, "/results?path=sub", null));
    assertThat(sub.get("entries").get(0).get("path").asText()).isEqualTo("sub/out.txt");

    ResponseEntity<String> file = exchange(HttpMethod.GET, "/results/file?path=sub/out.txt", null);
    assertThat(file.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(file.getBody()).isEqualTo("result-42");
    assertThat(file.getHeaders().getContentDisposition().isAttachment()).isTrue();

    assertJsonError(exchange(HttpMethod.GET, "/results?path=../", null), HttpStatus.BAD_REQUEST, "bad_request");
    assertJsonError(
        exchange(HttpMethod.GET, "/results/file?path=/etc/passwd", null), HttpStatus.BAD_REQUEST, "bad_request");
    assertJsonError(exchange(HttpMethod.GET, "/results/file?path=nope.txt", null), HttpStatus.NOT_FOUND, "not_found");

    Path outside = Files.writeString(tempDir.resolve("secret.txt"), "outside");
    Files.createSymbolicLink(root.resolve("link.txt"), outside);
    assertJsonError(exchange(HttpMethod.GET, "/results/file?path=link.txt", null), HttpStatus.FORBIDDEN, "forbidden");
  }

  @Test
  @DisplayName("security: empty localhost password is rejected; the in-process credential is not")
  void localhostPasswordNotBypassed() throws Exception {
    assertThatThrownBy(() -> localhostTool.authenticate("")).hasMessageContaining("Wrong Password");
    assertThatThrownBy(() -> localhostTool.authenticate(null)).isInstanceOf(Exception.class);
    localhostTool.authenticate(InternalRunCredential.get());
  }

  @Test
  @DisplayName("security: API CORS never allows credentials; GUI CORS is same-origin by default")
  void cors() {
    HttpHeaders pre = new HttpHeaders();
    pre.setOrigin("https://evil.example");
    pre.setAccessControlRequestMethod(HttpMethod.POST);
    pre.set("Access-Control-Request-Headers", "authorization,content-type");
    ResponseEntity<String> api =
        rest.exchange(base + "/processes", HttpMethod.OPTIONS, new HttpEntity<>(pre), String.class);
    assertThat(api.getHeaders().getAccessControlAllowOrigin()).isNotNull();
    assertThat(api.getHeaders().getAccessControlAllowCredentials()).isFalse();

    HttpHeaders get = new HttpHeaders();
    get.setOrigin("https://evil.example");
    ResponseEntity<String> gui =
        rest.exchange(
            "http://127.0.0.1:" + port + "/Geoweaver/web/localhost-login",
            HttpMethod.GET,
            new HttpEntity<>(get),
            String.class);
    assertThat(gui.getHeaders().getAccessControlAllowOrigin()).isNull();
  }

  @Test
  @DisplayName("security: cookie token refused cross-site; API responses are no-store + nosniff")
  void cookieAndHeaders() {
    HttpHeaders cross = new HttpHeaders();
    cross.add(HttpHeaders.COOKIE, AgentApiTokenFilter.AGENT_TOKEN_COOKIE + "=" + token);
    cross.add("Sec-Fetch-Site", "cross-site");
    assertThat(
            rest.exchange(base + "/processes", HttpMethod.GET, new HttpEntity<>(cross), String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);

    HttpHeaders foreignOrigin = new HttpHeaders();
    foreignOrigin.add(HttpHeaders.COOKIE, AgentApiTokenFilter.AGENT_TOKEN_COOKIE + "=" + token);
    foreignOrigin.setOrigin("https://evil.example");
    assertThat(
            rest.exchange(base + "/processes", HttpMethod.GET, new HttpEntity<>(foreignOrigin), String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);

    HttpHeaders cli = new HttpHeaders();
    cli.add(HttpHeaders.COOKIE, AgentApiTokenFilter.AGENT_TOKEN_COOKIE + "=" + token);
    ResponseEntity<String> fromCli =
        rest.exchange(base + "/capabilities", HttpMethod.GET, new HttpEntity<>(cli), String.class);
    assertThat(fromCli.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(fromCli.getHeaders().getCacheControl()).isEqualTo("no-store");
    assertThat(fromCli.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
  }
}
