import { readFileSync } from "node:fs";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { hasValidOrigin } from "./origin";

describe("validação server-only de Origin", () => {
  beforeEach(() => {
    vi.stubEnv("BACKEND_API_URL", "http://backend:8080");
    vi.stubEnv("APP_ORIGIN", "https://APP.example.test:443/configurada?x=1");
  });
  afterEach(() => vi.unstubAllEnvs());

  it("aceita a mesma origem e a canonicalização compatível", () => {
    expect(hasValidOrigin("https://app.example.test")).toBe(true);
    expect(hasValidOrigin("https://APP.EXAMPLE.TEST")).toBe(true);
  });

  it.each([
    ["http://app.example.test", "protocolo"],
    ["https://other.example.test", "host"],
    ["https://app.example.test:8443", "porta"],
    [null, "Origin ausente"],
    ["não-é-url", "Origin malformado"],
    ["https://app.example.test/caminho", "path não permitido"],
    ["https://app.example.test?x=1", "query não permitida"],
    ["https://app.example.test.evil", "hostname parecido"],
  ])("rejeita %s (%s)", (origin, _description) => {
    void _description;
    expect(hasValidOrigin(origin)).toBe(false);
  });

  it("não usa Host como fallback", () => {
    const requestHeaders = new Headers({ Host: "app.example.test", Origin: "https://evil.example.test" });
    expect(hasValidOrigin(requestHeaders.get("origin"))).toBe(false);
  });

  it("mantém o módulo server-only sem APIs de browser", () => {
    const source = readFileSync(join(process.cwd(), "src/server/auth/origin.ts"), "utf8");
    expect(source).toMatch(/^import "server-only";/);
    expect(source).not.toMatch(/window|document|location|localStorage|sessionStorage/);
  });
});
