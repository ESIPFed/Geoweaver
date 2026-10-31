/**
 * Unit tests for the workflow progress panel module.
 * Coverage gate: vitest.config.js thresholds (≥80%) on gw.progressindicator.js.
 */
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import "../../main/resources/static/js/gw.progressindicator.js";

function mountPanelDom() {
  document.body.innerHTML = `
    <div id="workspace_progress_indicator" class="workspace-progress-container invisible" aria-hidden="true">
      <div id="progress-tasks-container"></div>
    </div>
  `;
}

describe("GW.progressIndicator", () => {
  beforeEach(() => {
    mountPanelDom();
    localStorage.clear();
    GW.workspace = undefined;
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe("shouldAutoClose / ensureDefaultPreference", () => {
    it("treats missing preference as auto-close on", () => {
      expect(GW.progressIndicator.shouldAutoClose()).toBe(true);
    });

    it("respects explicit true", () => {
      localStorage.setItem("gw_progress_auto_close", "true");
      expect(GW.progressIndicator.shouldAutoClose()).toBe(true);
    });

    it("respects explicit false (keep summary)", () => {
      localStorage.setItem("gw_progress_auto_close", "false");
      expect(GW.progressIndicator.shouldAutoClose()).toBe(false);
    });

    it("writes default true when preference is absent", () => {
      GW.progressIndicator.ensureDefaultPreference();
      expect(localStorage.getItem("gw_progress_auto_close")).toBe("true");
    });

    it("does not overwrite an existing false preference", () => {
      localStorage.setItem("gw_progress_auto_close", "false");
      GW.progressIndicator.ensureDefaultPreference();
      expect(localStorage.getItem("gw_progress_auto_close")).toBe("false");
    });

    it("accepts an injected storage object", () => {
      const mem = {
        data: {},
        getItem(k) {
          return this.data[k] ?? null;
        },
        setItem(k, v) {
          this.data[k] = String(v);
        },
      };
      expect(GW.progressIndicator.shouldAutoClose(mem)).toBe(true);
      GW.progressIndicator.ensureDefaultPreference(mem);
      expect(mem.getItem("gw_progress_auto_close")).toBe("true");
      expect(GW.progressIndicator.shouldAutoClose(mem)).toBe(true);
    });
  });

  describe("open / close / clear", () => {
    it("opens the panel and clears aria-hidden", () => {
      GW.progressIndicator.open();
      const el = document.getElementById("workspace_progress_indicator");
      expect(el.classList.contains("visible")).toBe(true);
      expect(el.classList.contains("invisible")).toBe(false);
      expect(el.getAttribute("aria-hidden")).toBe("false");
    });

    it("closes and empties tasks when preference is default", () => {
      GW.progressIndicator.open();
      document.getElementById("progress-tasks-container").innerHTML =
        "<div class='progress-task'>x</div>";
      GW.progressIndicator.close();
      const el = document.getElementById("workspace_progress_indicator");
      expect(el.classList.contains("invisible")).toBe(true);
      expect(el.getAttribute("aria-hidden")).toBe("true");
      expect(document.getElementById("progress-tasks-container").innerHTML).toBe(
        "",
      );
    });

    it("keeps the panel open when user opted out, unless force", () => {
      localStorage.setItem("gw_progress_auto_close", "false");
      GW.progressIndicator.open();
      document.getElementById("progress-tasks-container").innerHTML =
        "<div id='keep'>k</div>";
      GW.progressIndicator.close(false);
      expect(
        document
          .getElementById("workspace_progress_indicator")
          .classList.contains("visible"),
      ).toBe(true);
      expect(document.getElementById("keep")).not.toBeNull();

      GW.progressIndicator.close(true);
      expect(
        document
          .getElementById("workspace_progress_indicator")
          .classList.contains("invisible"),
      ).toBe(true);
      expect(document.getElementById("progress-tasks-container").innerHTML).toBe(
        "",
      );
    });

    it("clear empties the task list without an empty-state banner", () => {
      document.getElementById("progress-tasks-container").innerHTML =
        "<div>task</div><div class='progress-empty-message'>No active tasks</div>";
      GW.progressIndicator.clear();
      expect(document.getElementById("progress-tasks-container").innerHTML).toBe(
        "",
      );
    });
  });

  describe("statusVisuals / resolveTaskName", () => {
    it("maps Running / Done / Failed / unknown", () => {
      expect(GW.progressIndicator.statusVisuals("Running").percent).toBe(30);
      expect(GW.progressIndicator.statusVisuals("Running").taskBorder).toBe(
        "task-running",
      );
      expect(GW.progressIndicator.statusVisuals("Done").percent).toBe(100);
      expect(GW.progressIndicator.statusVisuals("Done").barcolor).toBe(
        "progress-bar-done",
      );
      expect(GW.progressIndicator.statusVisuals("Failed").statusClass).toBe(
        "status-failed",
      );
      expect(GW.progressIndicator.statusVisuals("Queued").percent).toBe(0);
      expect(GW.progressIndicator.statusVisuals("Queued").taskBorder).toBe("");
    });

    it("resolves titles from the workspace graph", () => {
      const workspace = {
        theGraph: {
          nodes: [
            { id: "a-n0001", title: "discover" },
            { id: "b-n0002", title: "" },
          ],
        },
      };
      expect(GW.progressIndicator.resolveTaskName("a-n0001", workspace)).toBe(
        "discover",
      );
      expect(GW.progressIndicator.resolveTaskName("b-n0002", workspace)).toBe(
        "Task",
      );
      expect(GW.progressIndicator.resolveTaskName("missing", workspace)).toBe(
        "Task",
      );
      expect(GW.progressIndicator.resolveTaskName("x", null)).toBe("Task");
    });
  });

  describe("update", () => {
    it("creates a compact running row and updates to Done", () => {
      GW.workspace = {
        theGraph: { nodes: [{ id: "p1-n0001", title: "step-a" }] },
      };
      GW.progressIndicator.update("p1-n0001", "Running");
      const row = document.getElementById("progress-p1-n0001");
      expect(row).not.toBeNull();
      expect(row.classList.contains("task-running")).toBe(true);
      expect(row.textContent).toContain("step-a");
      expect(row.textContent).toContain("Running");
      expect(row.querySelector(".progress-bar-running")).not.toBeNull();
      expect(row.querySelector(".progress-percentage")).toBeNull();

      GW.progressIndicator.update("p1-n0001", "Running"); // idempotent create
      expect(document.querySelectorAll("#progress-p1-n0001").length).toBe(1);

      GW.progressIndicator.update("p1-n0001", "Done");
      expect(row.classList.contains("task-done")).toBe(true);
      expect(row.querySelector(".progress-bar-done").style.width).toBe("100%");
    });

    it("renders Failed with failed styling", () => {
      GW.progressIndicator.update("fail-1", "Failed");
      const row = document.getElementById("progress-fail-1");
      expect(row.classList.contains("task-failed")).toBe(true);
      expect(row.querySelector(".status-failed").textContent).toBe("Failed");
    });

    it("handles unknown flags without throwing", () => {
      GW.progressIndicator.update("u-1", "Queued");
      const row = document.getElementById("progress-u-1");
      expect(row).not.toBeNull();
      expect(row.querySelector(".progress-bar-custom").style.width).toBe("0%");
    });
  });

  describe("monitor delegation contract", () => {
    it("exposes the same surface GW.monitor delegates to", () => {
      expect(typeof GW.progressIndicator.clear).toBe("function");
      expect(typeof GW.progressIndicator.open).toBe("function");
      expect(typeof GW.progressIndicator.close).toBe("function");
      expect(typeof GW.progressIndicator.update).toBe("function");
    });

    it("throws a clear error when jQuery is missing", () => {
      const saved$ = globalThis.$;
      const savedJq = globalThis.jQuery;
      try {
        globalThis.$ = undefined;
        globalThis.jQuery = undefined;
        expect(() => GW.progressIndicator.clear()).toThrow(/jQuery is required/);
      } finally {
        globalThis.$ = saved$;
        globalThis.jQuery = savedJq;
      }
    });
  });
});

describe("progress panel static assets", () => {
  it("CSS hides idle panel and uses soft pulse (not stripes)", async () => {
    const fs = await import("node:fs");
    const path = await import("node:path");
    const cssPath = path.resolve(
      "src/main/resources/static/css/progress-indicator.css",
    );
    const css = fs.readFileSync(cssPath, "utf8");
    expect(css).toContain(".workspace-progress-container.invisible");
    expect(css).toContain("display: none");
    expect(css).toContain("progress-bar-soft-pulse");
    expect(css).not.toContain("progress-bar-stripes");
  });

  it("workspace fragment starts with an invisible empty panel", async () => {
    const fs = await import("node:fs");
    const path = await import("node:path");
    const html = fs.readFileSync(
      path.resolve(
        "src/main/resources/templates/fragments/content/workspace.html",
      ),
      "utf8",
    );
    expect(html).toContain("workspace-progress-container invisible");
    expect(html).not.toContain("No active tasks");
    expect(html).toContain('aria-hidden="true"');
  });

  it("script tags load progressindicator before monitor", async () => {
    const fs = await import("node:fs");
    const path = await import("node:path");
    const scripts = fs.readFileSync(
      path.resolve(
        "src/main/resources/templates/fragments/config/scripts.html",
      ),
      "utf8",
    );
    const pi = scripts.indexOf("gw.progressindicator.js");
    const mon = scripts.indexOf("gw.monitor.js");
    expect(pi).toBeGreaterThan(-1);
    expect(mon).toBeGreaterThan(pi);
  });
});
