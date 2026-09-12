import { beforeEach, describe, expect, it, vi } from "vitest";

const { backendRequest } = vi.hoisted(() => ({ backendRequest: vi.fn() }));
vi.mock("@/server/finance/transport", async (loadOriginal) => {
  const original = await loadOriginal<typeof import("@/server/finance/transport")>();
  return { ...original, financeBackendRequest: backendRequest };
});

import { GET as getSummary } from "./portfolio/summary/route";
import { POST as refreshSummary } from "./portfolio/summary/refresh/route";
import { GET as getPositions } from "./positions/route";
import { GET as getTransactions } from "./transactions/route";
import { GET as getCash } from "./cash/route";
import { GET as getMovements } from "./cash/movements/route";
import { correlationId, pageOf, position, summary, transaction, cashBalance, cashMovement } from "@/test/finance-fixtures";

describe("Route Handlers financeiros same-origin", () => {
  beforeEach(() => backendRequest.mockReset());

  it("mapeia o GET de resumo sem mutação, com no-store e correlation ID", async () => {
    backendRequest.mockResolvedValue({ data: summary, correlationId });
    const response = await getSummary(new Request("https://app.test/api/finance/portfolio/summary", { headers: { "X-Correlation-ID": correlationId } }));
    expect(response.status).toBe(200);
    expect(response.headers.get("cache-control")).toBe("no-store");
    expect(response.headers.get("x-correlation-id")).toBe(correlationId);
    expect((await response.json()).patrimonioTotalBrl).toBe("125001200.22345679");
    expect(backendRequest).toHaveBeenCalledWith("/api/v1/carteira/resumo", { correlationId });
  });

  it("faz refresh somente por POST sem corpo/query", async () => {
    backendRequest.mockResolvedValue({ data: summary });
    const ok = await refreshSummary(new Request("https://app.test/api/finance/portfolio/summary/refresh", { method: "POST" }));
    expect(ok.status).toBe(200);
    expect(backendRequest).toHaveBeenCalledWith("/api/v1/carteira/resumo/atualizar", { method: "POST", correlationId: undefined });
    backendRequest.mockClear();
    const bad = await refreshSummary(new Request("https://app.test/api/finance/portfolio/summary/refresh?force=true", { method: "POST", body: "{}" }));
    expect(bad.status).toBe(400);
    expect(bad.headers.get("cache-control")).toBe("no-store");
    expect(backendRequest).not.toHaveBeenCalled();
  });

  it.each([
    [getPositions, "/api/finance/positions", "/api/v1/carteira/posicoes?page=0&size=20", pageOf(position)],
    [getTransactions, "/api/finance/transactions", "/api/v1/carteira/transacoes?page=0&size=20", pageOf(transaction)],
    [getMovements, "/api/finance/cash/movements", "/api/v1/carteira/caixa/movimentacoes?page=0&size=20", pageOf(cashMovement)],
  ])("normaliza paginação de %s", async (handler, bffPath, backendPath, data) => {
    backendRequest.mockResolvedValue({ data });
    const response = await handler(new Request("https://app.test" + bffPath));
    expect(response.status).toBe(200);
    expect((await response.json()).page).toBe(0);
    expect(backendRequest).toHaveBeenCalledWith(backendPath, { correlationId: undefined });
  });

  it("rejeita paginação desconhecida ou insegura antes do Spring", async () => {
    for (const query of ["sort=ticker", "page=-1", "size=101", "page=9007199254740992"]) {
      const response = await getPositions(new Request("https://app.test/api/finance/positions?" + query, { headers: { "X-Correlation-ID": correlationId } }));
      expect(response.status).toBe(400);
      expect(response.headers.get("cache-control")).toBe("no-store");
      expect(response.headers.get("x-correlation-id")).toBe(correlationId);
    }
    expect(backendRequest).not.toHaveBeenCalled();
  });

  it("mapeia saldo sem campos inventados", async () => {
    backendRequest.mockResolvedValue({ data: cashBalance });
    const response = await getCash(new Request("https://app.test/api/finance/cash"));
    expect(await response.json()).toEqual({ saldoCaixaBrl: "1200.10000001" });
  });
});
