/**
 * Google-Doc-style Agent ↔ human workspace collaboration.
 *
 * Primary transport: HTTP long poll on /api/workspace/poll/{CLIENT_TOKEN}
 * (matches Geoweaver's polling-first GUI). Also: revision clock every 2s,
 * optional WebSocket fan-in, soft presence heartbeats, dirty-buffer safety.
 */
GW.workspaceevents = {
  ws: null,
  refreshTimer: null,
  revisionTimer: null,
  presenceTimer: null,
  pollXhr: null,
  isPolling: false,
  lastRevision: 0,
  seenRevisions: {},
  seenOrder: [],
  bannerDismissedThisSession: false,
  presenceByKey: {},

  init: function () {
    if (!GW.general || !GW.general.CLIENT_TOKEN) {
      console.warn("Workspace events: CLIENT_TOKEN missing");
      return;
    }
    GW.workspaceevents._hookWorkflowOpen();
    GW.workspaceevents.showRiskBanner();
    GW.workspaceevents.subscribeAndPoll();
    GW.workspaceevents.startRevisionWatch();
    GW.workspaceevents.startPresenceHeartbeat();
    GW.workspaceevents.connectWebSocketOptional();
  },

  subscribeAndPoll: function () {
    var token = GW.general.CLIENT_TOKEN;
    $.ajax({
      url: GW.path.getBasePath() + "api/workspace/subscribe",
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify({ clientToken: token }),
      dataType: "json",
      success: function (res) {
        if (res && typeof res.revision === "number") {
          GW.workspaceevents.lastRevision = res.revision;
        }
        GW.workspaceevents.startLongPoll();
      },
      error: function () {
        // Still try polling / revision — subscribe is best-effort
        GW.workspaceevents.startLongPoll();
      },
    });
  },

  startLongPoll: function () {
    if (GW.workspaceevents.isPolling) {
      return;
    }
    GW.workspaceevents.isPolling = true;
    GW.workspaceevents._pollOnce();
  },

  _pollOnce: function () {
    if (!GW.workspaceevents.isPolling) {
      return;
    }
    var token = GW.general.CLIENT_TOKEN;
    var xhr = new XMLHttpRequest();
    GW.workspaceevents.pollXhr = xhr;
    xhr.open("GET", GW.path.getBasePath() + "api/workspace/poll/" + encodeURIComponent(token), true);
    xhr.timeout = 30000;
    xhr.onload = function () {
      if (xhr.status === 200) {
        try {
          var messages = JSON.parse(xhr.responseText);
          if (messages && messages.length) {
            for (var i = 0; i < messages.length; i++) {
              GW.workspaceevents.onMessage(messages[i]);
            }
          }
        } catch (e) {
          console.warn("Workspace poll parse error", e);
        }
      }
      setTimeout(GW.workspaceevents._pollOnce, 50);
    };
    xhr.onerror = function () {
      setTimeout(GW.workspaceevents._pollOnce, 2000);
    };
    xhr.ontimeout = function () {
      setTimeout(GW.workspaceevents._pollOnce, 50);
    };
    xhr.send();
  },

  startRevisionWatch: function () {
    if (GW.workspaceevents.revisionTimer) {
      return;
    }
    GW.workspaceevents.revisionTimer = setInterval(function () {
      $.ajax({
        url: GW.path.getBasePath() + "api/workspace/revision",
        type: "GET",
        dataType: "json",
        success: function (res) {
          if (!res || typeof res.revision !== "number") {
            return;
          }
          if (res.presence) {
            GW.workspaceevents._mergePresenceSnapshot(res.presence);
          }
          if (res.revision > GW.workspaceevents.lastRevision) {
            var prev = GW.workspaceevents.lastRevision;
            GW.workspaceevents.lastRevision = res.revision;
            if (prev > 0) {
              // Missed push — reconcile like Docs refresh
              GW.workspaceevents._scheduleListRefresh();
              GW.workspaceevents._reconcileOpenViews();
              GW.workspaceevents._renderPresenceStrip();
            }
          }
        },
      });
    }, 2000);
  },

  startPresenceHeartbeat: function () {
    if (GW.workspaceevents.presenceTimer) {
      return;
    }
    GW.workspaceevents.presenceTimer = setInterval(function () {
      var openWf = GW.workflow && GW.workflow.loaded_workflow;
      var openPid =
        (GW.process && GW.process.current_pid) ||
        ($("#processid").length ? $("#processid").val() : null);
      var kind = null;
      var id = null;
      if (openWf) {
        kind = "workflow";
        id = openWf;
      } else if (openPid) {
        kind = "process";
        id = openPid;
      }
      if (!kind) {
        return;
      }
      $.ajax({
        url: GW.path.getBasePath() + "api/workspace/presence",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
          clientToken: GW.general.CLIENT_TOKEN,
          objectKind: kind,
          objectId: id,
          actor: "human",
        }),
      });
    }, 15000);
  },

  connectWebSocketOptional: function () {
    try {
      var url = GW.path.getWsPrefix() + "workspace-socket";
      var ws = new WebSocket(url);
      GW.workspaceevents.ws = ws;
      ws.onopen = function () {
        ws.send("token:" + GW.general.CLIENT_TOKEN);
      };
      ws.onmessage = function (ev) {
        GW.workspaceevents.onMessage(ev.data);
      };
      ws.onclose = function () {
        setTimeout(function () {
          GW.workspaceevents.connectWebSocketOptional();
        }, 8000);
      };
    } catch (e) {
      /* long poll is enough */
    }
  },

  showRiskBanner: function () {
    $.ajax({
      url: GW.path.getBasePath() + "api/config/agent-api",
      type: "GET",
      dataType: "json",
      success: function (cfg) {
        GW.workspaceevents._applySettingsLine(cfg);
        if (!cfg || !cfg.apiEnabled) {
          return;
        }
        var networkRisk = !!cfg.networkRiskActive;
        if (GW.workspaceevents.bannerDismissedThisSession && !networkRisk) {
          return;
        }
        GW.workspaceevents._renderTopBanner(cfg, networkRisk);
      },
    });
  },

  _applySettingsLine: function (cfg) {
    var el = document.getElementById("gw-agent-api-settings-status");
    if (!el) {
      return;
    }
    if (!cfg || !cfg.apiEnabled) {
      el.textContent = "Agent API: off";
      return;
    }
    el.textContent =
      "Agent API: on (localhost runs=" +
      cfg.allowLocalhostRuns +
      ", non-loopback=" +
      cfg.allowNonLoopback +
      ", workspace events=" +
      cfg.workspaceEventsEnabled +
      "). Live collab uses /api/workspace long poll. A leaked Bearer token is full API access.";
  },

  _renderTopBanner: function (cfg, networkRisk) {
    var existing = document.getElementById("gw-agent-api-risk-banner");
    if (existing) {
      existing.remove();
    }
    var bar = document.createElement("div");
    bar.id = "gw-agent-api-risk-banner";
    bar.setAttribute("role", "status");
    bar.style.cssText =
      "position:relative;z-index:10050;padding:8px 12px;background:#fff3cd;color:#664d03;" +
      "border-bottom:1px solid #ffecb5;font-size:13px;display:flex;gap:12px;align-items:center;";
    var msg = document.createElement("div");
    msg.style.flex = "1";
    msg.textContent =
      "Agent API is enabled. Open browsers sync process/workflow lists live (Google-Doc-style awareness). " +
      "A valid Bearer token can create and run python/shell jobs on this host." +
      (networkRisk
        ? " This server is reachable beyond this computer (non-loopback bind)."
        : "") +
      " Prefer HTTPS. Harden with geoweaver.agent.api-enabled=false or revoke tokens.";
    var dismiss = document.createElement("button");
    dismiss.type = "button";
    dismiss.className = "btn btn-sm btn-outline-secondary";
    dismiss.textContent = "Dismiss for this session";
    dismiss.onclick = function () {
      GW.workspaceevents.bannerDismissedThisSession = true;
      bar.remove();
    };
    bar.appendChild(msg);
    bar.appendChild(dismiss);
    var host =
      document.getElementById("main-content-area") ||
      document.querySelector(".main-panel") ||
      document.body;
    host.insertBefore(bar, host.firstChild);
  },

  onMessage: function (raw) {
    if (!raw || typeof raw !== "string") {
      return;
    }
    if (raw.indexOf("Session_Status:") === 0) {
      return;
    }
    var evt;
    try {
      evt = JSON.parse(raw);
    } catch (e) {
      return;
    }
    if (!evt || !evt.event) {
      return;
    }
    if (typeof evt.revision === "number" && evt.revision > 0) {
      // the WebSocket and the long poll can both deliver an event: handle it once
      var seen = GW.workspaceevents.seenRevisions;
      if (seen[evt.revision]) return;
      seen[evt.revision] = true;
      GW.workspaceevents.seenOrder.push(evt.revision);
      if (GW.workspaceevents.seenOrder.length > 500) delete seen[GW.workspaceevents.seenOrder.shift()];
    }
    if (typeof evt.revision === "number" && evt.revision > GW.workspaceevents.lastRevision) {
      GW.workspaceevents.lastRevision = evt.revision;
    }
    if (evt.objectKind === "run") {
      GW.workspaceevents._onRunEvent(evt);
      return;
    }
    GW.workspaceevents._notePresence(evt);
    GW.workspaceevents._scheduleListRefresh();
    GW.workspaceevents._handleOpenObject(evt);
  },

  _mergePresenceSnapshot: function (snap) {
    var now = Date.now();
    Object.keys(snap || {}).forEach(function (k) {
      var p = snap[k];
      if (!p) return;
      GW.workspaceevents.presenceByKey[k] = {
        actor: p.actor || "unknown",
        tokenFingerprint: p.tokenFingerprint || "",
        at: now,
        objectName: k,
      };
    });
    GW.workspaceevents._renderPresenceStrip();
  },

  _notePresence: function (evt) {
    var keys = [];
    if (evt.objectKind && evt.objectId) {
      keys.push(evt.objectKind + ":" + evt.objectId);
    }
    if (evt.workflowId) {
      keys.push("workflow:" + evt.workflowId);
    }
    var now = Date.now();
    keys.forEach(function (k) {
      GW.workspaceevents.presenceByKey[k] = {
        actor: evt.actor || "agent",
        tokenFingerprint: evt.tokenFingerprint || "",
        at: now,
        objectName: evt.objectName || evt.objectId,
      };
    });
    GW.workspaceevents._renderPresenceStrip();
  },

  _renderPresenceStrip: function () {
    var strip = document.getElementById("gw-agent-presence-strip");
    if (!strip) {
      strip = document.createElement("div");
      strip.id = "gw-agent-presence-strip";
      strip.setAttribute("role", "status");
      strip.style.cssText =
        "display:none;padding:6px 12px;background:#e7f1ff;color:#084298;border-bottom:1px solid #b6d4fe;font-size:13px;";
      var host =
        document.getElementById("main-content-area") ||
        document.querySelector(".main-panel") ||
        document.body;
      var after = document.getElementById("gw-agent-api-risk-banner");
      if (after && after.parentNode === host) {
        after.insertAdjacentElement("afterend", strip);
      } else {
        host.insertBefore(strip, host.firstChild);
      }
    }
    var live = [];
    var now = Date.now();
    var ttl = 5 * 60 * 1000;
    Object.keys(GW.workspaceevents.presenceByKey).forEach(function (k) {
      var p = GW.workspaceevents.presenceByKey[k];
      if (now - p.at <= ttl) {
        live.push({ key: k, p: p });
      } else {
        delete GW.workspaceevents.presenceByKey[k];
      }
    });
    if (!live.length) {
      strip.style.display = "none";
      strip.textContent = "";
      return;
    }
    var parts = live.slice(0, 4).map(function (x) {
      var who = x.p.actor === "agent" ? "AI agent" : "A person";
      var fp = x.p.tokenFingerprint ? " " + x.p.tokenFingerprint : "";
      return who + fp + " on " + x.key;
    });
    strip.style.display = "block";
    strip.textContent =
      "Live collaboration: " +
      parts.join("; ") +
      ". You may both edit — this is awareness, not a lock. Unsaved local edits are kept until you reload.";
  },

  _scheduleListRefresh: function () {
    if (GW.workspaceevents.refreshTimer) {
      clearTimeout(GW.workspaceevents.refreshTimer);
    }
    GW.workspaceevents.refreshTimer = setTimeout(function () {
      GW.workspaceevents.refreshTimer = null;
      if (GW.menu && typeof GW.menu.refresh === "function") {
        GW.menu.refresh();
      }
    }, 400);
  },

  _reconcileOpenViews: function () {
    var openWf = GW.workflow && GW.workflow.loaded_workflow;
    if (
      openWf &&
      GW.workspace &&
      !GW.workspace.workflow_unsaved &&
      typeof GW.workflow.showWorkflow === "function"
    ) {
      GW.workflow.showWorkflow(openWf);
    }
  },

  _handleOpenObject: function (evt) {
    var openWf = GW.workflow && GW.workflow.loaded_workflow;
    var touchesOpenWf =
      (evt.objectKind === "workflow" && evt.objectId === openWf) ||
      (evt.workflowId && evt.workflowId === openWf);

    if (touchesOpenWf) {
      if (GW.workspace && GW.workspace.workflow_unsaved) {
        GW.workspaceevents._showReloadNotice(
          "Someone else (or an AI agent) updated this workflow. You have unsaved canvas changes — reload only when ready.",
          function () {
            if (openWf && typeof GW.workflow.showWorkflow === "function") {
              GW.workflow.showWorkflow(openWf);
            }
          },
        );
      } else if (openWf && typeof GW.workflow.showWorkflow === "function") {
        GW.workflow.showWorkflow(openWf);
        GW.workspaceevents._toast(
          "Workflow refreshed (" + (evt.actor || "collaborator") + ").",
        );
      }
    }

    var openPid =
      (GW.process && GW.process.current_pid) ||
      ($("#processid").length ? $("#processid").val() : null);
    if (evt.objectKind === "process" && openPid && openPid === evt.objectId) {
      GW.workspaceevents._showReloadNotice(
        (evt.actor === "human" ? "A person" : "An AI agent") +
          " updated process " +
          (evt.objectName || evt.objectId) +
          ". Reload to see the saved code (your editor buffer is kept until you reload).",
        function () {
          if (typeof GW.process.display === "function") {
            GW.process.display(evt.objectId);
          } else if (typeof GW.menu.details === "function") {
            GW.menu.details(evt.objectId, "process");
          }
        },
      );
    }
  },

  _showReloadNotice: function (text, onReload) {
    var id = "gw-agent-reload-notice";
    var el = document.getElementById(id);
    if (!el) {
      el = document.createElement("div");
      el.id = id;
      el.style.cssText =
        "position:fixed;right:16px;bottom:16px;z-index:10060;max-width:360px;padding:12px;" +
        "background:#fff;border:1px solid #ccc;box-shadow:0 2px 8px rgba(0,0,0,.15);font-size:13px;";
      document.body.appendChild(el);
    }
    el.innerHTML = "";
    var p = document.createElement("div");
    p.textContent = text;
    var btn = document.createElement("button");
    btn.type = "button";
    btn.className = "btn btn-sm btn-primary";
    btn.style.marginTop = "8px";
    btn.textContent = "Reload saved version";
    btn.onclick = function () {
      el.remove();
      if (onReload) onReload();
    };
    var x = document.createElement("button");
    x.type = "button";
    x.className = "btn btn-sm btn-link";
    x.textContent = "Keep my buffer";
    x.onclick = function () {
      el.remove();
    };
    el.appendChild(p);
    el.appendChild(btn);
    el.appendChild(x);
  },

  _toast: function (text) {
    if (window.toastr && toastr.info) {
      toastr.info(text);
      return;
    }
    var box = document.getElementById("gw-workspace-toasts");
    if (!box) {
      box = document.createElement("div");
      box.id = "gw-workspace-toasts";
      box.style.cssText =
        "position:fixed;left:16px;bottom:16px;z-index:10070;display:flex;flex-direction:column;gap:6px;max-width:360px;";
      document.body.appendChild(box);
    }
    var t = document.createElement("div");
    t.setAttribute("role", "status");
    t.className = "gw-workspace-toast";
    t.textContent = text;
    t.style.cssText =
      "padding:8px 12px;background:#323232;color:#fff;border-radius:4px;font-size:13px;box-shadow:0 2px 6px rgba(0,0,0,.3);";
    box.appendChild(t);
    while (box.children.length > 4) box.removeChild(box.firstChild);
    setTimeout(function () {
      if (t.parentNode) t.parentNode.removeChild(t);
    }, 6000);
  },

  // ---- Runs started anywhere on this database (this browser, another browser, another
  // Geoweaver server, or the command line over SSH) ----

  liveRuns: {},
  runWatchTimer: null,
  runWatchLastId: null,
  historyRefreshTimer: null,

  _onRunEvent: function (evt) {
    var self = GW.workspaceevents;
    var target = evt.target || evt.objectName; // process or workflow id
    var name = evt.objectName || target;
    var isWorkflowRun = !!evt.workflowId && target === evt.workflowId;
    if (evt.event === "run.finished") {
      delete self.liveRuns[evt.objectId];
    } else {
      self.liveRuns[evt.objectId] = {
        target: target,
        name: name,
        workflowId: evt.workflowId,
        status: evt.status,
        workflowRun: isWorkflowRun,
      };
    }
    if (isWorkflowRun || !evt.workflowId) {
      if (evt.event === "run.started") {
        self._toast(
          (isWorkflowRun ? "Workflow " : "Process ") + name + " started running.",
        );
      } else if (evt.event === "run.finished") {
        self._toast(
          (isWorkflowRun ? "Workflow " : "Process ") + name + " finished: " + evt.status + ".",
        );
      }
    }
    self._renderRunStrip();
    if (evt.event === "run.started") {
      // a run of something imported elsewhere (for example on the command line) lists it here
      self._scheduleListRefresh();
    }
    if (evt.event !== "run.updated") {
      self._scheduleHistoryRefresh(target, evt.workflowId);
    }
    var openWf = GW.workflow && GW.workflow.loaded_workflow;
    if (openWf && evt.workflowId === openWf) {
      self._watchWorkflowRuns();
    }
  },

  /** One line naming what is running now, wherever it was started. */
  _renderRunStrip: function () {
    var self = GW.workspaceevents;
    var strip = document.getElementById("gw-live-runs-strip");
    var names = [];
    Object.keys(self.liveRuns).forEach(function (id) {
      var r = self.liveRuns[id];
      if (r.workflowRun) names.push("workflow " + (r.name || r.target));
      else if (!r.workflowId) names.push("process " + (r.name || r.target));
    });
    if (!names.length) {
      if (strip) strip.style.display = "none";
      return;
    }
    if (!strip) {
      strip = document.createElement("div");
      strip.id = "gw-live-runs-strip";
      strip.setAttribute("role", "status");
      strip.style.cssText =
        "padding:6px 12px;background:#e8f5e9;color:#1b5e20;border-bottom:1px solid #c8e6c9;font-size:13px;";
      var host =
        document.getElementById("main-content-area") ||
        document.querySelector(".main-panel") ||
        document.body;
      host.insertBefore(strip, host.firstChild);
    }
    strip.style.display = "block";
    strip.textContent = "Running now: " + names.slice(0, 5).join(", ") + (names.length > 5 ? ", ..." : "");
  },

  /** Refreshes the history tables on screen for this process or workflow (at most once a second). */
  _scheduleHistoryRefresh: function (target, workflowId) {
    var self = GW.workspaceevents;
    self._pendingHistory = self._pendingHistory || {};
    if (target) self._pendingHistory[target] = true;
    if (workflowId) self._pendingHistory[workflowId] = true;
    if (self.historyRefreshTimer) return;
    self.historyRefreshTimer = setTimeout(function () {
      var pending = self._pendingHistory;
      self._pendingHistory = {};
      self.historyRefreshTimer = null;
      try {
        var wfTable = $("#workflow-history-table");
        if (wfTable.length && wfTable.is(":visible") && pending[wfTable.data("workflow-id")]) {
          GW.history.refreshWorkflowHistoryTable();
        }
        $('[onclick*="refreshHistoryTable("]').each(function () {
          var m = /refreshHistoryTable\('([^']*)',\s*'([^']*)'\)/.exec($(this).attr("onclick") || "");
          if (!m) return;
          var table = $("#" + m[1]);
          if (table.length && table.is(":visible") && pending[table.data("process-id")]) {
            GW.history.refreshHistoryTable(m[1], m[2]);
          }
        });
      } catch (e) {
        console.warn("History refresh failed", e);
      }
    }, 1000);
  },

  /**
   * While the open workflow has a run that has not ended (started here or anywhere else), shows
   * each step's status on the canvas every two seconds; one last update when it ends.
   */
  _watchWorkflowRuns: function () {
    var self = GW.workspaceevents;
    if (self.runWatchTimer) return;
    var tick = function () {
      var wf = GW.workflow && GW.workflow.loaded_workflow;
      if (!wf) {
        self.runWatchTimer = null;
        return;
      }
      $.ajax({
        url: GW.path.getBasePath() + "api/workspace/runs?target=" + encodeURIComponent(wf),
        type: "GET",
        dataType: "json",
      })
        .done(function (runs) {
          var id = runs && runs.length ? runs[runs.length - 1].historyId : null;
          var last = self.runWatchLastId;
          if (id) self.runWatchLastId = id;
          var show = id || last;
          if (show) self._showRunOnCanvas(wf, show);
          if (id) {
            self.runWatchTimer = setTimeout(tick, 2000);
          } else {
            self.runWatchTimer = null;
            self.runWatchLastId = null;
          }
        })
        .fail(function () {
          self.runWatchTimer = null;
        });
    };
    self.runWatchTimer = setTimeout(tick, 50);
  },

  _showRunOnCanvas: function (wf, historyId) {
    $.ajax({
      url: GW.path.getBasePath() + "api/workspace/runs/" + encodeURIComponent(historyId),
      type: "GET",
      dataType: "json",
    }).done(function (run) {
      if (!run || !run.steps || (GW.workflow && GW.workflow.loaded_workflow !== wf)) return;
      if (!GW.workspace || !GW.workspace.theGraph || typeof GW.workspace.updateStatus !== "function") return;
      var list = run.steps.map(function (s) {
        return { id: s.nodeId, history_id: s.historyId, status: s.status };
      });
      try {
        GW.workspace.updateStatus(list);
      } catch (e) {
        console.warn("Could not show run status on the canvas", e);
      }
    });
  },

  /** Opening a workflow also shows a run of it that is going on right now. */
  _hookWorkflowOpen: function () {
    if (!GW.workflow || typeof GW.workflow.showWorkflow !== "function" || GW.workflow._liveRunHooked) {
      return;
    }
    var original = GW.workflow.showWorkflow;
    GW.workflow.showWorkflow = function () {
      var result = original.apply(this, arguments);
      setTimeout(function () {
        GW.workspaceevents._watchWorkflowRuns();
      }, 800);
      return result;
    };
    GW.workflow._liveRunHooked = true;
  },

  shouldAutoReloadWorkflow: function (unsaved) {
    return !unsaved;
  },
};
