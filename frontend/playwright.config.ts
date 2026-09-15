import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  testIgnore: "**/*.real.spec.ts",
  fullyParallel: false,
  workers: 1,
  use: {
    baseURL: process.env.PLAYWRIGHT_APP_ORIGIN ?? "http://localhost:3100",
    trace: "retain-on-failure",
  },
  webServer: [
    {
      command: "node e2e/mock-backend.mjs",
      url: "http://127.0.0.1:18080/health",
      reuseExistingServer: true,
      stdout: "ignore",
      stderr: "ignore",
    },
    {
      command: "node node_modules/next/dist/bin/next dev --port 3100",
      url: "http://localhost:3100",
      env: {
        BACKEND_API_URL: "http://127.0.0.1:18080",
        APP_ORIGIN: "http://localhost:3100",
      },
      reuseExistingServer: true,
      stdout: "ignore",
      stderr: "ignore",
    },
  ],
});
