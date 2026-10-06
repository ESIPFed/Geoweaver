package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.AgentExecutionStatuses;
import com.gw.api.v1.dto.RunStartResponse;
import com.gw.api.v1.dto.WorkflowCreateRequest;
import com.gw.api.v1.dto.WorkflowResponse;
import com.gw.api.v1.dto.WorkflowRunRequest;
import com.gw.api.v1.dto.WorkflowSummary;
import com.gw.api.v1.dto.WorkflowUpdateRequest;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import java.util.ArrayList;
import java.util.List;
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

  public AgentWorkflowService(
      WorkflowRepository workflowRepository,
      WorkflowTool workflowTool,
      AgentRunSupport runSupport,
      AgentRunIdempotencyStore idempotencyStore,
      AgentRunRateLimiter rateLimiter) {
    this.workflowRepository = workflowRepository;
    this.workflowTool = workflowTool;
    this.runSupport = runSupport;
    this.idempotencyStore = idempotencyStore;
    this.rateLimiter = rateLimiter;
  }

  public List<WorkflowSummary> listWorkflows() {
    List<WorkflowSummary> out = new ArrayList<>();
    for (Workflow w : workflowRepository.findAll()) {
      out.add(new WorkflowSummary(w.getId(), w.getName(), w.getOwner()));
    }
    return out;
  }

  public WorkflowResponse getWorkflow(String id) {
    return toResponse(requireWorkflow(id));
  }

  public WorkflowResponse createWorkflow(WorkflowCreateRequest req) {
    if (req == null || BaseTool.isNull(req.name()) || BaseTool.isNull(req.nodes())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and nodes are required");
    }
    String edges = req.edges() == null ? "[]" : req.edges();
    String id = workflowTool.add(req.name(), req.nodes(), edges, "111111");
    Workflow w = requireWorkflow(id);
    if (!BaseTool.isNull(req.description())) {
      w.setDescription(req.description());
      workflowRepository.save(w);
    }
    logger.info(
        "Agent API created workflow id={} fingerprint={}",
        id,
        AgentApiAuthContext.getTokenFingerprint());
    return toResponse(requireWorkflow(id));
  }

  public WorkflowResponse updateWorkflow(String id, WorkflowUpdateRequest req) {
    Workflow w = requireWorkflow(id);
    if (req == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
    }
    if (!BaseTool.isNull(req.name())) {
      w.setName(req.name());
    }
    if (!BaseTool.isNull(req.description())) {
      w.setDescription(req.description());
    }
    if (!BaseTool.isNull(req.nodes()) || !BaseTool.isNull(req.edges())) {
      String nodes = BaseTool.isNull(req.nodes()) ? w.getNodes() : req.nodes();
      String edges = BaseTool.isNull(req.edges()) ? w.getEdges() : req.edges();
      workflowTool.update(id, nodes, edges);
      w = requireWorkflow(id);
    } else {
      workflowRepository.save(w);
    }
    logger.info(
        "Agent API updated workflow id={} fingerprint={}",
        id,
        AgentApiAuthContext.getTokenFingerprint());
    return toResponse(w);
  }

  public RunStartResponse startRun(String workflowId, WorkflowRunRequest req) {
    requireWorkflow(workflowId);
    String fp = AgentApiAuthContext.getTokenFingerprint();
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

    String mode =
        req == null || BaseTool.isNull(req.mode()) ? "one" : req.mode().trim().toLowerCase();
    if (!"one".equals(mode) && !"multiple".equals(mode)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode must be one or multiple");
    }

    String password = runSupport.prepareLocalhostPassword(req == null ? null : req.hostPassword());
    String historyId = new RandomString(18).nextString();
    try {
      String[] hosts = new String[] {AgentRunSupport.LOCALHOST_ID};
      String[] pswds = new String[] {password};
      String[] envs = new String[] {""};
      workflowTool.execute(historyId, workflowId, mode, hosts, pswds, envs, "agent-api");
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      String msg = e.getMessage() == null ? "" : e.getMessage();
      logger.warn(
          "Agent API workflow run failed workflowId={} fingerprint={} msg={}",
          workflowId,
          fp,
          msg);
      String lower = msg.toLowerCase();
      if (lower.contains("authentication") || lower.contains("wrong password")) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Localhost password authentication failed");
      }
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start workflow run: " + msg);
    } finally {
      runSupport.clearLocalhostBypass();
    }

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

  private Workflow requireWorkflow(String id) {
    Optional<Workflow> opt = workflowRepository.findById(id);
    if (opt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found");
    }
    return opt.get();
  }

  private static WorkflowResponse toResponse(Workflow w) {
    return new WorkflowResponse(
        w.getId(), w.getName(), w.getDescription(), w.getOwner(), w.getNodes(), w.getEdges());
  }
}
