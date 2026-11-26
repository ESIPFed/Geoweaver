/**
 * Dirty-buffer policy for agent workspace events.
 */
import { describe, it, expect, beforeAll, beforeEach, afterEach, vi } from "vitest";

beforeAll(async () => {
  // Minimal stub if the production script is not loaded as a module
  if (!globalThis.GW?.workspaceevents?.shouldAutoReloadWorkflow) {
    const mod = await import(
      "../../main/resources/static/js/gw.workspaceevents.js"
    ).catch(() => null);
    if (!globalThis.GW?.workspaceevents && mod?.default) {
      // ignore — file is classic script
    }
  }
  if (!globalThis.GW?.workspaceevents) {
    // Load classic script side effects via dynamic import (vite executes IIFE)
    await import("../../main/resources/static/js/gw.workspaceevents.js");
  }
});

describe("GW.workspaceevents dirty policy", () => {
  it("does not auto-reload workflow when canvas is unsaved", () => {
    expect(GW.workspaceevents.shouldAutoReloadWorkflow(true)).toBe(false);
  });

  it("may auto-reload workflow when canvas is clean", () => {
    expect(GW.workspaceevents.shouldAutoReloadWorkflow(false)).toBe(true);
  });
});

describe("GW.workspaceevents presence note", () => {
  const strip = () => document.getElementById("gw-agent-presence-strip");
  const shown = () => !!strip() && strip().style.display !== "none";

  beforeEach(() => {
    document.body.innerHTML = '<div id="main-content-area"></div>';
    GW.workflow = { loaded_workflow: "w1" };
    GW.process = {};
    GW.workspaceevents.presenceByKey = {};
    GW.workspaceevents.presenceDismissed = {};
  });

  it("never announces a person, including this browser", () => {
    GW.workspaceevents._mergePresenceSnapshot({ "workflow:w1": { actor: "human", ageMs: 1000 } });
    GW.workspaceevents._notePresence({ objectKind: "workflow", objectId: "w1", actor: "human" });
    expect(shown()).toBe(false);
  });

  it("shows an agent change to the open workflow, and not to another one", () => {
    GW.workspaceevents._mergePresenceSnapshot({ "workflow:w2": { actor: "agent", ageMs: 1000 } });
    expect(shown()).toBe(false);
    GW.workspaceevents._notePresence({ objectKind: "workflow", objectId: "w1", actor: "agent" });
    expect(shown()).toBe(true);
    expect(strip().textContent).toContain("An AI agent changed this workflow");
  });

  it("is not kept alive by repeated status checks and hides on its own", () => {
    GW.workspaceevents._mergePresenceSnapshot({ "workflow:w1": { actor: "agent", ageMs: 3 * 60 * 1000 } });
    expect(shown()).toBe(false);
    GW.workspaceevents._mergePresenceSnapshot({ "workflow:w1": { actor: "agent", ageMs: 1000 } });
    expect(shown()).toBe(true);
    GW.workspaceevents.presenceByKey["workflow:w1"].at -= 3 * 60 * 1000;
    GW.workspaceevents._mergePresenceSnapshot({ "workflow:w1": { actor: "agent", ageMs: 3 * 60 * 1000 } });
    expect(shown()).toBe(false);
  });

  it("can be closed, and comes back only for a newer change", () => {
    GW.workspaceevents._notePresence({ objectKind: "workflow", objectId: "w1", actor: "agent" });
    strip().querySelector("button").click();
    expect(shown()).toBe(false);
    GW.workspaceevents._renderPresenceStrip();
    expect(shown()).toBe(false);
    GW.workspaceevents.presenceByKey["workflow:w1"].at += 1;
    GW.workspaceevents._renderPresenceStrip();
    expect(shown()).toBe(true);
  });
});

describe("GW.workspaceevents own saves", () => {
  it("treats a person's event right after this page saved as its own echo", () => {
    GW.workspaceevents.ownSaveAt = { workflow: Date.now() };
    expect(GW.workspaceevents._isOwnEcho({ actor: "human" }, "workflow")).toBe(true);
    expect(GW.workspaceevents._isOwnEcho({ actor: "agent" }, "workflow")).toBe(false);
    expect(GW.workspaceevents._isOwnEcho({ actor: "human" }, "process")).toBe(false);
  });

  it("does not reload or warn when the open workflow's change is this page's own save", () => {
    let reloaded = 0;
    GW.workflow = { loaded_workflow: "w1", showWorkflow: () => reloaded++ };
    GW.workspace = { workflow_unsaved: false };
    GW.workspaceevents.ownSaveAt = { workflow: Date.now() };
    GW.workspaceevents._handleOpenObject({ objectKind: "workflow", objectId: "w1", actor: "human" });
    expect(reloaded).toBe(0);
    GW.workspaceevents._handleOpenObject({ objectKind: "workflow", objectId: "w1", actor: "agent" });
    expect(reloaded).toBe(1);
  });
});

describe("GW.workspaceevents long poll", () => {
  let delays;
  let status;
  let body;
  beforeEach(() => {
    delays = [];
    vi.spyOn(globalThis, "setTimeout").mockImplementation((fn, ms) => delays.push(ms));
    globalThis.XMLHttpRequest = function () {
      const x = this;
      x.open = () => {};
      x.send = () => {
        x.status = status;
        x.responseText = body;
        x.onload();
      };
    };
    GW.general = { CLIENT_TOKEN: "t" };
    GW.path = { getBasePath: () => "/" };
    GW.workspaceevents.isPolling = true;
    GW.workspaceevents.pollFailures = 0;
  });
  afterEach(() => vi.restoreAllMocks());

  it("polls again at once after an empty answer", () => {
    status = 204;
    GW.workspaceevents._pollOnce();
    expect(delays).toEqual([50]);
  });

  it("backs off instead of spinning when the answer is a login page or an error", () => {
    status = 200;
    body = "<html>login</html>";
    for (let i = 0; i < 8; i++) GW.workspaceevents._pollOnce();
    expect(delays).toEqual([1000, 2000, 4000, 8000, 16000, 32000, 32000, 32000]);
    status = 500;
    GW.workspaceevents._pollOnce();
    expect(delays.at(-1)).toBe(32000);
    status = 204;
    GW.workspaceevents._pollOnce();
    expect(delays.at(-1)).toBe(50);
    expect(GW.workspaceevents.pollFailures).toBe(0);
  });
});
