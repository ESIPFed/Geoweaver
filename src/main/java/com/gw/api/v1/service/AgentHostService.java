package com.gw.api.v1.service;

import com.gw.GeoweaverApplication;
import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.dto.EnvironmentResponse;
import com.gw.api.v1.dto.HostRequest;
import com.gw.api.v1.dto.HostResponse;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HostRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.Host;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HostTool;
import com.gw.utils.BaseTool;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Agent API host surface: list/add/edit/delete hosts and their Python environments. */
@Service
public class AgentHostService {

  private static final Logger logger = LoggerFactory.getLogger(AgentHostService.class);
  static final Set<String> HOST_TYPES = Set.of("ssh", "jupyter", "jupyterhub", "jupyterlab", "gee");
  private static final Pattern HOSTNAME = Pattern.compile("^[A-Za-z0-9._:\\[\\]-]{1,253}$");
  private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._@-]{1,64}$");

  private final HostRepository hostRepository;
  private final EnvironmentRepository environmentRepository;
  private final HostTool hostTool;
  private final ExecutionTool executionTool;
  private final AgentRunSupport runSupport;
  private final AgentApiProperties properties;
  private final WorkspaceEventBroadcaster workspaceEvents;
  private final AgentRunRateLimiter rateLimiter;

  public AgentHostService(
      HostRepository hostRepository,
      EnvironmentRepository environmentRepository,
      HostTool hostTool,
      ExecutionTool executionTool,
      AgentRunSupport runSupport,
      AgentApiProperties properties,
      WorkspaceEventBroadcaster workspaceEvents,
      AgentRunRateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
    this.hostRepository = hostRepository;
    this.environmentRepository = environmentRepository;
    this.hostTool = hostTool;
    this.executionTool = executionTool;
    this.runSupport = runSupport;
    this.properties = properties;
    this.workspaceEvents = workspaceEvents;
  }

  /** @param query optional case-insensitive filter on id, name, ip and url */
  public List<HostResponse> listHosts(String query) {
    GeoweaverApplication.addLocalhost();
    List<HostResponse> out = new ArrayList<>();
    for (Host h : hostRepository.findAll()) {
      if (AgentSearch.matches(query, h.getId(), h.getName(), h.getIp(), h.getUrl())) {
        out.add(toResponse(h));
      }
    }
    return out;
  }

  public HostResponse getHost(String id) {
    return toResponse(requireHost(id));
  }

  public HostResponse createHost(HostRequest req) {
    requireHostWrites();
    if (req == null) {
      throw badRequest("body required");
    }
    String type = normalizeType(req.type());
    String name = requireName(req.name());
    String[] conn = validateConnection(type, req.ip(), req.port(), req.username(), req.url());
    String id = hostTool.add(name, conn[0], conn[1], conn[2], conn[3], type, "111111", "FALSE");
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info("Agent API created host id={} type={} fingerprint={}", id, type, fp);
    workspaceEvents.publishAgent("host.created", "host", id, name, null, fp);
    return toResponse(requireHost(id));
  }

  public HostResponse updateHost(String id, HostRequest req) {
    requireHostWrites();
    Host h = requireEditableHost(id);
    if (req == null) {
      throw badRequest("body required");
    }
    String type = BaseTool.isNull(req.type()) ? normalizeType(h.getType()) : normalizeType(req.type());
    String name = BaseTool.isNull(req.name()) ? h.getName() : requireName(req.name());
    String[] conn =
        validateConnection(
            type,
            BaseTool.isNull(req.ip()) ? h.getIp() : req.ip(),
            BaseTool.isNull(req.port()) ? h.getPort() : req.port(),
            BaseTool.isNull(req.username()) ? h.getUsername() : req.username(),
            BaseTool.isNull(req.url()) ? h.getUrl() : req.url());
    h.setName(name);
    h.setType(type);
    h.setIp(conn[0]);
    h.setPort(conn[1]);
    h.setUsername(conn[2]);
    h.setUrl(conn[3]);
    hostTool.save(h);
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info("Agent API updated host id={} fingerprint={}", id, fp);
    workspaceEvents.publishAgent("host.updated", "host", id, name, null, fp);
    return toResponse(requireHost(id));
  }

  /** Deletes a host and its saved environments; run history is kept. */
  public Map<String, Object> deleteHost(String id) {
    requireHostWrites();
    Host h = requireEditableHost(id);
    try {
      environmentRepository.deleteAll(environmentRepository.findEnvByHost(id));
      hostTool.del(id);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Host " + id + " is still referenced and cannot be deleted");
    }
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info("Agent API deleted host id={} fingerprint={}", id, fp);
    workspaceEvents.publishAgent("host.deleted", "host", id, h.getName(), null, fp);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", id);
    out.put("deleted", true);
    return out;
  }

  public List<EnvironmentResponse> listEnvironments(String hostId) {
    requireHost(hostId);
    List<EnvironmentResponse> out = new ArrayList<>();
    for (Environment e : environmentRepository.findEnvByHost(hostId)) {
      out.add(
          new EnvironmentResponse(
              e.getId(), e.getName(), e.getType(), e.getBin(), e.getPyenv(), e.getBasedir(), hostId));
    }
    return out;
  }

  /**
   * Finds Python interpreters/conda envs on the host (SSH for remote hosts) and saves them, like the
   * GUI "read environments" button. Returns the saved list.
   */
  public List<EnvironmentResponse> scanEnvironments(String hostId, String hostPassword) {
    requireHost(hostId);
    if (!rateLimiter.tryAcquire(AgentApiAuthContext.getTokenFingerprint())) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Run rate limit exceeded for this token");
    }
    AgentRunSupport.RunTarget target = runSupport.resolveRunTarget(hostId, hostPassword);
    try {
      executionTool.readEnvironment(target.hostId(), target.password());
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      throw runSupport.mapRunFailure(e, "environment scan", target);
    }
    logger.info(
        "Agent API scanned environments host={} fingerprint={}",
        hostId,
        AgentApiAuthContext.getTokenFingerprint());
    return listEnvironments(hostId);
  }

  private void requireHostWrites() {
    if (!properties.isAllowRemoteHosts()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Host changes are disabled for the Agent API (geoweaver.agent.allow-remote-hosts=false)");
    }
  }

  private Host requireHost(String id) {
    if (AgentRunSupport.LOCALHOST_ID.equals(id)) {
      GeoweaverApplication.addLocalhost();
    }
    return hostRepository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host " + id + " not found"));
  }

  private Host requireEditableHost(String id) {
    if (AgentRunSupport.LOCALHOST_ID.equals(id)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "The built-in localhost host (100001) cannot be changed");
    }
    return requireHost(id);
  }

  private static String normalizeType(String type) {
    String t = BaseTool.isNull(type) ? "ssh" : type.trim().toLowerCase(Locale.ROOT);
    if (!HOST_TYPES.contains(t)) {
      throw badRequest("type must be one of " + String.join(", ", HOST_TYPES.stream().sorted().toList()));
    }
    return t;
  }

  private static String requireName(String name) {
    AgentSearch.requireDisplayName(name, "name");
    return name.trim();
  }

  /** @return {ip, port, username, url} validated for the host type */
  private static String[] validateConnection(
      String type, String ip, String port, String username, String url) {
    if ("ssh".equals(type)) {
      if (BaseTool.isNull(ip) || !HOSTNAME.matcher(ip.trim()).matches()) {
        throw badRequest("ip must be a hostname or IP address (letters, digits, . : - [ ])");
      }
      String p = BaseTool.isNull(port) ? "22" : port.trim();
      int n;
      try {
        n = Integer.parseInt(p);
      } catch (NumberFormatException e) {
        n = -1;
      }
      if (n < 1 || n > 65535) {
        throw badRequest("port must be 1-65535");
      }
      if (BaseTool.isNull(username) || !USERNAME.matcher(username.trim()).matches()) {
        throw badRequest("username is required (letters, digits, . _ @ -)");
      }
      return new String[] {
        ip.trim(), String.valueOf(n), username.trim(), BaseTool.isNull(url) ? null : requireHttpUrl(url)
      };
    }
    if (BaseTool.isNull(url)) {
      throw badRequest("url is required for " + type + " hosts");
    }
    return new String[] {ip, port, username, requireHttpUrl(url)};
  }

  private static String requireHttpUrl(String url) {
    try {
      URI u = URI.create(url.trim());
      String scheme = u.getScheme() == null ? "" : u.getScheme().toLowerCase(Locale.ROOT);
      if (!(scheme.equals("http") || scheme.equals("https")) || u.getHost() == null) {
        throw new IllegalArgumentException();
      }
    } catch (IllegalArgumentException e) {
      throw badRequest("url must be an http(s) URL");
    }
    return url.trim();
  }

  private static HostResponse toResponse(Host h) {
    boolean local =
        AgentRunSupport.LOCALHOST_ID.equals(h.getId())
            || "127.0.0.1".equals(h.getIp())
            || "localhost".equalsIgnoreCase(h.getIp());
    return new HostResponse(
        h.getId(), h.getName(), h.getType(), h.getIp(), h.getPort(), h.getUsername(), h.getUrl(), local);
  }

  private static ResponseStatusException badRequest(String msg) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
  }
}
