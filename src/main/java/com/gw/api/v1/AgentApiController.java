package com.gw.api.v1;

import com.gw.api.v1.dto.AgentErrorResponse;
import com.gw.api.v1.dto.ProcessCreateRequest;
import com.gw.api.v1.dto.ProcessResponse;
import com.gw.api.v1.dto.ProcessSummary;
import com.gw.api.v1.dto.ProcessUpdateRequest;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStartRequest;
import com.gw.api.v1.dto.RunStartResponse;
import com.gw.api.v1.dto.TokenCreateRequest;
import com.gw.api.v1.dto.TokenCreateResponse;
import com.gw.api.v1.dto.TokenMetadata;
import com.gw.api.v1.dto.WorkflowCreateRequest;
import com.gw.api.v1.dto.WorkflowResponse;
import com.gw.api.v1.dto.WorkflowRunRequest;
import com.gw.api.v1.dto.WorkflowSummary;
import com.gw.api.v1.dto.WorkflowUpdateRequest;
import com.gw.api.v1.service.AgentApiTokenService;
import com.gw.api.v1.service.AgentProcessService;
import com.gw.api.v1.service.AgentRunSupport;
import com.gw.api.v1.service.AgentTokenBootstrapService;
import com.gw.api.v1.service.AgentTokenRevealSessionStore;
import com.gw.api.v1.service.AgentWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thin JSON API so coding agents can use Geoweaver's existing Python/Shell and workflow runners
 * without the HTML UI. Not a general automation platform.
 */
@RestController
@RequestMapping("/api/v1")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Tag(name = "Agent API", description = "JSON API for coding agents (Cursor/Codex/Claude Code)")
public class AgentApiController {

  private static final Logger logger = LoggerFactory.getLogger(AgentApiController.class);

  private final AgentApiProperties properties;
  private final AgentApiTokenService tokenService;
  private final AgentProcessService processService;
  private final AgentWorkflowService workflowService;
  private final AgentTokenBootstrapService tokenBootstrapService;
  private final AgentTokenRevealSessionStore revealSessionStore;

  public AgentApiController(
      AgentApiProperties properties,
      AgentApiTokenService tokenService,
      AgentProcessService processService,
      AgentWorkflowService workflowService,
      AgentTokenBootstrapService tokenBootstrapService,
      AgentTokenRevealSessionStore revealSessionStore) {
    this.properties = properties;
    this.tokenService = tokenService;
    this.processService = processService;
    this.workflowService = workflowService;
    this.tokenBootstrapService = tokenBootstrapService;
    this.revealSessionStore = revealSessionStore;
  }

  @GetMapping("/health")
  @Operation(summary = "Public liveness (no secrets)")
  public Map<String, String> health() {
    return Map.of("status", "up");
  }

