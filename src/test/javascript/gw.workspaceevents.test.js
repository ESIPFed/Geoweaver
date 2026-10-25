/**
 * Dirty-buffer policy for agent workspace events.
 */
import { describe, it, expect, beforeAll } from "vitest";

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
