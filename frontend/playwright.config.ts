import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  use: {
    baseURL: process.env.APP_ORIGIN ?? "http://127.0.0.1:3000",
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
      command: "npm.cmd run dev",
      url: "http://127.0.0.1:3000",
      env: {
        BACKEND_API_URL: "http://127.0.0.1:18080",
        APP_ORIGIN: "http://127.0.0.1:3000",
      },
      reuseExistingServer: true,
      stdout: "ignore",
      stderr: "ignore",
    },
  ],
});
