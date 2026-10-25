package com.gw.api.v1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.Workflow;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Portable JSON bundle of a workflow and the code of every process it uses, so an agent can copy
 * a workflow between Geoweaver servers or keep it in git. JSON only: no archive is unpacked, so
 * nothing from a bundle is ever written to the file system.
 */
@Service
public class AgentWorkflowBundleService {

  public static final String FORMAT = "geoweaver.workflow.v1";
  private static final Logger logger = LoggerFactory.getLogger(AgentWorkflowBundleService.class);
  private static final Pattern ID = Pattern.compile("^[A-Za-z0-9_]{1,64}$");
  private static final int MAX_PROCESSES = 500;
  private static final int MAX_CODE_CHARS = 1_000_000;

  private final ObjectMapper mapper = new ObjectMapper();
  private final WorkflowRepository workflowRepository;
  private final ProcessRepository processRepository;
  private final AgentWorkflowService workflowService;
  private final AgentProcessService processService;
  private final WorkflowGraphValidator graphValidator;
  private final WorkspaceEventBroadcaster workspaceEvents;

  public AgentWorkflowBundleService(
      WorkflowRepository workflowRepository,
      ProcessRepository processRepository,
      AgentWorkflowService workflowService,
      AgentProcessService processService,
      WorkflowGraphValidator graphValidator,
      WorkspaceEventBroadcaster workspaceEvents) {
    this.workflowRepository = workflowRepository;
    this.processRepository = processRepository;
    this.workflowService = workflowService;
    this.processService = processService;
    this.graphValidator = graphValidator;
    this.workspaceEvents = workspaceEvents;
  }

  public ObjectNode export(String workflowId) {
    Workflow w = workflowService.requireWorkflow(workflowId);
    ArrayNode nodes = parseArray(w.getNodes());
    ObjectNode bundle = mapper.createObjectNode();
    bundle.put("format", FORMAT);
    bundle.put("exportedAt", Instant.now().toString());
    ObjectNode wf = bundle.putObject("workflow");
    wf.put("id", w.getId());
    wf.put("name", w.getName());
    wf.put("description", w.getDescription());
    wf.set("nodes", nodes);
    wf.set("edges", parseArray(w.getEdges()));
    ArrayNode processes = bundle.putArray("processes");
    for (String pid : processIds(nodes)) {
      processRepository
          .findById(pid)
          .ifPresent(
              p -> {
                ObjectNode o = processes.addObject();
                o.put("id", p.getId());
                o.put("name", p.getName());
                o.put("lang", BaseTool.isNull(p.getLang()) ? p.getDescription() : p.getLang());
                o.put("description", p.getDescription());
                o.put("code", p.getCode());
              });
    }
    return bundle;
  }

  /**
   * Imports a bundle. Ids are kept so re-importing updates in place. Existing objects that differ
   * are a 409 unless {@code replace} is true; identical processes are reused.
   */
  public Map<String, Object> importBundle(JsonNode bundle, boolean replace) {
    if (bundle == null || !bundle.isObject() || !FORMAT.equals(bundle.path("format").asText())) {
      throw badRequest("Body must be a bundle from GET /api/v1/workflows/{id}/export (format " + FORMAT + ")");
    }
    JsonNode wf = bundle.path("workflow");
    if (!wf.isObject()) {
      throw badRequest("workflow is required");
    }
    AgentSearch.requireDisplayName(wf.path("name").asText(null), "workflow.name");
    AgentSearch.requireNoMarkup(wf.path("description").asText(null), "workflow.description");
    String workflowId = wf.path("id").asText("");
    if (workflowId.isEmpty()) {
      workflowId = new RandomString(20).nextString();
    } else if (!ID.matcher(workflowId).matches()) {
      throw badRequest("workflow.id must be 1-64 letters, digits or _");
    }

    JsonNode procs = bundle.path("processes");
    if (!procs.isMissingNode() && !procs.isArray()) {
      throw badRequest("processes must be an array");
    }
    if (procs.size() > MAX_PROCESSES) {
      throw badRequest("A bundle may hold at most " + MAX_PROCESSES + " processes");
    }
    List<GWProcess> incoming = new ArrayList<>();
    Set<String> incomingIds = new LinkedHashSet<>();
    for (int i = 0; i < procs.size(); i++) {
      GWProcess p = toProcess(procs.get(i), i);
      if (!incomingIds.add(p.getId())) {
        throw badRequest("processes[" + i + "].id " + p.getId() + " is duplicated");
      }
      incoming.add(p);
    }

    ArrayNode nodes = asArray(wf.get("nodes"), "workflow.nodes");
    for (String pid : processIds(nodes)) {
      if (!incomingIds.contains(pid) && processRepository.findById(pid).isEmpty()) {
        throw badRequest("workflow node uses process " + pid + ", which is neither in the bundle nor on this server");
      }
    }

    List<String> conflicts = new ArrayList<>();
    List<String> created = new ArrayList<>();
    List<String> updated = new ArrayList<>();
    List<String> unchanged = new ArrayList<>();
    for (GWProcess p : incoming) {
      Optional<GWProcess> existing = processRepository.findById(p.getId());
      if (existing.isEmpty()) {
        created.add(p.getId());
      } else if (sameProcess(existing.get(), p)) {
        unchanged.add(p.getId());
      } else if (replace) {
        updated.add(p.getId());
      } else {
        conflicts.add("process " + p.getId());
      }
    }
    boolean workflowExists = workflowRepository.existsById(workflowId);
    if (workflowExists && !replace) {
      conflicts.add("workflow " + workflowId);
    }
    if (!conflicts.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Already on this server with different content: "
              + String.join(", ", conflicts)
              + ". Re-send with ?replace=true to overwrite.");
    }

