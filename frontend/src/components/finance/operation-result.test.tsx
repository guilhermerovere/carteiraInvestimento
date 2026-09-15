import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { Asset, Broker, CashOperationResponse, InvestmentOperationResponse, OperationKind } from "@/lib/finance/contracts";
import { OperationResult } from "./operation-center";

const asset: Asset = { id: "asset-1", ticker: "ACME3", nome: "Acme", tipo: "ACAO", mercado: "B3", moeda: "BRL", ativo: true, logoProvider: null, logoReference: null };
const broker: Broker = { id: "broker-1", razaoSocial: "Corretora Exemplo S.A.", nomeFantasia: "Corretora Exemplo", logoProvider: null, logoReference: null };
const cash = (tipo: "DEPOSITO" | "SAQUE"): CashOperationResponse => ({ movimentacao: { id: "movement-technical-id", tipo, valorBrl: "1000.00", descricao: "Reserva", dataHora: "2026-09-13T12:00:00Z" }, saldoResultante: tipo === "DEPOSITO" ? "2000.00" : "500.00" });
const investment = (tipo: "BUY" | "SELL"): InvestmentOperationResponse => ({
  transacao: { id: "transaction-technical-id", ativoId: asset.id, ticker: asset.ticker, corretoraId: broker.id, exchangeRateId: null, tipo, quantidade: "2.00000000", moeda: "BRL", precoUnitario: "10.00000000", taxas: "0.00", taxaCambioBrl: "1.00", valorTotalBrl: "20.00", resultadoRealizadoBrl: tipo === "SELL" ? "5.00" : null, dataNegociacao: "2026-09-13T12:00:00Z", dataRegistro: "2026-09-13T12:00:01Z" },
  valorOrigem: "20.00", saldoCaixaBrl: tipo === "BUY" ? "980.00" : "1020.00",
  posicao: { id: "position-technical-id", ativoId: asset.id, ticker: asset.ticker, nome: asset.nome, mercado: "B3", moeda: "BRL", ativo: true, logoProvider: null, logoReference: null, quantidade: tipo === "BUY" ? "2.00000000" : "0.00000000", precoMedioBrl: tipo === "BUY" ? "10.00000000" : "0.00000000", totalInvestidoBrl: tipo === "BUY" ? "20.00" : "0.00", lucroRealizadoAcumuladoBrl: tipo === "SELL" ? "5.00" : "0.00", ultimaAtualizacao: "2026-09-13T12:00:01Z" },
});

describe("resultado operacional tipado", () => {
  afterEach(cleanup);
  it.each([
    ["DEPOSIT", cash("DEPOSITO"), "Depósito realizado com sucesso"],
    ["WITHDRAW", cash("SAQUE"), "Saque realizado com sucesso"],
    ["BUY", investment("BUY"), "Compra registrada com sucesso"],
    ["SELL", investment("SELL"), "Venda registrada com sucesso"],
  ] as const)("apresenta %s em pt-BR sem representação técnica", (kind, result, success) => {
    render(<OperationResult kind={kind as OperationKind} result={result} asset={asset} broker={broker} onClose={vi.fn()} />);
    expect(screen.getByText(success)).toBeVisible();
    expect(screen.getByRole("button", { name: "Concluir" })).toBeEnabled();
    expect(document.body.textContent).not.toMatch(/[{}]|movement-technical-id|transaction-technical-id|position-technical-id|ativoId|saldoResultante/);
  });

  it("mostra quantidades inteiras, BRL em duas casas e preco medio real apos BUY", () => {
    render(<OperationResult kind="BUY" result={investment("BUY")} asset={asset} broker={broker} onClose={vi.fn()} />);
    expect(screen.getByText("Quantidade").parentElement).toHaveTextContent("2");
    expect(screen.getByText("Preço").parentElement).toHaveTextContent("R$ 10,00");
    expect(screen.getByText("Valor da operação").parentElement).toHaveTextContent("R$ 20,00");
    expect(screen.getByText("Quantidade restante").parentElement).toHaveTextContent("2");
    expect(screen.getByText("Preço médio restante").parentElement).toHaveTextContent("R$ 10,00");
    expect(document.body).not.toHaveTextContent("2,00000000");
    expect(document.body).not.toHaveTextContent("BRL 10.00000000");
  });

  it("usa US$ no preco unitario de uma compra americana", () => {
    const result = investment("BUY");
    result.transacao.moeda = "USD";
    result.transacao.precoUnitario = "42.73000000";
    render(<OperationResult kind="BUY" result={result} asset={{ ...asset, mercado: "US", moeda: "USD" }} broker={broker} onClose={vi.fn()} />);
    expect(screen.getByText("Preço").parentElement).toHaveTextContent("US$ 42,73");
  });
});
