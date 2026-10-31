package com.gw.api.v1;

import com.gw.api.v1.dto.EnvironmentResponse;
import com.gw.api.v1.dto.HostRequest;
import com.gw.api.v1.dto.HostResponse;
import com.gw.api.v1.service.AgentHostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Hosts (localhost, SSH servers, Jupyter/GEE endpoints) and their Python environments. */
@RestController
@RequestMapping("/api/v1/hosts")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Tag(name = "Agent API: hosts")
public class AgentHostController {

  private final AgentHostService hostService;
  private final AgentApiProperties properties;

  public AgentHostController(AgentHostService hostService, AgentApiProperties properties) {
    this.hostService = hostService;
    this.properties = properties;
  }

  @GetMapping
  @Operation(summary = "List hosts; q filters by id, name, ip or url. No passwords are stored")
  public List<HostResponse> list(@RequestParam(value = "q", required = false) String q) {
    return hostService.listHosts(q);
  }

  @PostMapping
  @Operation(summary = "Add a host (ssh: name, ip, port, username; jupyter/gee: name, url)")
  public HostResponse create(@RequestBody HostRequest body) {
    return hostService.createHost(body);
  }

  @GetMapping("/{id}")
  public HostResponse get(@PathVariable("id") String id) {
    return hostService.getHost(id);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Edit a host (omitted fields are kept)")
  public HostResponse update(@PathVariable("id") String id, @RequestBody HostRequest body) {
    return hostService.updateHost(id, body);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a host and its saved environments (run history is kept)")
  public Map<String, Object> delete(@PathVariable("id") String id) {
    AgentApiController.requireDeleteAllowed(properties);
    return hostService.deleteHost(id);
  }

  @GetMapping("/{id}/environments")
  @Operation(summary = "Python environments saved for this host; pass id as envId in runs")
  public List<EnvironmentResponse> environments(@PathVariable("id") String id) {
    return hostService.listEnvironments(id);
  }

  @PostMapping("/{id}/environments/scan")
  @Operation(
      summary =
          "Find and save Python environments on the host. Body {hostPassword} (SSH password; not"
              + " needed for localhost when allow-localhost-runs=true)")
  public List<EnvironmentResponse> scan(
      @PathVariable("id") String id, @RequestBody(required = false) Map<String, String> body) {
    return hostService.scanEnvironments(id, body == null ? null : body.get("hostPassword"));
  }
}
