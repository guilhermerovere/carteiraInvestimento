import { describe, expect, it } from "vitest";
import { authErrorResponse } from "./route-error";
import { BackendHttpError, BackendNetworkError } from "./types";

describe("respostas seguras de erro dos Route Handlers", () => {
  it("preserva ProblemDetail e correlation ID, removendo segredos e URL interna", async () => {
    const response = authErrorResponse(new BackendHttpError(401, {
      status: 401,
      title: "Não autenticado",
      detail: "accessToken=jwt-secret; refreshToken=refresh-secret; Authorization: Bearer bearer-secret; senha=secret; password=secret; hash=secret; cookie=auth_session=jwt-secret; http://spring.internal:8080/api",
      instance: "/api/v1/auth/me",
    }, "corr-safe", "problem-detail"));
    const body = await response.text();
    expect(response.status).toBe(401);
    expect(response.headers.get("x-correlation-id")).toBe("corr-safe");
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(body).toContain("Não autenticado");
    expect(body).toContain("/api/v1/auth/me");
    expect(body).not.toMatch(/jwt-secret|refresh-secret|bearer-secret|senha=secret|password=secret|hash=secret|spring\.internal|auth_session/i);
    expect(response.headers.get("authorization")).toBeNull();
    expect(response.headers.get("set-cookie")).toBeNull();
  });

  it("normaliza HTTP não-ProblemDetail e falha de rede sem detalhes internos", async () => {
    const http = authErrorResponse(new BackendHttpError(502, { status: 502, title: "Erro HTTP no serviço de autenticação" }, "corr-http", "http"));
    expect(http.status).toBe(502);
    expect(http.headers.get("x-correlation-id")).toBe("corr-http");
    expect(await http.json()).toEqual({ status: 502, title: "Erro HTTP no serviço de autenticação", detail: undefined, instance: undefined });
    const network = authErrorResponse(new BackendNetworkError());
    expect(network.status).toBe(503);
    expect(await network.text()).not.toMatch(/BACKEND_API_URL|https?:|Bearer|Authorization|token|senha|password/i);
  });
});
