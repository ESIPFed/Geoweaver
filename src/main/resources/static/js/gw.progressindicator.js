/**
 * Workspace workflow progress panel (idle-hidden; quiet while running).
 * Used by GW.monitor; kept in its own file so unit tests can cover it.
 */
(function (global) {
  "use strict";

  var GW = (global.GW = global.GW || {});

  function $ref() {
    var $ = global.jQuery || global.$;
    if (!$) {
      throw new Error("jQuery is required for GW.progressIndicator");
    }
    return $;
  }

  GW.progressIndicator = {
    STORAGE_KEY: "gw_progress_auto_close",

    /**
     * Whether the panel should hide when a workflow finishes.
     * Missing localStorage value means yes (hide by default).
     */
    shouldAutoClose: function (storage) {
      var store = storage || global.localStorage;
      var stored = store.getItem(GW.progressIndicator.STORAGE_KEY);
      return stored === null || stored === "true";
    },

    /**
     * Ensure new sessions default to hiding the panel after a run.
     */
    ensureDefaultPreference: function (storage) {
      var store = storage || global.localStorage;
      if (store.getItem(GW.progressIndicator.STORAGE_KEY) === null) {
        store.setItem(GW.progressIndicator.STORAGE_KEY, "true");
      }
    },

    clear: function () {
      $ref()("#progress-tasks-container").empty();
    },

    open: function () {
      $ref()("#workspace_progress_indicator")
        .removeClass("invisible")
        .addClass("visible")
        .attr("aria-hidden", "false");
    },

    /**
     * @param {boolean} [force=false] hide even when user opted to keep the summary
     */
    close: function (force) {
      force = !!force;
      if (GW.progressIndicator.shouldAutoClose() || force) {
        $ref()("#workspace_progress_indicator")
          .removeClass("visible")
          .addClass("invisible")
          .attr("aria-hidden", "true");
        GW.progressIndicator.clear();
      }
    },

    resolveTaskName: function (id, workspace) {
      var taskName = "Task";
      var graph = workspace && workspace.theGraph;
      if (graph && graph.nodes) {
        for (var i = 0; i < graph.nodes.length; i++) {
          if (graph.nodes[i].id == id) {
            taskName = graph.nodes[i].title || "Task";
            break;
          }
        }
      }
      return taskName;
    },

    statusVisuals: function (flag) {
      if (flag == "Running") {
        return {
          percent: 30,
          barcolor: "progress-bar-running",
          statusClass: "status-running",
          taskBorder: "task-running",
        };
      }
      if (flag == "Done") {
        return {
          percent: 100,
          barcolor: "progress-bar-done",
          statusClass: "status-done",
          taskBorder: "task-done",
        };
      }
      if (flag == "Failed") {
        return {
          percent: 100,
          barcolor: "progress-bar-failed",
          statusClass: "status-failed",
          taskBorder: "task-failed",
        };
      }
      return {
        percent: 0,
        barcolor: "",
        statusClass: "",
        taskBorder: "",
      };
    },

    update: function (id, flag, workspace) {
      var $ = $ref();
      var visuals = GW.progressIndicator.statusVisuals(flag);
      var taskName = GW.progressIndicator.resolveTaskName(
        id,
        workspace || (global.GW && global.GW.workspace),
      );

      if (!$("#progress-" + id).length) {
        $("#progress-tasks-container").append(
          '<div id="progress-' + id + '" class="progress-task"></div>',
        );
      }

      $("#progress-" + id)
        .attr("class", "progress-task " + visuals.taskBorder)
        .html(
          '<div class="progress-task-header">' +
            '  <span class="task-id" title="' +
            id +
            '">' +
            taskName +
            "</span>" +
            '  <span class="task-status ' +
            visuals.statusClass +
            '">' +
            flag +
            "</span>" +
            "</div>" +
            '<div class="progress">' +
            '  <div class="progress-bar-custom ' +
            visuals.barcolor +
            '" style="width:' +
            visuals.percent +
            '%"></div>' +
            "</div>",
        );
    },
  };
})(typeof globalThis !== "undefined" ? globalThis : this);
