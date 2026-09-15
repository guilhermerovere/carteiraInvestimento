import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  testMatch: "**/*.real.spec.ts",
  fullyParallel: false,
  workers: 1,
  timeout: 120_000,
  use: {
    baseURL: process.env.PLAYWRIGHT_APP_ORIGIN ?? "http://127.0.0.1:3101",
    trace: "retain-on-failure",
  },
  webServer: [{
    command: `node node_modules/next/dist/bin/next dev --hostname 127.0.0.1 --port ${new URL(process.env.PLAYWRIGHT_APP_ORIGIN ?? "http://127.0.0.1:3101").port || "3101"}`,
    url: `${process.env.PLAYWRIGHT_APP_ORIGIN ?? "http://127.0.0.1:3101"}/login`,
    env: {
      BACKEND_API_URL: process.env.BACKEND_API_URL ?? "http://127.0.0.1:8080",
      APP_ORIGIN: process.env.PLAYWRIGHT_APP_ORIGIN ?? "http://127.0.0.1:3101",
    },
    reuseExistingServer: false,
    timeout: 120_000,
    stdout: "ignore",
    stderr: "pipe",
  }],
});
