import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { CashBalance, CashMovement, Page, PortfolioSummary, Position, Transaction } from "@/lib/finance/contracts";

type QueryState<T> = {
  data: T;
  isPending: boolean;
  isError: boolean;
  isFetching: boolean;
  error: unknown;
  refetch: ReturnType<typeof vi.fn>;
};
type RefreshState = {
  isPending: boolean;
  isError: boolean;
  error: unknown;
  mutateAsync: ReturnType<typeof vi.fn>;
};

const hooks = vi.hoisted(() => ({
  summary: {} as QueryState<PortfolioSummary>,
  refresh: {} as RefreshState,
  positions: {} as QueryState<Page<Position>>,
  transactions: {} as QueryState<Page<Transaction>>,
  balance: {} as QueryState<CashBalance>,
  movements: {} as QueryState<Page<CashMovement>>,
}));
vi.mock("@/client/portfolio-queries", () => ({
  usePortfolioSummary: () => hooks.summary,
  useRefreshPortfolioSummary: () => hooks.refresh,
  usePositions: () => hooks.positions,
  useTransactions: () => hooks.transactions,
  useCashBalance: () => hooks.balance,
  useCashMovements: () => hooks.movements,
}));

import { FinanceApiError } from "@/client/finance-api";
import { cashBalance, cashMovement, correlationId, pageOf, position, secondAssetId, summary, transaction } from "@/test/finance-fixtures";
import { CashPanel } from "./cash-panel";
import { Pagination } from "./pagination";
import { PositionsPanel } from "./positions-panel";
import { EmptyState, FinancialSkeleton, ProblemDetailAlert } from "./states";
import { SummaryPanel } from "./summary-panel";
import { TransactionsPanel } from "./transactions-panel";

function successful<T>(data: T) {
  return { data, isPending: false, isError: false, isFetching: false, error: null, refetch: vi.fn() };
}

