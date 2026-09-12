import "server-only";
import type {
  CashBalance, CashMovement, Currency, ExchangeProvider, Market, Page, PortfolioSummary,
  Position, QuoteProvider, Transaction, TransactionType, ValuedPosition,
} from "@/lib/finance/contracts";

type Data = Record<string, unknown>;
const DECIMAL = /^-?(?:0|[1-9]\d*)(?:\.\d+)?$/;

function object(value: unknown, field = "response"): Data {
  if (!value || typeof value !== "object" || Array.isArray(value)) throw new TypeError(field + " inválido");
  return value as Data;
}
function string(value: unknown, field: string): string {
  if (typeof value !== "string" || !value) throw new TypeError(field + " inválido");
  return value;
}
function nullableString(value: unknown, field: string): string | null {
  return value === null ? null : string(value, field);
}
function decimal(value: unknown, field: string): string {
  if (typeof value !== "string" || !DECIMAL.test(value)) throw new TypeError(field + " decimal inválido");
  return value;
}
function nullableDecimal(value: unknown, field: string): string | null {
  return value === null ? null : decimal(value, field);
}
function oneOf<T extends string>(value: unknown, values: readonly T[], field: string): T {
  if (typeof value !== "string" || !values.includes(value as T)) throw new TypeError(field + " inválido");
  return value as T;
}
function array(value: unknown, field: string): unknown[] {
  if (!Array.isArray(value)) throw new TypeError(field + " inválido");
  return value;
}
function safeCounter(value: unknown, field: string): number {
  let parsed: number;
  if (typeof value === "string" && /^(0|[1-9]\d*)$/.test(value)) parsed = Number(value);
  else if (typeof value === "number") parsed = value;
  else throw new TypeError(field + " inválido");
  if (!Number.isFinite(parsed) || !Number.isSafeInteger(parsed) || parsed < 0) throw new TypeError(field + " inseguro");
  return parsed;
}

function mapValuedPosition(value: unknown): ValuedPosition {
  const d = object(value, "posição avaliada");
  return {
    ativoId: string(d.ativoId, "ativoId"),
    ticker: string(d.ticker, "ticker"),
    mercado: oneOf<Market>(d.mercado, ["B3", "US"], "mercado"),
    moeda: oneOf<Currency>(d.moeda, ["BRL", "USD"], "moeda"),
    quantidade: decimal(d.quantidade, "quantidade"),
    precoMedioBrl: decimal(d.precoMedioBrl, "precoMedioBrl"),
    totalInvestidoBrl: decimal(d.totalInvestidoBrl, "totalInvestidoBrl"),
    cotacaoAtual: decimal(d.cotacaoAtual, "cotacaoAtual"),
    providerCotacao: oneOf<QuoteProvider>(d.providerCotacao, ["BRAPI", "ALPHA_VANTAGE", "TWELVE_DATA"], "providerCotacao"),
    instanteCotacao: string(d.instanteCotacao, "instanteCotacao"),
    valorAtualOrigem: decimal(d.valorAtualOrigem, "valorAtualOrigem"),
    valorAtualBrl: decimal(d.valorAtualBrl, "valorAtualBrl"),
    lucroNaoRealizadoBrl: decimal(d.lucroNaoRealizadoBrl, "lucroNaoRealizadoBrl"),
    rentabilidadePercentual: decimal(d.rentabilidadePercentual, "rentabilidadePercentual"),
  };
}