  @GetMapping("/capabilities")
  @Operation(summary = "What this Agent API exposes")
  public Map<String, Object> capabilities() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("apiVersion", "v1");
    body.put("apiEnabled", properties.isApiEnabled());
    body.put("allowLocalhostRuns", properties.isAllowLocalhostRuns());
    body.put("allowDelete", properties.isAllowDelete());
    body.put("executionHost", AgentRunSupport.LOCALHOST_ID);
    body.put(
        "executionHostMeaning",
        "hostId 100001 is the machine where Geoweaver is running (server local disk/CPU), "
            + "not the coding agent's laptop. Works the same for public HTTPS base URLs.");
    body.put("processLanguages", List.of("python", "shell"));
    body.put(
        "operations",
        List.of(
            "listProcesses",
            "createProcess",
            "getProcess",
            "updateProcess",
            "deleteProcess",
            "startProcessRun",
            "listWorkflows",
            "createWorkflow",
            "getWorkflow",
            "updateWorkflow",
            "deleteWorkflow",
            "startWorkflowRun",
            "listWorkflowRuns",
            "getRun",
            "listProcessRuns",
            "stopRun",
            "createTokenWithLocalhostPassword",
            "listTokens",
            "revokeToken"));
    body.put("terminalStatuses", List.of("Done", "Failed", "Stopped", "Skipped"));
    body.put(
        "statuses",
        List.of("Done", "Failed", "Running", "Unknown", "Stopped", "Ready", "Skipped"));
    body.put("auth", "Authorization: Bearer <token>");
    body.put("openApi", "/v3/api-docs/agent-api");
    body.put(
        "workflowGraph",
        Map.of(
            "nodes",
            "JSON array (or JSON string) of {\"id\":\"{processId}-{suffix}\"}; title/x/y optional",
            "edges",
            "JSON array of {\"source\":\"nodeId\",\"target\":\"nodeId\"} (GUI object form also"
                + " accepted)",
            "semantics",
            "A step starts after all upstream steps finish, even if one Failed. Check run.steps."));
    body.put(
        "waitForRun",
        "GET /api/v1/runs/{historyId}?waitSeconds=1.."
            + AgentRunSupport.MAX_WAIT_SECONDS
            + " blocks until terminal; add includeStepLogs=true for per-step logs of workflow runs");
    body.put("errors", "Every error is JSON {\"error\":code,\"message\":text}; never an HTML redirect");
    body.put(
        "remoteClients",
        "Bearer works from another computer if this HTTP port is reachable. "
            + "hostId 100001 is still this Geoweaver server, not the caller. "
            + "The one-time token copy web page stays on 127.0.0.1 only.");
    body.put("tokenRevealLoopbackOnly", true);
    body.put(
        "tokenCreate",
        "POST /api/v1/tokens with JSON hostPassword (GUI localhost password) and optional "
            + "ttlDays (1..180 / six months); no Bearer; rate-limited per IP; requires api-enabled=true");
    body.put("tokenTtlDaysDefault", properties.getTokenTtlDays());
    body.put("tokenMaxTtlDays", Math.min(180, properties.getTokenMaxTtlDays()));
    body.put(
        "tokenExpiry",
        "Every Bearer token expires; maximum lifetime is 180 days (six months). "
            + "Raw secrets are shown once at mint; the database stores a SHA-256 hash only.");
    body.put("tokenStoredInDatabase", tokenService.hasActiveToken());
    body.put(
        "note",
        "Python/Shell and workflows on the Geoweaver host only. Set GEOWEAVER_BASE_URL to this "
            + "server's public or private URL (including /Geoweaver). Remote SSH hosts are not exposed.");
    return body;
  }

  @PostMapping("/tokens")
  @Operation(
      summary =
          "Issue a new Agent API Bearer token using GUI localhost password (no Bearer required). "
              + "Optional JSON revokeOthers=true revokes other active tokens.")
  public ResponseEntity<TokenCreateResponse> createToken(
      @RequestBody(required = false) TokenCreateRequest body, HttpServletRequest request) {
    String clientIp = clientIp(request);
    TokenCreateResponse created = tokenBootstrapService.createWithLocalhostPassword(body, clientIp);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/tokens")
  @Operation(summary = "List Agent API token metadata (no secrets)")
  public List<TokenMetadata> listTokens() {
    return tokenService.listMetadata();
  }

  @DeleteMapping("/tokens/{fingerprint}")
  @Operation(summary = "Revoke an Agent API token by fingerprint")
  public Map<String, Object> revokeToken(@PathVariable("fingerprint") String fingerprint) {
    boolean revoked = tokenService.revokeByFingerprint(fingerprint);
    if (!revoked) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown token fingerprint");
    }
    return Map.of("fingerprint", fingerprint, "revoked", true);
  }

  @PostMapping("/token-reveal/sessions")
  @Operation(
      summary =
          "Loopback-only: create a one-time browser reveal session on this Geoweaver (port 8070)")
  public Map<String, Object> createTokenRevealSession(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    requireLoopback(request);
    String token = body == null ? null : stringField(body, "token");
    String fingerprint = body == null ? null : stringField(body, "fingerprint");
    String expiresAt = body == null ? null : stringField(body, "expiresAt");
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token is required");
    }
    if (fingerprint == null || fingerprint.isBlank()) {
      fingerprint = tokenService.fingerprint(token);
    }
    String nonce = revealSessionStore.create(token, fingerprint, expiresAt);
    int port = properties.getServerPort() > 0 ? properties.getServerPort() : 8070;
    String url = "http://127.0.0.1:" + port + "/Geoweaver/agent-token-reveal/" + nonce;
    Map<String, Object> resp = new LinkedHashMap<>();
    resp.put("nonce", nonce);
    resp.put("url", url);
    return resp;
  }

  @GetMapping("/token-reveal/sessions/{nonce}/status")
  @Operation(summary = "Loopback-only: pending|acked|missing for a reveal session")
  public Map<String, String> tokenRevealStatus(
      @PathVariable("nonce") String nonce, HttpServletRequest request) {
    requireLoopback(request);
    if (revealSessionStore.isAcked(nonce)) {
      return Map.of("status", "acked");
    }
    if (revealSessionStore.get(nonce) != null) {
      return Map.of("status", "pending");
    }
    return Map.of("status", "missing");
  }

  static void requireLoopback(HttpServletRequest request) {
    String addr = request.getRemoteAddr();
    if (addr == null
        || !(addr.equals("127.0.0.1")
            || addr.equals("::1")
            || addr.equals("https://example.net/id/garnet")
            || addr.equals("localhost"))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Token reveal sessions are loopback-only");
    }
  }

  static String stringField(Map<String, Object> body, String key) {
    Object v = body.get(key);
    return v == null ? null : String.valueOf(v);
  }

  @GetMapping("/processes")
  public List<ProcessSummary> processes() {
    return processService.listProcesses();
  }

  @PostMapping("/processes")
  public ProcessResponse createProcess(@RequestBody ProcessCreateRequest body) {
    return processService.createProcess(body);
  }

  @GetMapping("/processes/{id}")
  public ProcessResponse getProcess(@PathVariable("id") String id) {
    return processService.getProcess(id);
  }

  @PutMapping("/processes/{id}")
  public ProcessResponse updateProcess(
      @PathVariable("id") String id, @RequestBody ProcessUpdateRequest body) {
    return processService.updateProcess(id, body);
  }

  @PostMapping("/processes/{id}/runs")
  public RunStartResponse startProcessRun(
      @PathVariable("id") String id, @RequestBody(required = false) RunStartRequest body) {
    return processService.startRun(
        id, body == null ? new RunStartRequest(null, null, null, null) : body);
  }

  @DeleteMapping("/processes/{id}")
  @Operation(summary = "Delete a process (409 while a workflow uses it)")
  public Map<String, Object> deleteProcess(@PathVariable("id") String id) {
    requireDeleteAllowed();
    return processService.deleteProcess(id);
  }

  @GetMapping("/processes/{id}/runs")
  public List<RunResponse> listProcessRuns(
      @PathVariable("id") String id,
      @RequestParam(value = "logLimit", required = false) Integer logLimit) {
    return processService.listRunsForProcess(id, logLimit);
  }

  @GetMapping("/workflows")
  public List<WorkflowSummary> workflows() {
    return workflowService.listWorkflows();
  }

  @PostMapping("/workflows")
  public WorkflowResponse createWorkflow(@RequestBody WorkflowCreateRequest body) {
    return workflowService.createWorkflow(body);
  }

  @GetMapping("/workflows/{id}")
  public WorkflowResponse getWorkflow(@PathVariable("id") String id) {
    return workflowService.getWorkflow(id);
  }

  @PutMapping("/workflows/{id}")
  public WorkflowResponse updateWorkflow(
      @PathVariable("id") String id, @RequestBody WorkflowUpdateRequest body) {
    return workflowService.updateWorkflow(id, body);
  }

  @DeleteMapping("/workflows/{id}")
  @Operation(summary = "Delete a workflow (processes and run history are kept)")
  public Map<String, Object> deleteWorkflow(@PathVariable("id") String id) {
    requireDeleteAllowed();
    return workflowService.deleteWorkflow(id);
  }

  private void requireDeleteAllowed() {
    if (!properties.isAllowDelete()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Agent deletes are disabled (geoweaver.agent.allow-delete=false)");
    }
  }

  @GetMapping("/workflows/{id}/runs")
  @Operation(summary = "Workflow run history, newest first (max 50), with per-step status")
  public List<RunResponse> listWorkflowRuns(
      @PathVariable("id") String id,
      @RequestParam(value = "logLimit", required = false) Integer logLimit) {
    return workflowService.listRuns(id, logLimit);
  }

  @PostMapping("/workflows/{id}/runs")
  public RunStartResponse startWorkflowRun(
      @PathVariable("id") String id, @RequestBody(required = false) WorkflowRunRequest body) {
    return workflowService.startRun(
        id, body == null ? new WorkflowRunRequest(null, null, null) : body);
  }

  @GetMapping("/runs/{historyId}")
  @Operation(
      summary =
          "Run detail + log. waitSeconds (0..60) blocks until terminal; workflow runs include steps")
  public RunResponse getRun(
      @PathVariable("historyId") String historyId,
      @RequestParam(value = "logLimit", required = false) Integer logLimit,
      @RequestParam(value = "waitSeconds", required = false, defaultValue = "0") int waitSeconds,
      @RequestParam(value = "includeStepLogs", required = false, defaultValue = "false")
          boolean includeStepLogs) {
    return processService.getRun(historyId, logLimit, waitSeconds, includeStepLogs);
  }

  @PostMapping("/runs/{historyId}/stop")
  public RunResponse stopRun(@PathVariable("historyId") String historyId) {
    return processService.stopRun(historyId);
  }

  static String clientIp(HttpServletRequest request) {
    // Prefer direct remote addr. Do not trust X-Forwarded-For for rate-limit keys unless the
    // reverse proxy is known to strip client-supplied values (operators may tighten later).
    String addr = request.getRemoteAddr();
    return addr == null ? "unknown" : addr;
  }

  /** Unknown path or method under /api/v1: JSON 404 instead of the GUI error page. */
  @RequestMapping("/**")
  public ResponseEntity<AgentErrorResponse> unknownEndpoint(HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new AgentErrorResponse(
                "not_found",
                "No Agent API endpoint for "
                    + request.getMethod()
                    + " "
                    + AgentApiTokenFilter.normalizedPath(request)
                    + ". See GET /api/v1/capabilities or /v3/api-docs/agent-api"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<AgentErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getMostSpecificCause();
    String detail = cause == null || cause.getMessage() == null ? "" : cause.getMessage();
    int nl = detail.indexOf('\n');
    if (nl > 0) {
      detail = detail.substring(0, nl);
    }
    return ResponseEntity.badRequest()
        .body(new AgentErrorResponse("bad_request", "Request body is not valid JSON for this endpoint: " + detail));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<AgentErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(new AgentErrorResponse("unsupported_media_type", "Send Content-Type: application/json"));
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<AgentErrorResponse> handleBadParam(Exception ex) {
    return ResponseEntity.badRequest().body(new AgentErrorResponse("bad_request", ex.getMessage()));
  }

  /** Last resort: log server-side, never leak stack traces or paths to the caller. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<AgentErrorResponse> handleUnexpected(Exception ex) {
    logger.error("Agent API unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new AgentErrorResponse("internal_error", "Unexpected server error; see Geoweaver log"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<AgentErrorResponse> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String msg = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
    String code =
        status == HttpStatus.UNAUTHORIZED
            ? "unauthorized"
            : status == HttpStatus.FORBIDDEN
                ? "forbidden"
                : status == HttpStatus.NOT_FOUND
                    ? "not_found"
                    : status == HttpStatus.TOO_MANY_REQUESTS
                        ? "rate_limited"
                        : status == HttpStatus.BAD_REQUEST
                            ? "bad_request"
                            : status == HttpStatus.CONFLICT ? "conflict" : "error";
    return ResponseEntity.status(status).body(new AgentErrorResponse(code, msg));
  }
}
