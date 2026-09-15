import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { correlationId, summary } from "@/test/finance-fixtures";
import { FinanceApiError, financeApi } from "./finance-api";

const fetchMock = vi.fn<typeof fetch>();

describe("cliente financeiro same-origin", () => {
  beforeEach(() => { vi.stubGlobal("fetch", fetchMock); fetchMock.mockReset(); });
  afterEach(() => vi.unstubAllGlobals());

  it("consulta somente /api/finance com credenciais same-origin e no-store", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify(summary), { status: 200, headers: { "Content-Type": "application/json" } }));
    expect((await financeApi.summary()).patrimonioTotalBrl).toBe(summary.patrimonioTotalBrl);
    expect(fetchMock).toHaveBeenCalledWith("/api/finance/portfolio/summary", expect.objectContaining({ credentials: "same-origin", cache: "no-store" }));
    expect(new Headers(fetchMock.mock.calls[0][1]?.headers).has("authorization")).toBe(false);
  });

  it("faz refresh apenas por POST, sem body", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify(summary), { status: 200, headers: { "Content-Type": "application/json" } }));
    await financeApi.refreshSummary();
    expect(fetchMock).toHaveBeenCalledWith("/api/finance/portfolio/summary/refresh", expect.objectContaining({ method: "POST" }));
    expect(fetchMock.mock.calls[0][1]?.body).toBeUndefined();
  });

  it("normaliza ProblemDetail e correlation ID sem retornar conteúdo não-JSON", async () => {
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ status: 403, title: "Proibido" }), { status: 403, headers: { "Content-Type": "application/problem+json", "X-Correlation-ID": correlationId } }));
    const error = await financeApi.cash().catch((caught) => caught) as FinanceApiError;
    expect(error).toBeInstanceOf(FinanceApiError);
    expect(error).toMatchObject({ status: 403, correlationId });
  });
});