describe("painéis financeiros isolados", () => {
  beforeEach(() => {
    hooks.summary = successful(summary);
    hooks.refresh = { isPending: false, isError: false, error: null, mutateAsync: vi.fn().mockResolvedValue(summary) };
    hooks.positions = successful(pageOf(position));
    hooks.transactions = successful(pageOf(transaction));
    hooks.balance = successful(cashBalance);
    hooks.movements = successful(pageOf(cashMovement));
  });
  afterEach(cleanup);

  it("reserva espaço durante loading e usa empty states neutros", () => {
    const { rerender } = render(<FinancialSkeleton rows={2} label="Carregando posições" />);
    expect(screen.getByRole("status", { name: "Carregando posições" })).toBeInTheDocument();
    rerender(<EmptyState title="Sem posições abertas" description="Não há ativos em custódia." />);
    expect(screen.getByRole("heading", { name: "Sem posições abertas" })).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/comprar|depositar/i);
  });

  it("prioriza patrimônio, contexto, métricas e freshness sem alegar simultaneidade", () => {
    render(<SummaryPanel />);
    expect(screen.getByText("Patrimônio total")).toBeInTheDocument();
    expect(screen.getByText("R$ 125.001.200,22345679")).toBeInTheDocument();
    expect(screen.getAllByText("Lucro").length).toBeGreaterThan(0);
    expect(screen.getByText("Saldo em caixa")).toBeInTheDocument();
    expect(screen.getByText(/Carteira calculada em/)).toBeInTheDocument();
    expect(screen.getByText(/podem ser diferentes/)).toBeInTheDocument();
  });

  it("mantém o último resumo em falhas 409 e 502 de refresh e oferece retry", () => {
    hooks.refresh = { isPending: false, isError: true, error: new FinanceApiError(409, { status: 409, title: "Conflict" }), mutateAsync: vi.fn() };
    const { rerender } = render(<SummaryPanel />);
    expect(screen.getByText("R$ 125.001.200,22345679")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Os dados da carteira mudaram durante a atualização");
    hooks.refresh = { isPending: false, isError: true, error: new FinanceApiError(502, { status: 502, title: "Bad gateway" }), mutateAsync: vi.fn() };
    rerender(<SummaryPanel />);
    expect(screen.getByText("R$ 125.001.200,22345679")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Não foi possível atualizar os dados de mercado agora");
  });

  it("impede refresh duplicado enquanto a primeira solicitação está em voo", async () => {
    hooks.refresh.mutateAsync = vi.fn(() => new Promise(() => undefined));
    render(<SummaryPanel />);
    const button = screen.getByRole("button", { name: "Atualizar mercado" });
    await userEvent.click(button);
    await userEvent.click(button);
    expect(hooks.refresh.mutateAsync).toHaveBeenCalledOnce();
    expect(screen.getByText("Atualizando dados de mercado.")).toBeInTheDocument();
  });

  it("enriquece posições só por ativoId e mostra indisponível quando o valuation falta", () => {
    hooks.positions = successful({ ...pageOf(position), items: [position, { ...position, id: "77777777-7777-4777-8777-777777777777", ativoId: secondAssetId }] });
    render(<PositionsPanel />);
    expect(screen.getByRole("table", { name: "Posições abertas" })).toBeInTheDocument();
    expect(screen.getAllByText("ACME3").length).toBeGreaterThan(1);
    expect(screen.getAllByText("Indisponível").length).toBeGreaterThan(0);
    expect(screen.getByText(/Valuation não atualizado para este ativo/)).toBeInTheDocument();
    expect(screen.getAllByText("Detalhes").length).toBeGreaterThan(1);
  });

  it("uma falha de posições não remove um resumo carregado", () => {
    hooks.positions = { ...successful(pageOf(position)), isError: true, error: new FinanceApiError(502, { status: 502, title: "Falha" }) };
    render(<><SummaryPanel /><PositionsPanel /></>);
    expect(screen.getByText("R$ 125.001.200,22345679")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toBeInTheDocument();
  });

  it("exibe transação textual e detalhes reais sem edição ou nome de corretora inventado", () => {
    render(<TransactionsPanel />);
    expect(screen.getByText("SELL · Venda")).toBeInTheDocument();
    expect(screen.getByText("Prejuízo realizado")).toBeInTheDocument();
    expect(screen.getByText("Identificador da corretora")).toBeInTheDocument();
    expect(screen.getByText(transaction.corretoraId)).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/editar|excluir/i);
  });

  it("isola saldo e histórico e nunca inventa saldo resultante por movimento", () => {
    render(<CashPanel />);
    expect(screen.getByText("Saldo atual")).toBeInTheDocument();
    expect(screen.getByText("Aporte mensal")).toBeInTheDocument();
    expect(screen.getByText("Depósito")).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/saldo resultante/i);
    expect(screen.queryByRole("button", { name: /depositar|sacar/i })).not.toBeInTheDocument();
  });
});

describe("erros, suporte e paginação acessíveis", () => {
  afterEach(cleanup);

  it.each([
    [401, "Sua sessão precisa ser confirmada novamente."],
    [403, "Você não tem permissão para acessar estes dados."],
    [409, "Os dados da carteira mudaram durante a atualização."],
    [502, "Não foi possível atualizar os dados de mercado agora."],
  ])("traduz status %i sem expor upstream", (status, message) => {
    render(<ProblemDetailAlert error={new FinanceApiError(status, { status, title: "http://spring.internal" }, correlationId)} />);
    expect(screen.getByRole("alert")).toHaveTextContent(message);
    expect(screen.getByText(/Código de suporte/)).toHaveTextContent(correlationId);
    expect(document.body.textContent).not.toContain("spring.internal");
  });

  it("oferece paginação por botões com limites corretos", async () => {
    const change = vi.fn();
    render(<Pagination page={1} totalPages={3} onPageChange={change} />);
    expect(screen.getByRole("navigation", { name: "Paginação" })).toBeInTheDocument();
    await userEvent.click(screen.getByRole("button", { name: /Próxima/ }));
    expect(change).toHaveBeenCalledWith(2);
    await userEvent.click(screen.getByRole("button", { name: /Anterior/ }));
    expect(change).toHaveBeenCalledWith(0);
  });
});
