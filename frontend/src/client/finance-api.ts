import type { CashBalance, CashMovement, Page, PortfolioSummary, Position, SafeProblemDetail, Transaction } from "@/lib/finance/contracts";

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
};
