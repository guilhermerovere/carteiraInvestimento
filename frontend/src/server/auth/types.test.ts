import { describe, expect, expectTypeOf, it } from "vitest";
import { BackendHttpError, BackendNetworkError, parseBackendError, safeProblem, type CurrentUser, type LoginInput, type LoginResult, type ProblemDetail, type RegisteredUser, type RegisterInput } from "./types";

const sensitivePayload = {
  accessToken: "access-secret",
  refreshToken: "refresh-secret",
  Authorization: "Bearer authorization-secret",
  senha: "password-secret",
  credential: "credential-secret",
  headers: { server: "internal" },
};

describe("parsing seguro de erros do backend", () => {
  it("define contratos explícitos para usuário, login, cadastro e ProblemDetail", () => {
    expectTypeOf<CurrentUser>().toEqualTypeOf<{ id: string; nome: string; email: string; role: "ROLE_USER" | "ROLE_ADMIN"; ativo: boolean }>();
    expectTypeOf<RegisteredUser>().toEqualTypeOf<CurrentUser>();
    expectTypeOf<LoginInput>().toEqualTypeOf<{ email: string; senha: string }>();
    expectTypeOf<RegisterInput>().toEqualTypeOf<{ nome: string; email: string; senha: string }>();
    expectTypeOf<LoginResult>().toEqualTypeOf<{ accessToken: string; tokenType: string; expiresIn: number }>();
    expectTypeOf<ProblemDetail>().toEqualTypeOf<{ status?: number; title?: string; detail?: string; instance?: string }>();
  });

  it("preserva somente campos seguros de ProblemDetail e correlation ID", async () => {
    const error = await parseBackendError(new Response(JSON.stringify({ status: 401, title: "Não autenticado", detail: "Sessão expirada", instance: "/api/v1/auth/me", ...sensitivePayload }), {
      status: 401,
      headers: { "Content-Type": "application/problem+json", "X-Correlation-ID": "corr-123" },
    }));
    expect(error).toBeInstanceOf(BackendHttpError);
    expect(error.kind).toBe("problem-detail");
    expect(error.problem).toEqual({ status: 401, title: "Não autenticado", detail: "Sessão expirada", instance: "/api/v1/auth/me" });
    expect(error.correlationId).toBe("corr-123");
    expect(JSON.stringify(error)).not.toMatch(/access-secret|refresh-secret|authorization-secret|password-secret|credential-secret|internal/);
  });

  it("aceita ProblemDetail parcial com fallback de status", async () => {
    const error = await parseBackendError(new Response(JSON.stringify({ title: "Inválido" }), { status: 422, headers: { "Content-Type": "application/problem+json; charset=utf-8" } }));
    expect(error.problem).toEqual({ status: 422, title: "Inválido", detail: undefined, instance: undefined });
  });

  it.each([
    ["content-type comum", new Response(JSON.stringify(sensitivePayload), { status: 500, headers: { "Content-Type": "application/json" } })],
    ["corpo inválido", new Response("{", { status: 502, headers: { "Content-Type": "application/problem+json" } })],
    ["resposta sem corpo", new Response(null, { status: 503, headers: { "Content-Type": "application/problem+json" } })],
  ])("classifica %s como erro HTTP comum", async (_label, response) => {
    const error = await parseBackendError(response);
    expect(error.kind).toBe("http");
    expect(error.problem).toEqual({ status: response.status, title: "Erro HTTP no serviço de autenticação" });
    expect(JSON.stringify(error)).not.toMatch(/access-secret|refresh-secret|authorization-secret|password-secret|credential-secret|internal/);
  });

  it("sanitiza conteúdo sensível dentro de campos permitidos", () => {
    const problem = safeProblem({ status: 401, detail: "Authorization: Bearer jwt-secret; senha=guess-me; password=guess-me; hash=hash-secret; cookie=auth_session=jwt; http://spring.internal:8080/api" }, 500);
    expect(problem.detail).not.toContain("jwt-secret");
    expect(problem.detail).not.toContain("guess-me");
    expect(problem.detail).not.toMatch(/hash-secret|auth_session|spring\.internal/);
  });

  it("representa falha de infraestrutura sem causa, URL ou credenciais", () => {
    const error = new BackendNetworkError();
    expect(error.kind).toBe("network");
    expect(error.message).toBe("Serviço de autenticação indisponível");
    expect(JSON.stringify(error)).not.toMatch(/https?:|Bearer|token|Authorization/i);
  });
});
