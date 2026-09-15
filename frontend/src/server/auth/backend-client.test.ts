import { readFileSync } from "node:fs";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { backendClient } from "./backend-client";
import { BackendHttpError, BackendNetworkError } from "./types";

const fetchMock = vi.fn<typeof fetch>();

describe("cliente Spring server-only", () => {
  beforeEach(() => {
    vi.stubEnv("BACKEND_API_URL", "http://spring.internal:8080");
    vi.stubEnv("APP_ORIGIN", "https://app.test");
    vi.stubGlobal("fetch", fetchMock);
    fetchMock.mockReset();
  });

  afterEach(() => {
    vi.unstubAllEnvs();
    vi.unstubAllGlobals();
  });

  it("faz login no path correto por POST sem Authorization", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ accessToken: "server-token", tokenType: "Bearer", expiresIn: 3600 }), { status: 200 }));
    await expect(backendClient.login({ email: "user@example.test", senha: "Password1!" })).resolves.toEqual({ accessToken: "server-token", tokenType: "Bearer", expiresIn: 3600 });
    const [url, init] = fetchMock.mock.calls[0];
    const headers = new Headers(init?.headers);
    expect(url).toBe("http://spring.internal:8080/api/v1/auth/login");
    expect(init).toMatchObject({ method: "POST", cache: "no-store" });
    expect(headers.get("accept")).toBe("application/json");
    expect(headers.get("content-type")).toBe("application/json");
    expect(headers.has("authorization")).toBe(false);
    expect(init?.body).toBe(JSON.stringify({ email: "user@example.test", senha: "Password1!" }));
  });

  it("faz cadastro no path correto por POST sem Bearer", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ id: "user-id" }), { status: 201 }));
    await backendClient.register({ nome: "Usuário", email: "user@example.test", senha: "Password1!" });
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("http://spring.internal:8080/api/v1/auth/register");
    expect(init?.method).toBe("POST");
    expect(new Headers(init?.headers).has("authorization")).toBe(false);
    expect(init?.body).toBe(JSON.stringify({ nome: "Usuário", email: "user@example.test", senha: "Password1!" }));
  });

  it("rejeita resposta de login sem o contrato esperado", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ accessToken: "server-token" }), { status: 200 }));
    const error = await backendClient.login({ email: "user@example.test", senha: "Password1!" }).catch((cause: unknown) => cause);
    expect(error).toMatchObject({ status: 502, problem: { status: 502, title: "Resposta inválida do serviço de autenticação" } });
    expect(JSON.stringify(error)).not.toContain("server-token");
  });

  it("consulta me por GET com Bearer recebido explicitamente", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ id: "1", nome: "User", email: "user@example.test", role: "ROLE_USER", ativo: true }), { status: 200 }));
    await backendClient.me("explicit-token");
    const [url, init] = fetchMock.mock.calls[0];
    const headers = new Headers(init?.headers);
    expect(url).toBe("http://spring.internal:8080/api/v1/auth/me");
    expect(init?.method).toBe("GET");
    expect(headers.get("authorization")).toBe("Bearer explicit-token");
    expect(headers.get("accept")).toBe("application/json");
    expect(headers.has("content-type")).toBe(false);
  });

  it("interpreta ProblemDetail sem manter token e preserva correlation ID", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 401, title: "Unauthorized", detail: "Expirado", instance: "/api/v1/auth/me", accessToken: "leak" }), {
      status: 401,
      headers: { "Content-Type": "application/problem+json", "X-Correlation-ID": "corr-upstream", Authorization: "Bearer upstream-header-secret" },
    }));
    const error = await backendClient.me("explicit-token").catch((cause: unknown) => cause);
    expect(error).toBeInstanceOf(BackendHttpError);
    expect(error).toMatchObject({ status: 401, kind: "problem-detail", correlationId: "corr-upstream", problem: { status: 401, title: "Unauthorized", detail: "Expirado", instance: "/api/v1/auth/me" } });
    expect(JSON.stringify(error)).not.toMatch(/explicit-token|upstream-header-secret|leak|Authorization/i);
  });

  it("classifica resposta não ProblemDetail como erro HTTP comum", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ error: "internal", accessToken: "leak" }), { status: 500, headers: { "Content-Type": "application/json" } }));
    const error = await backendClient.login({ email: "user@example.test", senha: "Password1!" }).catch((cause: unknown) => cause);
    expect(error).toMatchObject({ status: 500, kind: "http", problem: { status: 500, title: "Erro HTTP no serviço de autenticação" } });
    expect(JSON.stringify(error)).not.toContain("leak");
  });

  it("classifica fetch rejeitado sem vazar URL, token ou Authorization", async () => {
    fetchMock.mockRejectedValue(new Error("ECONNREFUSED http://spring.internal Authorization: Bearer explicit-token"));
    const error = await backendClient.me("explicit-token").catch((cause: unknown) => cause);
    expect(error).toBeInstanceOf(BackendNetworkError);
    expect((error as Error).message).toBe("Serviço de autenticação indisponível");
    expect(JSON.stringify(error)).not.toMatch(/spring\.internal|explicit-token|Authorization/i);
  });

  it("não exporta headers upstream e mantém o módulo server-only", () => {
    expect(Object.keys(backendClient)).toEqual(["login", "register", "me"]);
    const source = readFileSync(join(process.cwd(), "src/server/auth/backend-client.ts"), "utf8");
    expect(source).toMatch(/^import "server-only";/);
  });
});
