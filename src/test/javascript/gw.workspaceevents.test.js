/**
 * Dirty-buffer policy for agent workspace events (CRITICAL-002).
 * Run with the project's JS test harness if configured; also documents expected behavior.
 */
describe("GW.workspaceevents dirty policy", function () {
  it("does not auto-reload workflow when canvas is unsaved", function () {
    expect(GW.workspaceevents.shouldAutoReloadWorkflow(true)).toBe(false);
  });

  it("may auto-reload workflow when canvas is clean", function () {
    expect(GW.workspaceevents.shouldAutoReloadWorkflow(false)).toBe(true);
  });
});
