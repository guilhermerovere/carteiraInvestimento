import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { BackendHttpError, BackendNetworkError } from "@/server/auth/types";

const { resolveMock } = vi.hoisted(() => ({ resolveMock: vi.fn() }));
vi.mock("@/server/auth/current-user", () => ({ resolveCurrentUserFromSessionCookie: resolveMock }));

import { GET } from "./route";

describe("GET /api/auth/me", () => {
  beforeEach(() => {
    vi.stubEnv("BACKEND_API_URL", "http://spring.internal:8080");
    vi.stubEnv("APP_ORIGIN", "https://app.test");
    resolveMock.mockReset();
  });
  afterEach(() => vi.unstubAllEnvs());

  it("não inventa usuário nem chama Spring quando não há cookie", async () => {
    resolveMock.mockResolvedValue(null);
    const response = await GET();
    expect(response.status).toBe(401);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(response.headers.get("set-cookie")).toBeNull();
  });

  it("retorna somente o usuário seguro com no-store", async () => {
    resolveMock.mockResolvedValue({ id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER", ativo: true });
    const response = await GET();
    expect(response.status).toBe(200);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(response.headers.get("authorization")).toBeNull();
    expect(await response.json()).toEqual({ id: "u1", nome: "Usuário", email: "user@example.test", role: "ROLE_USER", ativo: true });
  });

  it("remove auth_session somente quando upstream devolve 401", async () => {
    resolveMock.mockRejectedValue(new BackendHttpError(401, { status: 401, title: "Não autenticado" }, "corr-401", "problem-detail"));
    const response = await GET();
    expect(response.status).toBe(401);
    expect(response.headers.get("x-correlation-id")).toBe("corr-401");
    const setCookie = response.headers.get("set-cookie") ?? "";
    expect(setCookie).toMatch(/auth_session=; Path=\//i);
    expect(setCookie).toMatch(/Max-Age=0; Secure; HttpOnly; SameSite=Lax/i);
  });

  it("preserva sessão em 403 e em falha de rede", async () => {
    resolveMock.mockRejectedValueOnce(new BackendHttpError(403, { status: 403, title: "Proibido" }, "corr-403", "problem-detail"));
    const forbidden = await GET();
    expect(forbidden.status).toBe(403);
    expect(forbidden.headers.get("x-correlation-id")).toBe("corr-403");
    expect(forbidden.headers.get("set-cookie")).toBeNull();
    resolveMock.mockRejectedValueOnce(new BackendNetworkError());
    const unavailable = await GET();
    expect(unavailable.status).toBe(503);
    expect(unavailable.headers.get("set-cookie")).toBeNull();
  });
});
