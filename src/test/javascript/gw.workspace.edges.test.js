/**
 * Edge styling in the workflow canvas: straight edges must not use the gradient or the shadow
 * filter, because SVG draws nothing for those on a zero-width or zero-height path.
 */
import { describe, it, expect, beforeAll } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

beforeAll(() => {
  const src = readFileSync(
    resolve(process.cwd(), "src/main/resources/static/js/gw.workspace.js"),
    "utf8",
  );
  const start = src.indexOf("GW.workspace.isStraightEdge = function");
  const end = src.indexOf("// Get status style class");
  expect(start).toBeGreaterThan(-1);
  expect(end).toBeGreaterThan(start);
  globalThis.GW.workspace = {};
  new Function("GW", src.slice(start, end))(globalThis.GW);
});

const edge = (x1, y1, x2, y2) => ({ source: { x: x1, y: y1 }, target: { x: x2, y: y2 } });

describe("GW.workspace edge style", () => {
  it("uses a plain stroke and no filter for a vertical edge", () => {
    expect(GW.workspace.edgeStroke(edge(460, 40, 460, 300))).toBe("#7d8288");
    expect(GW.workspace.edgeFilter(edge(460, 40, 460, 300))).toBe("none");
  });

  it("uses a plain stroke and no filter for a horizontal edge", () => {
    expect(GW.workspace.edgeStroke(edge(0, 100, 300, 100))).toBe("#7d8288");
    expect(GW.workspace.edgeFilter(edge(0, 100, 300, 100))).toBe("none");
  });

  it("keeps the gradient and shadow for a diagonal edge", () => {
    expect(GW.workspace.edgeStroke(edge(0, 40, 230, 170))).toBe("url(#edge-gradient)");
    expect(GW.workspace.edgeFilter(edge(0, 40, 230, 170))).toBe("url(#edge-shadow)");
  });

  it("falls back to the gradient when an edge has no positions yet", () => {
    expect(GW.workspace.edgeStroke(undefined)).toBe("url(#edge-gradient)");
    expect(GW.workspace.edgeStroke({ source: {} })).toBe("url(#edge-gradient)");
  });
});