    for (GWProcess p : incoming) {
      if (!unchanged.contains(p.getId())) {
        processRepository.save(p);
      }
    }
    WorkflowGraphValidator.Graph graph = graphValidator.normalize(nodes, wf.get("edges"));
    Workflow w = workflowRepository.findById(workflowId).orElseGet(Workflow::new);
    w.setId(workflowId);
    w.setName(wf.path("name").asText());
    w.setDescription(wf.path("description").asText(null));
    w.setNodes(graph.nodes());
    w.setEdges(graph.edges());
    if (BaseTool.isNull(w.getOwner())) {
      w.setOwner("111111");
    }
    if (BaseTool.isNull(w.getConfidential())) {
      w.setConfidential("FALSE");
    }
    workflowRepository.save(w);

    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info(
        "Agent API imported workflow id={} processes created={} updated={} fingerprint={}",
        workflowId,
        created.size(),
        updated.size(),
        fp);
    workspaceEvents.publishAgent(
        workflowExists ? "workflow.updated" : "workflow.created", "workflow", workflowId, w.getName(), workflowId, fp);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("workflow", AgentWorkflowService.toResponse(workflowService.requireWorkflow(workflowId)));
    Map<String, Object> procOut = new LinkedHashMap<>();
    procOut.put("created", created);
    procOut.put("updated", updated);
    procOut.put("unchanged", unchanged);
    out.put("processes", procOut);
    return out;
  }

  private GWProcess toProcess(JsonNode n, int i) {
    String where = "processes[" + i + "]";
    if (!n.isObject()) {
      throw badRequest(where + " must be an object");
    }
    String id = n.path("id").asText("");
    if (!ID.matcher(id).matches()) {
      throw badRequest(where + ".id must be 1-64 letters, digits or _");
    }
    String name = n.path("name").asText("");
    try {
      AgentProcessService.requireSafeName(name);
    } catch (ResponseStatusException e) {
      throw badRequest(where + "." + e.getReason());
    }
    String lang = AgentProcessService.normalizeLang(n.path("lang").asText(""));
    if (!Set.of("python", "shell", "builtin").contains(lang)) {
      throw badRequest(where + ".lang must be python, shell or builtin");
    }
    if (!n.path("code").isTextual()) {
      throw badRequest(where + ".code must be a string");
    }
    String code = n.path("code").asText();
    if (code.length() > MAX_CODE_CHARS) {
      throw badRequest(where + ".code is longer than " + MAX_CODE_CHARS + " characters");
    }
    processService.requireValidCode(lang, code);
    AgentSearch.requireNoMarkup(n.path("description").asText(null), where + ".description");
    GWProcess p = new GWProcess();
    p.setId(id);
    p.setName(name);
    p.setLang(lang);
    p.setDescription(n.path("description").asText(lang));
    p.setCode(code);
    p.setOwner("111111");
    p.setConfidential("FALSE");
    return p;
  }

  private static boolean sameProcess(GWProcess a, GWProcess b) {
    return Objects.equals(a.getName(), b.getName())
        && Objects.equals(AgentProcessService.normalizeLang(a.getLang()), b.getLang())
        && Objects.equals(a.getCode(), b.getCode());
  }

  private static Set<String> processIds(ArrayNode nodes) {
    Set<String> ids = new LinkedHashSet<>();
    for (JsonNode n : nodes) {
      String id = n.path("id").asText("");
      int dash = id.indexOf('-');
      if (dash > 0) {
        ids.add(id.substring(0, dash));
      }
    }
    return ids;
  }

  private ArrayNode parseArray(String json) {
    try {
      JsonNode n = mapper.readTree(BaseTool.isNull(json) ? "[]" : json);
      return n != null && n.isArray() ? (ArrayNode) n : mapper.createArrayNode();
    } catch (Exception e) {
      return mapper.createArrayNode();
    }
  }

  private ArrayNode asArray(JsonNode n, String field) {
    if (n != null && n.isTextual()) {
      n = parseArray(n.asText());
    }
    if (n == null || !n.isArray() || n.isEmpty()) {
      throw badRequest(field + " must be a non-empty array");
    }
    return (ArrayNode) n;
  }

  private static ResponseStatusException badRequest(String msg) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
  }
}
