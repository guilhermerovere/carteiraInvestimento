import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const { cookiesMock, configMock } = vi.hoisted(() => ({
  cookiesMock: vi.fn(),
  configMock: vi.fn(() => ({ backendApiUrl: "https://backend.internal" })),
}));
vi.mock("next/headers", () => ({ cookies: cookiesMock }));
vi.mock("@/server/auth/config", () => ({ loadAuthConfig: configMock }));

import { FinanceHttpError, financeBackendRequest } from "./transport";
import { correlationId } from "@/test/finance-fixtures";

const fetchMock = vi.fn<typeof fetch>();

describe("transporte financeiro server-only", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", fetchMock);
    fetchMock.mockReset();
    cookiesMock.mockResolvedValue({ get: () => ({ value: "jwt-secret-that-must-stay-server-side" }) });
  });
  afterEach(() => vi.unstubAllGlobals());

  it("anexa Bearer somente no salto para Spring, usa no-store e preserva decimal raw", async () => {
    fetchMock.mockResolvedValue(new Response('{"amount":0.00000001}', { status: 200, headers: { "X-Correlation-ID": correlationId } }));
    const result = await financeBackendRequest("/api/v1/carteira/resumo", { correlationId });
    expect(result).toEqual({ data: { amount: "0.00000001" }, correlationId });
    const [url, init] = fetchMock.mock.calls[0];
    expect(String(url)).toBe("https://backend.internal/api/v1/carteira/resumo");
    expect(init?.cache).toBe("no-store");
    expect(new Headers(init?.headers).get("authorization")).toBe("Bearer jwt-secret-that-must-stay-server-side");
    expect(new Headers(init?.headers).get("x-correlation-id")).toBe(correlationId);
  });

  it("não chama Spring sem cookie e não altera sessão", async () => {
    cookiesMock.mockResolvedValue({ get: () => undefined });
    await expect(financeBackendRequest("/api/v1/carteira/resumo")).rejects.toMatchObject({ status: 401 });
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("não encaminha correlation ID inseguro", async () => {
    fetchMock.mockResolvedValue(new Response("{}", { status: 200 }));
    await financeBackendRequest("/api/v1/carteira/resumo", { correlationId: "Bearer leaked" });
    expect(new Headers(fetchMock.mock.calls[0][1]?.headers).has("x-correlation-id")).toBe(false);
  });

  it("sanitiza ProblemDetail do upstream e prefere o correlation ID seguro da resposta", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({
      title: "Falha em https://spring.internal/private",
      detail: "Authorization=Bearer-secret password=hunter2",
      instance: "https://spring.internal/problem/1",
    }), { status: 502, headers: { "X-Correlation-ID": correlationId, "Content-Type": "application/problem+json" } }));
    const error = await financeBackendRequest("/api/v1/carteira/resumo").catch((caught) => caught) as FinanceHttpError;
    expect(error).toBeInstanceOf(FinanceHttpError);
    expect(error.status).toBe(502);
    expect(error.correlationId).toBe(correlationId);
    expect(JSON.stringify(error.problem)).not.toMatch(/spring\.internal|hunter2|Bearer-secret/);
  });

  it("preserva somente códigos seguros para distinguir os resultados de CNPJ da corretora", async () => {
    fetchMock.mockResolvedValueOnce(new Response(JSON.stringify({ status: 422, title: "Broker rejected", code: "BROKER_CVM_NOT_REGISTERED" }), { status: 422 }));
    const cvmError = await financeBackendRequest("/api/v1/corretoras", { method: "POST", body: "{}" }).catch((caught) => caught) as FinanceHttpError;
    expect(cvmError.problem.code).toBe("BROKER_CVM_NOT_REGISTERED");

    fetchMock.mockResolvedValueOnce(new Response(JSON.stringify({ status: 422, title: "Broker rejected", code: "PRIVATE_PROVIDER_REASON" }), { status: 422 }));
    const otherError = await financeBackendRequest("/api/v1/corretoras", { method: "POST", body: "{}" }).catch((caught) => caught) as FinanceHttpError;
    expect(otherError.problem.code).toBeUndefined();
  });
});
