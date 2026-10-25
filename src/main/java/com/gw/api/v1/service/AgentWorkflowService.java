package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStartResponse;
import com.gw.api.v1.dto.WorkflowCreateRequest;
import com.gw.api.v1.dto.WorkflowResponse;
import com.gw.api.v1.dto.WorkflowRunRequest;
import com.gw.api.v1.dto.WorkflowSummary;
import com.gw.api.v1.dto.WorkflowUpdateRequest;
import com.gw.database.CheckpointRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.Checkpoint;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tools.CheckpointTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AgentWorkflowService {

  private static final Logger logger = LoggerFactory.getLogger(AgentWorkflowService.class);

  private final WorkflowRepository workflowRepository;
  private final WorkflowTool workflowTool;
  private final AgentRunSupport runSupport;
  private final AgentRunIdempotencyStore idempotencyStore;
  private final AgentRunRateLimiter rateLimiter;
  private final WorkspaceEventBroadcaster workspaceEvents;
  private final WorkflowGraphValidator graphValidator;
  private final HistoryRepository historyRepository;
  private final CheckpointRepository checkpointRepository;
  private final CheckpointTool checkpointTool;
  private final ObjectMapper mapper = new ObjectMapper();

  public AgentWorkflowService(
      WorkflowRepository workflowRepository,
      WorkflowTool workflowTool,
      AgentRunSupport runSupport,
      AgentRunIdempotencyStore idempotencyStore,
      AgentRunRateLimiter rateLimiter,
      WorkspaceEventBroadcaster workspaceEvents,
      WorkflowGraphValidator graphValidator,
      HistoryRepository historyRepository,
      CheckpointRepository checkpointRepository,
      CheckpointTool checkpointTool) {
    this.checkpointRepository = checkpointRepository;
    this.checkpointTool = checkpointTool;
    this.graphValidator = graphValidator;
    this.historyRepository = historyRepository;
    this.workflowRepository = workflowRepository;
    this.workflowTool = workflowTool;
    this.runSupport = runSupport;
    this.idempotencyStore = idempotencyStore;
    this.rateLimiter = rateLimiter;
    this.workspaceEvents = workspaceEvents;
  }

  /** @param query optional case-insensitive filter on id, name and description */
  public List<WorkflowSummary> listWorkflows(String query) {
    List<WorkflowSummary> out = new ArrayList<>();
    for (Workflow w : workflowRepository.findAll()) {
      if (AgentSearch.matches(query, w.getId(), w.getName(), w.getDescription())) {
        out.add(new WorkflowSummary(w.getId(), w.getName(), w.getOwner()));
      }
    }
    return out;
  }

  public WorkflowResponse getWorkflow(String id) {
    return toResponse(requireWorkflow(id));
  }

  public WorkflowResponse createWorkflow(WorkflowCreateRequest req) {
    if (req == null || BaseTool.isNull(req.name()) || req.nodes() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and nodes are required");
    }
    AgentSearch.requireDisplayName(req.name(), "name");
    AgentSearch.requireNoMarkup(req.description(), "description");
    WorkflowGraphValidator.Graph graph = graphValidator.normalize(req.nodes(), req.edges());
    String id = workflowTool.add(req.name(), graph.nodes(), graph.edges(), "111111");
    Workflow w = requireWorkflow(id);
    if (!BaseTool.isNull(req.description())) {
      w.setDescription(req.description());
      workflowRepository.save(w);
    }
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info(
        "Agent API created workflow id={} fingerprint={}",
        id,
        fp);
    Workflow created = requireWorkflow(id);
    workspaceEvents.publishAgent(
        "workflow.created", "workflow", id, created.getName(), id, fp);
    return toResponse(created);
  }

  public WorkflowResponse updateWorkflow(String id, WorkflowUpdateRequest req) {
    Workflow w = requireWorkflow(id);
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
    }
    if (!BaseTool.isNull(req.name())) {
      AgentSearch.requireDisplayName(req.name(), "name");
      w.setName(req.name());
    }
    if (!BaseTool.isNull(req.description())) {
      AgentSearch.requireNoMarkup(req.description(), "description");
      w.setDescription(req.description());
    }
    if (req.nodes() != null || req.edges() != null) {
      WorkflowGraphValidator.Graph graph =
          graphValidator.normalize(
              req.nodes() != null ? req.nodes() : TextNode.valueOf(w.getNodes()),
              req.edges() != null ? req.edges() : TextNode.valueOf(w.getEdges()));
      workflowTool.update(id, graph.nodes(), graph.edges());
      w = requireWorkflow(id);
    } else {
      workflowRepository.save(w);
    }
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info(
        "Agent API updated workflow id={} fingerprint={}",
        id,
        fp);
    workspaceEvents.publishAgent("workflow.updated", "workflow", id, w.getName(), id, fp);
    return toResponse(w);
  }

  public RunStartResponse startRun(String workflowId, WorkflowRunRequest req) {
    String fp = AgentApiAuthContext.getTokenFingerprint();
    requireWorkflow(workflowId);
    if (!rateLimiter.tryAcquire(fp)) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Run rate limit exceeded for this token");
    }

    String clientRunKey = req == null ? null : req.clientRunKey();
    String existing = idempotencyStore.lookup(fp, clientRunKey);
    if (existing != null) {
      try {
        History h = runSupport.requireHistory(existing);
        return new RunStartResponse(
            existing, AgentExecutionStatuses.normalize(h.getIndicator()), true);
      } catch (ResponseStatusException e) {
        // stale key — fall through and start a new run
      }
    }

    Workflow workflow = requireWorkflow(workflowId);
    RunPlan plan = planHosts(workflow, req == null ? WorkflowRunRequest.empty() : req);
    String historyId = new RandomString(18).nextString();
    try {
      workflowTool.execute(
          historyId, workflowId, plan.mode(), plan.hosts(), plan.passwords(), plan.envs(), "agent-api");
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      Exception scrubbed = plan.scrubbed(e);
      throw runSupport.mapRunFailure(scrubbed, "workflowId=" + workflowId, plan.firstFailure(scrubbed));
    }
    saveCheckpoint(workflow, historyId);

    idempotencyStore.put(fp, clientRunKey, historyId);
    String status = ExecutionStatus.RUNNING;
    try {
      History h = runSupport.requireHistory(historyId);
      status = AgentExecutionStatuses.normalize(h.getIndicator());
      if (BaseTool.isNull(status) || ExecutionStatus.UNKOWN.equals(status)) {
        status = ExecutionStatus.RUNNING;
      }
    } catch (ResponseStatusException ignored) {
      // history row may appear shortly after async start
    }
    logger.info(
        "Agent API started workflow run historyId={} workflowId={} fingerprint={}",
        historyId,
        workflowId,
        fp);
    return new RunStartResponse(historyId, status, false);
  }

  public Map<String, Object> deleteWorkflow(String id) {
    Workflow w = requireWorkflow(id);
    workflowTool.del(id);
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info("Agent API deleted workflow id={} fingerprint={}", id, fp);
    workspaceEvents.publishAgent("workflow.deleted", "workflow", id, w.getName(), id, fp);
    return Map.of("id", id, "deleted", true);
  }

  public List<RunResponse> listRuns(String workflowId, Integer logLimit) {
    requireWorkflow(workflowId);
    List<RunResponse> out = new ArrayList<>();
    List<History> histories = historyRepository.findByWorkflowId(workflowId);
    if (histories == null) {
      return out;
    }
    int limit = logLimit == null ? 0 : Math.min(logLimit, 4000);
    for (History h : histories) {
      out.add(runSupport.toRunResponse(h, limit, false));
      if (out.size() >= 50) {
        break;
      }
    }
    return out;
  }

  /** Host, password and environment arrays in node order, as GeoweaverWorkflowTask expects. */
  record RunPlan(
      String mode,
      String[] hosts,
      String[] passwords,
      String[] envs,
      List<AgentRunSupport.RunTarget> targets) {

    /** Copy of {@code e} whose message has every password of this run replaced by ***. */
    Exception scrubbed(Exception e) {
      String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
      for (String p : passwords) {
        if (!BaseTool.isNull(p)) {
          msg = msg.replace(p, "***");
        }
      }
      return new RuntimeException(msg);
    }

    /** The target an error message most likely refers to (first remote host, else localhost). */
    AgentRunSupport.RunTarget firstFailure(Exception e) {
      String msg = e.getMessage() == null ? "" : e.getMessage();
      for (AgentRunSupport.RunTarget t : targets) {
        if (!t.local() && msg.contains(t.hostId())) {
          return t;
        }
      }
      for (AgentRunSupport.RunTarget t : targets) {
        if (!t.local()) {
          return t;
        }
      }
      return targets.isEmpty() ? null : targets.get(0);
    }
  }

  RunPlan planHosts(Workflow workflow, WorkflowRunRequest req) {
    if (req.mode() != null
        && !"one".equalsIgnoreCase(req.mode().trim())
        && !"multiple".equalsIgnoreCase(req.mode().trim())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode must be one or multiple");
    }
    Map<String, String> passwords =
        req.hostPasswords() == null ? new HashMap<>() : new HashMap<>(req.hostPasswords());
    String defaultHost = BaseTool.isNull(req.hostId()) ? AgentRunSupport.LOCALHOST_ID : req.hostId().trim();
    if (!BaseTool.isNull(req.hostPassword())) {
      passwords.putIfAbsent(defaultHost, req.hostPassword());
    }
    Map<String, WorkflowRunRequest.NodeHost> nodeHosts =
        req.nodeHosts() == null ? Map.of() : req.nodeHosts();

    List<String> nodeIds = nodeIds(workflow);
    for (String assigned : nodeHosts.keySet()) {
      if (!nodeIds.contains(assigned)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "nodeHosts key " + assigned + " is not a node of this workflow");
      }
    }
    Map<String, AgentRunSupport.RunTarget> resolved = new LinkedHashMap<>();
    String[] hosts = new String[Math.max(1, nodeIds.size())];
    String[] pswds = new String[hosts.length];
    String[] envs = new String[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      WorkflowRunRequest.NodeHost nh = i < nodeIds.size() ? nodeHosts.get(nodeIds.get(i)) : null;
      String hostId = nh == null || BaseTool.isNull(nh.hostId()) ? defaultHost : nh.hostId().trim();
      // The run-level envId belongs to the default host; other hosts need their own nodeHosts envId.
      String envId =
          nh != null && !BaseTool.isNull(nh.envId())
              ? nh.envId()
              : hostId.equals(defaultHost) ? req.envId() : null;
      AgentRunSupport.RunTarget target =
          resolved.computeIfAbsent(
              hostId, h -> runSupport.resolveRunTarget(h, passwords.get(h)));
      hosts[i] = target.hostId();
      pswds[i] = target.password();
      envs[i] = runSupport.resolveEnvironment(envId, target.hostId()) == null ? "" : envId;
    }
    boolean single = nodeHosts.isEmpty();
    return new RunPlan(
        single ? "one" : "multiple", hosts, pswds, envs, new ArrayList<>(resolved.values()));
  }

  private List<String> nodeIds(Workflow workflow) {
    List<String> out = new ArrayList<>();
    try {
      JsonNode nodes = mapper.readTree(BaseTool.isNull(workflow.getNodes()) ? "[]" : workflow.getNodes());
      for (JsonNode n : nodes) {
        out.add(n.path("id").asText());
      }
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Workflow " + workflow.getId() + " has an unreadable graph; PUT new nodes");
    }
    return out;
  }

  /** Same snapshot the GUI saves on run, so POST /workflows/{id}/restore can roll back to it. */
  private void saveCheckpoint(Workflow workflow, String historyId) {
    Checkpoint checkpoint = new Checkpoint();
    checkpoint.setWorkflow(workflow);
    checkpoint.setNodes(workflow.getNodes());
    checkpoint.setEdges(workflow.getEdges());
    checkpoint.setCreatedAt(new Date());
    checkpoint.setExecutionId(historyId);
    checkpointRepository.save(checkpoint);
  }

  /** Marks one node to be skipped (or run again) on later runs. */
  public WorkflowResponse setStepSkipped(String workflowId, String nodeId, Boolean skip) {
    Workflow w = requireWorkflow(workflowId);
    if (skip == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "skip (true|false) is required");
    }
    if (!nodeIds(w).contains(nodeId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Node " + nodeId + " is not in workflow " + workflowId);
    }
    workflowTool.skip_process(workflowId, nodeId, String.valueOf(skip));
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info(
        "Agent API set skip={} node={} workflow={} fingerprint={}", skip, nodeId, workflowId, fp);
    Workflow updated = requireWorkflow(workflowId);
    workspaceEvents.publishAgent("workflow.updated", "workflow", workflowId, updated.getName(), workflowId, fp);
    return toResponse(updated);
  }

  /** Snapshots saved by GUI and Agent API runs, newest first. */
  public List<Map<String, Object>> listCheckpoints(String workflowId) {
    requireWorkflow(workflowId);
    List<Checkpoint> list = new ArrayList<>(checkpointRepository.findByWorkflowId(workflowId));
    list.sort(
        Comparator.comparing(
            Checkpoint::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
    List<Map<String, Object>> out = new ArrayList<>();
    for (Checkpoint c : list) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", String.valueOf(c.getId()));
      m.put("runId", c.getExecutionId());
      m.put("createdAt", c.getCreatedAt() == null ? null : c.getCreatedAt().toInstant().toString());
      out.add(m);
    }
    return out;
  }

  /** Restores the graph and step code saved for run {@code runId} of this workflow. */
  public WorkflowResponse restoreCheckpoint(String workflowId, String runId) {
    requireWorkflow(workflowId);
    if (BaseTool.isNull(runId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "runId is required");
    }
    boolean known =
        checkpointRepository.findCheckpointByExecutionId(runId).stream()
            .anyMatch(c -> c.getWorkflow() != null && workflowId.equals(c.getWorkflow().getId()));
    if (!known || historyRepository.findHistoryWithExecutionId(runId, workflowId).isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "No checkpoint for run " + runId + " of workflow " + workflowId
              + ". See GET /api/v1/workflows/" + workflowId + "/checkpoints");
    }
    checkpointTool.restoreCheckpoint(workflowId, runId);
    String fp = AgentApiAuthContext.getTokenFingerprint();
    logger.info("Agent API restored workflow={} to run={} fingerprint={}", workflowId, runId, fp);
    Workflow w = requireWorkflow(workflowId);
    workspaceEvents.publishAgent("workflow.updated", "workflow", workflowId, w.getName(), workflowId, fp);
    return toResponse(w);
  }

  Workflow requireWorkflow(String id) {
    Optional<Workflow> opt = workflowRepository.findById(id);
    if (opt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found");
    }
    return opt.get();
  }

  static WorkflowResponse toResponse(Workflow w) {
    return new WorkflowResponse(
        w.getId(), w.getName(), w.getDescription(), w.getOwner(), w.getNodes(), w.getEdges());
  }
}
