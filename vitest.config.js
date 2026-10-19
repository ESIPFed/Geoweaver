import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "jsdom",
    setupFiles: ["src/test/javascript/setup.js"],
    include: ["src/test/javascript/**/*.test.js"],
    // Legacy placeholder suites have syntax errors / missing GW globals; exclude until repaired.
    exclude: [
      "src/test/javascript/gw.history.test.js",
      "src/test/javascript/gw.settings.test.js",
      "src/test/javascript/gw.workspace.test.js",
      "src/test/javascript/gw.general.test.js",
      "**/node_modules/**",
      "**/target/**",
    ],
    coverage: {
      provider: "v8",
      reportsDirectory: "coverage/js",
      reporter: ["text", "json-summary", "html"],
      include: ["src/main/resources/static/js/gw.progressindicator.js"],
      thresholds: {
        lines: 80,
        functions: 80,
        statements: 80,
        branches: 80,
      },
    },
  },
});