export function mapSummary(value: unknown): PortfolioSummary {
  const d = object(value);
  const exchange = d.cambioAtual === null ? null : object(d.cambioAtual, "cambioAtual");
  return {
    valuationInstant: string(d.valuationInstant, "valuationInstant"),
    saldoCaixaBrl: decimal(d.saldoCaixaBrl, "saldoCaixaBrl"),
    totalInvestidoBrl: decimal(d.totalInvestidoBrl, "totalInvestidoBrl"),
    valorPosicoesBrl: decimal(d.valorPosicoesBrl, "valorPosicoesBrl"),
    lucroNaoRealizadoBrl: decimal(d.lucroNaoRealizadoBrl, "lucroNaoRealizadoBrl"),
    lucroRealizadoAcumuladoBrl: decimal(d.lucroRealizadoAcumuladoBrl, "lucroRealizadoAcumuladoBrl"),
    patrimonioTotalBrl: decimal(d.patrimonioTotalBrl, "patrimonioTotalBrl"),
    rentabilidadeNaoRealizadaPercentual: nullableDecimal(d.rentabilidadeNaoRealizadaPercentual, "rentabilidadeNaoRealizadaPercentual"),
    cambioAtual: exchange ? {
      taxaCambioBrl: decimal(exchange.taxaCambioBrl, "taxaCambioBrl"),
      provider: oneOf<ExchangeProvider>(exchange.provider, ["ALPHA_VANTAGE", "TWELVE_DATA"], "provider"),
      instanteCambio: string(exchange.instanteCambio, "instanteCambio"),
    } : null,
    posicoes: array(d.posicoes, "posicoes").map(mapValuedPosition),
  };
}

export function mapPosition(value: unknown): Position {
  const d = object(value, "posição");
  return {
    id: string(d.id, "id"), ativoId: string(d.ativoId, "ativoId"), ticker: string(d.ticker, "ticker"),
    quantidade: decimal(d.quantidade, "quantidade"), precoMedioBrl: decimal(d.precoMedioBrl, "precoMedioBrl"),
    totalInvestidoBrl: decimal(d.totalInvestidoBrl, "totalInvestidoBrl"),
    lucroRealizadoAcumuladoBrl: decimal(d.lucroRealizadoAcumuladoBrl, "lucroRealizadoAcumuladoBrl"),
    ultimaAtualizacao: string(d.ultimaAtualizacao, "ultimaAtualizacao"),
  };
}

export function mapTransaction(value: unknown): Transaction {
  const d = object(value, "transação");
  return {
    id: string(d.id, "id"), ativoId: string(d.ativoId, "ativoId"), ticker: string(d.ticker, "ticker"),
    corretoraId: string(d.corretoraId, "corretoraId"), exchangeRateId: nullableString(d.exchangeRateId, "exchangeRateId"),
    tipo: oneOf<TransactionType>(d.tipo, ["BUY", "SELL"], "tipo"),
    quantidade: decimal(d.quantidade, "quantidade"), moeda: oneOf<Currency>(d.moeda, ["BRL", "USD"], "moeda"),
    precoUnitario: decimal(d.precoUnitario, "precoUnitario"), taxas: decimal(d.taxas, "taxas"),
    taxaCambioBrl: decimal(d.taxaCambioBrl, "taxaCambioBrl"), valorTotalBrl: decimal(d.valorTotalBrl, "valorTotalBrl"),
    resultadoRealizadoBrl: nullableDecimal(d.resultadoRealizadoBrl, "resultadoRealizadoBrl"),
    dataNegociacao: string(d.dataNegociacao, "dataNegociacao"), dataRegistro: string(d.dataRegistro, "dataRegistro"),
  };
}

export function mapCashBalance(value: unknown): CashBalance {
  const d = object(value, "saldo");
  return { saldoCaixaBrl: decimal(d.saldoCaixaBrl, "saldoCaixaBrl") };
}

export function mapCashMovement(value: unknown): CashMovement {
  const d = object(value, "movimentação");
  return {
    id: string(d.id, "id"), tipo: oneOf(d.tipo, ["DEPOSITO", "SAQUE"] as const, "tipo"),
    valorBrl: decimal(d.valorBrl, "valorBrl"),
    descricao: d.descricao === null ? null : string(d.descricao, "descricao"),
    dataHora: string(d.dataHora, "dataHora"),
  };
}

export function mapPage<T>(value: unknown, itemMapper: (item: unknown) => T): Page<T> {
  const d = object(value, "página");
  return {
    items: array(d.items, "items").map(itemMapper),
    page: safeCounter(d.page, "page"), size: safeCounter(d.size, "size"),
    totalElements: safeCounter(d.totalElements, "totalElements"), totalPages: safeCounter(d.totalPages, "totalPages"),
  };
}
