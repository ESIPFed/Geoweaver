package com.gw.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.api.v1.workspace.WorkspaceEvent;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.database.WorkspaceFeedRepository;
import com.gw.jpa.WorkspaceFeedEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Keeps every open browser up to date with what other Geoweaver processes on the same database do.
 *
 * <p>Several Geoweaver processes can share one database: a server in an Open OnDemand session,
 * another server or the command line that an agent drives over SSH, a second browser on another
 * machine. Each server tells only its own browsers about changes, so this component, once a
 * second:
 *
 * <ul>
 *   <li>passes on change notices that other servers wrote to the shared {@code gw_workspace_feed}
 *       table (processes, workflows and hosts created, edited or deleted), and writes this
 *       server's own notices there;
 *   <li>looks at the runs in the database that have not ended and tells the browsers when a run
 *       starts, changes status or ends, whichever Geoweaver process started it.
 * </ul>
 *
 * <p>{@code GEOWEAVER_WORKSPACE_SYNC=false} turns it off; {@code GEOWEAVER_WORKSPACE_SYNC_MS}
 * (1000) sets how often it looks.
 */
@Component
public class WorkspaceSync implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger log = Logger.getLogger(WorkspaceSync.class);

  /** Identifies this process's rows in the shared feed. */
  static final String ORIGIN = UUID.randomUUID().toString();

  static final long FEED_KEEP_MS = 3600_000L;
  private static final long CLEANUP_EVERY_MS = 600_000L;

  private final WorkspaceFeedRepository feed;
  private final HistoryRepository history;
  private final WorkflowRepository workflows;
  private final ProcessRepository processes;
  private final WorkspaceEventBroadcaster broadcaster;
  private final ObjectMapper mapper;
  private final Environment env;

  private ScheduledExecutorService executor;
  private long lastFeedId = -1;
  private long lastCleanup;

  /** Active runs seen last time: run id -> {process or workflow id, status}. */
  private Map<String, String[]> active;

  /** Step run id -> workflow id, for workflow runs that have not ended. */
  private final Map<String, String> workflowOfStep = new HashMap<>();

  /** Process or workflow id -> name, for the messages people see. */
  private final Map<String, String> names =
      new java.util.LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> e) {
          return size() > 2000;
        }
      };

  /** Workflow run id -> its step run ids (workflow runs already read). */
  private final Map<String, List<String>> stepsOfRun = new HashMap<>();

  public WorkspaceSync(
      WorkspaceFeedRepository feed,
      HistoryRepository history,
      WorkflowRepository workflows,
      ProcessRepository processes,
      WorkspaceEventBroadcaster broadcaster,
      ObjectMapper mapper,
      Environment env) {
    this.feed = feed;
    this.history = history;
    this.workflows = workflows;
    this.processes = processes;
    this.broadcaster = broadcaster;
    this.mapper = mapper;
    this.env = env;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (!(event.getApplicationContext() instanceof WebServerApplicationContext)) return; // CLI
    if (!env.getProperty("GEOWEAVER_WORKSPACE_SYNC", Boolean.class, true)) return;
    synchronized (this) {
      if (executor != null) return;
      broadcaster.setSharer(this::share);
      long ms = Math.max(200, env.getProperty("GEOWEAVER_WORKSPACE_SYNC_MS", Long.class, 1000L));
      executor =
          Executors.newSingleThreadScheduledExecutor(
              r -> {
                Thread t = new Thread(r, "geoweaver-workspace-sync");
                t.setDaemon(true);
                return t;
              });
      executor.scheduleWithFixedDelay(this::tick, ms, ms, TimeUnit.MILLISECONDS);
    }
  }

  /** Writes one of this server's change notices to the shared feed. */
  void share(WorkspaceEvent e) {
    try {
      WorkspaceFeedEvent row = new WorkspaceFeedEvent();
      row.setCreatedAt(new Date());
      row.setOrigin(ORIGIN);
      String json = mapper.writeValueAsString(e.withRevision(0));
      if (json.length() > 4000) return; // notices are small; never truncate JSON
      row.setPayload(json);
      feed.save(row);
    } catch (Exception ex) {
      log.debug("Could not share a workspace change: " + ex);
    }
  }

  /** One pass. Never throws. */
  synchronized void tick() {
    try {
      pullFeed();
    } catch (Throwable t) {
      log.debug("Workspace feed check failed: " + t);
    }
    try {
      watchRuns();
    } catch (Throwable t) {
      log.debug("Run check failed: " + t);
    }
    long now = System.currentTimeMillis();
    if (now - lastCleanup > CLEANUP_EVERY_MS) {
      lastCleanup = now;
      try {
        feed.deleteOlderThan(new Date(now - FEED_KEEP_MS));
      } catch (Throwable t) {
        log.debug("Workspace feed clean-up failed: " + t);
      }
    }
  }

  void pullFeed() throws Exception {
    if (lastFeedId < 0) {
      lastFeedId = feed.maxId(); // only what happens from now on
      return;
    }
    List<WorkspaceFeedEvent> rows;
    do {
      rows = feed.findTop200ByIdGreaterThanOrderByIdAsc(lastFeedId);
      for (WorkspaceFeedEvent row : rows) {
        lastFeedId = row.getId();
        if (ORIGIN.equals(row.getOrigin())) continue;
        broadcaster.relay(mapper.readValue(row.getPayload(), WorkspaceEvent.class));
      }
    } while (rows.size() == 200);
  }

  void watchRuns() {
    Map<String, String[]> now = new HashMap<>();
    for (Object[] r : history.findActiveRunHeads()) {
      now.put((String) r[0], new String[] {(String) r[1], (String) r[2]});
    }
    Map<String, String[]> before = active;
    active = now;
    if (before == null) return; // first look: only changes from now on

    List<String> started = new ArrayList<>();
    for (Map.Entry<String, String[]> e : now.entrySet()) {
      String[] old = before.get(e.getKey());
      if (old == null) {
        started.add(e.getKey());
      } else if (!Objects.equals(old[1], e.getValue()[1])) {
        emit("run.updated", e.getKey(), e.getValue()[0], e.getValue()[1]);
      }
    }
    if (!started.isEmpty()) {
      learnWorkflowSteps(now);
      for (String id : started) emit("run.started", id, now.get(id)[0], now.get(id)[1]);
    }

    Set<String> ended = new HashSet<>(before.keySet());
    ended.removeAll(now.keySet());
    if (!ended.isEmpty()) {
      Map<String, String> finalStatus = new HashMap<>();
      for (Object[] r : history.findStatusByIds(ended)) finalStatus.put((String) r[0], (String) r[1]);
      for (String id : ended) {
        emit("run.finished", id, before.get(id)[0], finalStatus.getOrDefault(id, "Deleted"));
      }
      for (String id : ended) {
        List<String> steps = stepsOfRun.remove(id);
        if (steps != null) workflowOfStep.keySet().removeAll(steps);
      }
    }
  }

  /** Reads the step run ids of workflow runs that have not ended (they are set when it starts). */
  private void learnWorkflowSteps(Map<String, String[]> now) {
    for (Map.Entry<String, String[]> e : now.entrySet()) {
      String runId = e.getKey();
      String target = e.getValue()[0];
      if (target == null || stepsOfRun.containsKey(runId) || !workflows.existsById(target)) continue;
      List<String> steps = new ArrayList<>();
      stepsOfRun.put(runId, steps);
      history
          .findById(runId)
          .ifPresent(
              h -> {
                if (h.getHistory_output() == null) return;
                for (String step : h.getHistory_output().split(";")) {
                  if (step.isBlank()) continue;
                  steps.add(step.trim());
                  workflowOfStep.put(step.trim(), target);
                }
              });
    }
  }

  private void emit(String event, String historyId, String target, String status) {
    String workflowId = workflowOfStep.get(historyId);
    if (workflowId == null && (stepsOfRun.containsKey(historyId) || isWorkflow(target))) {
      workflowId = target; // the workflow run itself
    }
    broadcaster.relay(WorkspaceEvent.run(event, historyId, target, nameOf(target), workflowId, status));
  }

  private String nameOf(String id) {
    if (id == null) return null;
    String n = names.get(id);
    if (n != null) return n;
    try {
      n = workflows.findById(id).map(w -> w.getName()).orElse(null);
      if (n == null) n = processes.findById(id).map(p -> p.getName()).orElse(null);
    } catch (RuntimeException e) {
      n = null;
    }
    if (n == null || n.isBlank()) n = id;
    names.put(id, n);
    return n;
  }

  private boolean isWorkflow(String id) {
    try {
      return id != null && workflows.existsById(id);
    } catch (RuntimeException e) {
      return false;
    }
  }

  /** For tests: whether the first look at the database has happened. */
  boolean started() {
    return active != null && lastFeedId >= 0;
  }
}
