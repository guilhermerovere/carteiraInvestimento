import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { BackendHttpError, BackendNetworkError } from "@/server/auth/types";

const { loginMock, originMock } = vi.hoisted(() => ({ loginMock: vi.fn(), originMock: vi.fn() }));
vi.mock("@/server/auth/backend-client", () => ({ backendClient: { login: loginMock } }));
vi.mock("@/server/auth/origin", () => ({ hasValidOrigin: originMock }));

import { POST } from "./route";

const request = (origin: string | undefined, body: unknown = { email: "user@example.test", senha: "Password1!" }) =>
  new Request("http://app.test/api/auth/login", { method: "POST", headers: { "Content-Type": "application/json", ...(origin ? { Origin: origin } : {}) }, body: JSON.stringify(body) });

describe("POST /api/auth/login", () => {
  beforeEach(() => {
    vi.stubEnv("BACKEND_API_URL", "http://spring.internal:8080");
    vi.stubEnv("APP_ORIGIN", "https://app.test");
    loginMock.mockReset(); originMock.mockReset(); originMock.mockReturnValue(true);
  });
  afterEach(() => vi.unstubAllEnvs());

  it("encaminha somente o contrato de login e cria a sessão em 204 no-store", async () => {
    loginMock.mockResolvedValue({ accessToken: "jwt-server-only", tokenType: "Bearer", expiresIn: 3600 });
    const response = await POST(request("https://app.test", { email: "user@example.test", senha: "Password1!", ignored: "not-forwarded" }));
    expect(originMock).toHaveBeenCalledWith("https://app.test");
    expect(loginMock).toHaveBeenCalledWith({ email: "user@example.test", senha: "Password1!" });
    expect(response.status).toBe(204);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(await response.text()).toBe("");
    expect(response.headers.get("authorization")).toBeNull();
    expect(response.headers.get("tokenType")).toBeNull();
    const setCookie = response.headers.get("set-cookie") ?? "";
    expect(setCookie).toMatch(/auth_session=jwt-server-only; Path=\//i);
    expect(setCookie).toMatch(/Max-Age=3600/i);
    expect(setCookie).toMatch(/Secure; HttpOnly; SameSite=Lax/i);
  });

  it.each([undefined, "https://evil.test"]) ("rejeita Origin %s sem chamar upstream ou criar sessão", async (origin) => {
    originMock.mockReturnValue(false);
    const response = await POST(request(origin));
    expect(response.status).toBe(403);
    expect(response.headers.get("set-cookie")).toBeNull();
    expect(loginMock).not.toHaveBeenCalled();
  });

  it("preserva ProblemDetail e correlation ID sem expor token", async () => {
    loginMock.mockRejectedValue(new BackendHttpError(401, { status: 401, title: "Não autorizado", detail: "Credenciais inválidas", instance: "/api/v1/auth/login" }, "corr-login", "problem-detail"));
    const response = await POST(request("https://app.test"));
    expect(response.status).toBe(401);
    expect(response.headers.get("x-correlation-id")).toBe("corr-login");
    expect(await response.json()).toEqual({ status: 401, title: "Não autorizado", detail: "Credenciais inválidas", instance: "/api/v1/auth/login" });
    expect(response.headers.get("authorization")).toBeNull();
  });

  it("converte falha de rede em resposta segura", async () => {
    loginMock.mockRejectedValue(new BackendNetworkError());
    const response = await POST(request("https://app.test"));
    expect(response.status).toBe(503);
    expect(await response.json()).toEqual({ status: 503, title: "Serviço indisponível" });
  });

  it("normaliza erro HTTP comum sem detalhes upstream", async () => {
    loginMock.mockRejectedValue(new BackendHttpError(502, { status: 502, title: "Erro HTTP no serviço de autenticação" }, "corr-http", "http"));
    const response = await POST(request("https://app.test"));
    expect(response.status).toBe(502);
    expect(response.headers.get("x-correlation-id")).toBe("corr-http");
    expect(await response.json()).toEqual({ status: 502, title: "Erro HTTP no serviço de autenticação", detail: undefined, instance: undefined });
  });
});
