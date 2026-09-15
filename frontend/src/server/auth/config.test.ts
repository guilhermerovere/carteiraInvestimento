import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { loadAuthConfig } from "./config";

describe("configuração server-only de autenticação", () => {
  it("aceita URLs válidas e canonicaliza APP_ORIGIN", () => {
    const config = loadAuthConfig({
      BACKEND_API_URL: "http://backend:8080/base/",
      APP_ORIGIN: "https://APP.Example.test:443/caminho?query=1#hash",
    });
    expect(config.backendApiUrl.toString()).toBe("http://backend:8080/base/");
    expect(config.appOrigin.toString()).toBe("https://app.example.test/");
    expect(config.appOrigin.origin).toBe("https://app.example.test");
  });

  it.each([
    [{ APP_ORIGIN: "https://app.test" }, "BACKEND_API_URL ausente"],
    [{ BACKEND_API_URL: "http://backend:8080" }, "APP_ORIGIN ausente"],
    [{ BACKEND_API_URL: "não-é-url", APP_ORIGIN: "https://app.test" }, "BACKEND_API_URL inválida"],
    [{ BACKEND_API_URL: "http://backend:8080", APP_ORIGIN: "não-é-url" }, "APP_ORIGIN inválida"],
    [{ BACKEND_API_URL: "ftp://backend", APP_ORIGIN: "https://app.test" }, "BACKEND_API_URL com esquema inválido"],
    [{ BACKEND_API_URL: "http://backend:8080", APP_ORIGIN: "ftp://app.test" }, "APP_ORIGIN com esquema inválido"],
  ])("rejeita configuração inválida: %s (%s)", (env, _description) => {
    void _description;
    expect(() => loadAuthConfig(env)).toThrow();
  });

  it("mantém a fronteira server-only explícita", () => {
    const source = readFileSync(join(process.cwd(), "src/server/auth/config.ts"), "utf8");
    expect(source).toMatch(/^import "server-only";/);
    expect(source).not.toContain("NEXT_PUBLIC_");
  });
});
