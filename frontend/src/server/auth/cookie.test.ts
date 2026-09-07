import { readFileSync } from "node:fs";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { AUTH_SESSION_COOKIE, authCookie, authCookieBase, expiredAuthCookie } from "./cookie";

describe("cookie auth_session server-only", () => {
  beforeEach(() => vi.stubEnv("BACKEND_API_URL", "http://backend:8080"));
  afterEach(() => vi.unstubAllEnvs());

  it.each([
    ["http://localhost:3000", false],
    ["https://app.example.test", true],
  ])("deriva Secure=%s para APP_ORIGIN %s", (origin, expectedSecure) => {
    vi.stubEnv("APP_ORIGIN", origin);
    expect(authCookieBase()).toMatchObject({ httpOnly: true, sameSite: "lax", path: "/", secure: expectedSecure });
  });

  it("cria o cookie com atributos confidenciais, host-only e Max-Age derivado", () => {
    vi.stubEnv("APP_ORIGIN", "https://app.example.test");
    const cookie = authCookie("jwt-only-here", 90.9);
    expect(cookie).toEqual({ name: AUTH_SESSION_COOKIE, value: "jwt-only-here", httpOnly: true, sameSite: "lax", path: "/", secure: true, maxAge: 90 });
    expect(cookie).not.toHaveProperty("domain");
    expect(Object.entries(cookie).filter(([, value]) => value === "jwt-only-here")).toEqual([["value", "jwt-only-here"]]);
  });

  it("expira usando a mesma configuração-base", () => {
    vi.stubEnv("APP_ORIGIN", "http://localhost:3000");
    const created = authCookie("jwt-only-here", 3600);
    const removed = expiredAuthCookie();
    expect(removed).toEqual({ name: AUTH_SESSION_COOKIE, value: "", httpOnly: true, sameSite: "lax", path: "/", secure: false, maxAge: 0 });
    expect(removed).not.toHaveProperty("domain");
    expect(Object.fromEntries(Object.entries(created).filter(([key]) => key !== "value" && key !== "maxAge"))).toEqual(Object.fromEntries(Object.entries(removed).filter(([key]) => key !== "value" && key !== "maxAge")));
  });

  it("não contém mecanismos client-side ou exportação pública", () => {
    const source = readFileSync(join(process.cwd(), "src/server/auth/cookie.ts"), "utf8");
    expect(source).toMatch(/^import "server-only";/);
    expect(source).not.toMatch(/localStorage|sessionStorage|NEXT_PUBLIC|React|Query/i);
  });
});
