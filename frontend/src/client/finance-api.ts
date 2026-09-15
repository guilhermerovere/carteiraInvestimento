import type { Asset, AssetDiscovery, Broker, CashBalance, CashMovement, CashOperationResponse, CashPayload, ExchangeRate, InvestmentOperationResponse, MarketQuote, Page, PortfolioSummary, Position, SafeProblemCode, SafeProblemDetail, Transaction, TransactionPayload } from "@/lib/finance/contracts";

export class FinanceApiError extends Error {
  constructor(
    readonly status: number,
    readonly problem: SafeProblemDetail,
    readonly correlationId?: string,
  ) {
    super(problem.detail ?? problem.title);
  }
}

function safeProblem(value: unknown, status: number): SafeProblemDetail {
  if (!value || typeof value !== "object") return { status, title: "Não foi possível carregar os dados." };
  const d = value as Record<string, unknown>;
  return {
    status,
    title: typeof d.title === "string" ? d.title : "Não foi possível carregar os dados.",
    detail: typeof d.detail === "string" ? d.detail : undefined,
    instance: typeof d.instance === "string" ? d.instance : undefined,
    code: ["FX_EXPIRED", "EMAIL_IN_USE", "CURRENT_PASSWORD_INCORRECT", "CASH_NOT_ZERO", "OPEN_POSITIONS"].includes(String(d.code))
      ? d.code as SafeProblemCode : undefined,
  };
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, {
      ...init,
      cache: "no-store",
      credentials: "same-origin",
      headers: { Accept: "application/json", ...init.headers },
    });
  } catch {
    throw new FinanceApiError(0, { status: 0, title: "Sem conexão com o serviço financeiro." });
  }
  if (!response.ok) {
    let body: unknown;
    try { body = await response.json(); } catch { body = undefined; }
    throw new FinanceApiError(
      response.status,
      safeProblem(body, response.status),
      response.headers.get("x-correlation-id") ?? undefined,
    );
  }
  return response.json() as Promise<T>;
}

export const financeApi = {
  summary: () => request<PortfolioSummary>("/api/finance/portfolio/summary"),
  refreshSummary: () => request<PortfolioSummary>("/api/finance/portfolio/summary/refresh", { method: "POST" }),
  positions: (page: number, size: number) => request<Page<Position>>("/api/finance/positions?page=" + page + "&size=" + size),
  position: (assetId: string) => request<Position>("/api/finance/positions/" + encodeURIComponent(assetId)),
  transactions: (page: number, size: number) => request<Page<Transaction>>("/api/finance/transactions?page=" + page + "&size=" + size),
  transaction: (id: string) => request<Transaction>("/api/finance/transactions/" + encodeURIComponent(id)),
  cash: () => request<CashBalance>("/api/finance/cash"),
  cashMovements: (page: number, size: number) => request<Page<CashMovement>>("/api/finance/cash/movements?page=" + page + "&size=" + size),
  deposit: (payload: CashPayload, key: string) => request<CashOperationResponse>("/api/finance/cash/deposit", { method: "POST", headers: { "Content-Type": "application/json", "Idempotency-Key": key }, body: JSON.stringify(payload) }),
  withdraw: (payload: CashPayload, key: string) => request<CashOperationResponse>("/api/finance/cash/withdraw", { method: "POST", headers: { "Content-Type": "application/json", "Idempotency-Key": key }, body: JSON.stringify(payload) }),
  executeTransaction: (payload: TransactionPayload, key: string) => request<InvestmentOperationResponse>("/api/finance/transactions", { method: "POST", headers: { "Content-Type": "application/json", "Idempotency-Key": key }, body: JSON.stringify(payload) }),
  assets: (q: string, page: number, size: number) => request<Page<Asset>>(`/api/finance/catalog/assets?q=${encodeURIComponent(q)}&page=${page}&size=${size}`),
  exactAsset: (ticker: string) => request<Asset>("/api/finance/catalog/assets/ticker/" + encodeURIComponent(ticker)),
  discoverB3: (q: string) => request<AssetDiscovery[]>("/api/finance/catalog/assets/discovery?q=" + encodeURIComponent(q)),
  registerAsset: (ticker: string, mercado: "B3" | "US") => request<Asset>("/api/finance/catalog/assets", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ticker, mercado }) }),
  brokers: (page: number, size: number) => request<Page<Broker>>(`/api/finance/catalog/brokers?page=${page}&size=${size}`),
  quote: (assetId: string) => request<MarketQuote>("/api/finance/market/quotes/" + encodeURIComponent(assetId)),
  fx: () => request<ExchangeRate>("/api/finance/market/usd-brl"),
};
