import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { CashBalance, CashMovement, Page, PortfolioEvolutionPoint, PortfolioSummary, Position, Transaction } from "@/lib/finance/contracts";

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
  evolution: {} as QueryState<PortfolioEvolutionPoint[]>,
}));
vi.mock("@/client/portfolio-queries", () => ({
  usePortfolioSummary: () => hooks.summary,
  useRefreshPortfolioSummary: () => hooks.refresh,
  usePositions: () => hooks.positions,
  useTransactions: () => hooks.transactions,
  useCashBalance: () => hooks.balance,
  useCashMovements: () => hooks.movements,
  usePortfolioEvolution: () => hooks.evolution,
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

function evolutionPoint(day: number, multiplier = day): PortfolioEvolutionPoint {
  return {
    dataReferencia: `2026-09-${String(day).padStart(2, "0")}`,
    totalInvestidoBrl: String(100 + multiplier * 10),
    resultadoNaoRealizadoBrl: String(-20 + multiplier * 5),
    valorPosicoesBrl: String(90 + multiplier * 12),
    patrimonioTotalBrl: String(120 + multiplier * 14),
  };
}

describe("painéis financeiros isolados", () => {
  beforeEach(() => {
    hooks.summary = successful(summary);
    hooks.refresh = { isPending: false, isError: false, error: null, mutateAsync: vi.fn().mockResolvedValue(summary) };
    hooks.positions = successful(pageOf(position));
    hooks.transactions = successful(pageOf(transaction));
    hooks.balance = successful(cashBalance);
    hooks.movements = successful(pageOf(cashMovement));
    hooks.evolution = successful([{ dataReferencia: "2026-09-10", totalInvestidoBrl: "10.00", resultadoNaoRealizadoBrl: "1.00", valorPosicoesBrl: "11.00", patrimonioTotalBrl: "12.00" }]);
  });
  afterEach(cleanup);

  it("reserva espaço durante loading e usa empty states neutros", () => {
    const { rerender } = render(<FinancialSkeleton rows={2} label="Carregando posições" />);
    expect(screen.getByRole("status", { name: "Carregando posições" })).toBeInTheDocument();
    rerender(<EmptyState title="Sem posições abertas" description="Não há ativos em custódia." />);
    expect(screen.getByRole("heading", { name: "Sem posições abertas" })).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(/comprar|depositar/i);
  });

  it("prioriza patrimônio, métricas e última atualização em composição compacta", () => {
    render(<SummaryPanel />);
    expect(screen.getByText("Patrimônio total")).toBeInTheDocument();
    expect(screen.getByText("R$ 125.001.200,22")).toBeInTheDocument();
    expect(screen.getAllByText("Lucro").length).toBeGreaterThan(0);
    expect(screen.getByText("Saldo em caixa")).toBeInTheDocument();
    expect(screen.getByText("Última atualização")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Minhas posições" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Evolução patrimonial" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Composição por ação" })).toBeInTheDocument();
    expect(screen.getAllByText("R$ 123.456.789,1235").length).toBeGreaterThan(0);
  });

  it("preenche Patrimônio total com a composição atual Caixa x Posições", () => {
    render(<SummaryPanel />);
    const wealth = screen.getByText("Patrimônio total").closest(".wealth-hero")!;
    expect(wealth.querySelector(".current-composition")).toBeInTheDocument();
    expect(screen.getByText("Caixa", { exact: true })).toBeInTheDocument();
    expect(screen.getByText("Posições", { exact: true })).toBeInTheDocument();
  });

  it("mostra as proporções atuais de caixa e posições sobre o patrimônio", () => {
    render(<SummaryPanel />);
    expect(screen.getByText(/do patrimônio em caixa/)).toBeInTheDocument();
    expect(screen.getByText(/do patrimônio em ativos/)).toBeInTheDocument();
    expect(document.querySelectorAll(".current-proportion")).toHaveLength(2);
  });

  it("compara sempre Investido e Atual em duas barras compactas", () => {
    render(<SummaryPanel />);
    const invested = screen.getByText("Total investido").closest(".portfolio-metric")!;
    expect(invested.querySelector(".current-comparison")).toBeInTheDocument();
    expect(invested.querySelectorAll(".current-comparison__row")).toHaveLength(2);
    expect(screen.getByText("Investido", { exact: true })).toBeInTheDocument();
    expect(screen.getByText("Atual", { exact: true })).toBeInTheDocument();
  });

  it.each([
    ["0 snapshots", []],
    ["1 snapshot", [evolutionPoint(10)]],
    ["3 snapshots", [evolutionPoint(10), evolutionPoint(11), evolutionPoint(12)]],
  ])("não faz os cards dependerem de %s", (_label, points) => {
    hooks.evolution = successful(points);
    render(<SummaryPanel />);
    const metrics = document.querySelector(".summary-metrics")!;
    expect(metrics.querySelectorAll(".current-metric")).toHaveLength(5);
    expect(metrics.querySelector(".metric-sparkline")).toBeNull();
    expect(metrics.querySelector(".recharts-responsive-container")).toBeNull();
    expect(metrics.querySelector(".recharts-dot")).toBeNull();
    expect(metrics.querySelector(".recharts-bar-rectangle")).toBeNull();
  });

  it("preserva estado neutro e as duas barras com valores zero", () => {
    hooks.summary = successful({ ...summary, saldoCaixaBrl: "0.00", patrimonioTotalBrl: "0.00", totalInvestidoBrl: "0.00", valorPosicoesBrl: "0.00", lucroNaoRealizadoBrl: "0.00" });
    render(<SummaryPanel />);
    expect(screen.getByText("Sem patrimônio distribuído")).toBeInTheDocument();
    expect(screen.getAllByText("Sem patrimônio para calcular")).toHaveLength(2);
    expect(document.querySelectorAll(".current-comparison__row")).toHaveLength(2);
    expect(screen.getAllByText("Resultado neutro")).toHaveLength(2);
  });

  it("nomeia resultado não realizado negativo além de usar cor", () => {
    hooks.summary = successful({ ...summary, lucroNaoRealizadoBrl: "-2.00" });
    render(<SummaryPanel />);
    expect(screen.getByText("Resultado negativo")).toBeInTheDocument();
    expect(document.querySelector(".current-result--negative")).toBeInTheDocument();
    expect(document.querySelectorAll(".metric-accent").length).toBeGreaterThan(0);
  });

  it("mantém as visualizações atuais em Light e Dark", () => {
    document.documentElement.classList.add("dark");
    render(<SummaryPanel />);
    expect(document.querySelectorAll(".current-metric")).toHaveLength(5);
    document.documentElement.classList.remove("dark");
  });

  it("mantém o último resumo em falhas 409 e 502 de refresh e oferece retry", () => {
    hooks.refresh = { isPending: false, isError: true, error: new FinanceApiError(409, { status: 409, title: "Conflict" }), mutateAsync: vi.fn() };
    const { rerender } = render(<SummaryPanel />);
    expect(screen.getByText("R$ 125.001.200,22")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Os dados da carteira mudaram durante a atualização");
    hooks.refresh = { isPending: false, isError: true, error: new FinanceApiError(502, { status: 502, title: "Bad gateway" }), mutateAsync: vi.fn() };
    rerender(<SummaryPanel />);
    expect(screen.getByText("R$ 125.001.200,22")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Não foi possível atualizar os dados de mercado agora");
  });

  it("impede refresh duplicado enquanto a primeira solicitação está em voo", async () => {
    hooks.refresh.mutateAsync = vi.fn(() => new Promise(() => undefined));
    render(<SummaryPanel />);
    const button = screen.getByRole("button", { name: "Atualizar" });
    await userEvent.click(button);
    await userEvent.click(button);
    expect(hooks.refresh.mutateAsync).toHaveBeenCalledOnce();
    expect(screen.getByText("Atualizando dados de mercado.")).toBeInTheDocument();
  });

  it("enriquece posições só por ativoId e mostra indisponível quando o valuation falta", () => {
    hooks.positions = successful({ ...pageOf(position), items: [position, { ...position, id: "77777777-7777-4777-8777-777777777777", ativoId: secondAssetId }] });
    render(<PositionsPanel />);
    expect(screen.getByRole("table")).toBeInTheDocument();
    expect(screen.getAllByText("ACME3").length).toBeGreaterThan(1);
    expect(screen.getAllByText("Cotação indisponível").length).toBeGreaterThan(0);
    expect(screen.getAllByText("R$ 123.456.789,1235").length).toBeGreaterThan(0);
    expect(screen.getByText("Variação / Resultado")).toBeInTheDocument();
  });

  it("uma falha de posições não remove um resumo carregado", () => {
    hooks.positions = { ...successful(pageOf(position)), isError: true, error: new FinanceApiError(502, { status: 502, title: "Falha" }) };
    render(<><SummaryPanel /><PositionsPanel /></>);
    expect(screen.getByText("R$ 125.001.200,22")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toBeInTheDocument();
  });

  it("exibe histórico tabular persistido sem edição nem identificadores técnicos", () => {
    render(<TransactionsPanel />);
    expect(screen.getByText("Venda")).toBeInTheDocument();
    expect(screen.getByText("Prejuízo realizado")).toBeInTheDocument();
    expect(screen.getByText("XP Investimentos")).toBeInTheDocument();
    expect(screen.getByText("ACME S.A.")).toBeInTheDocument();
    expect(document.body.textContent).not.toMatch(new RegExp(transaction.corretoraId));
    expect(document.body.textContent).not.toMatch(/editar|excluir|uuid/i);
  });

  it("preserva o histórico persistido quando uma atualização complementar falha", () => {
    hooks.transactions = { ...successful(pageOf(transaction)), isError: true, error: new FinanceApiError(502, { status: 502, title: "Falha externa" }) };
    render(<TransactionsPanel />);
    expect(screen.getByRole("table")).toBeInTheDocument();
    expect(screen.getByText("Venda")).toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Os dados exibidos foram preservados");
    expect(screen.getByRole("alert")).not.toHaveTextContent("dados de mercado");
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
