import "server-only";
import type {
  CashBalance, CashMovement, Currency, ExchangeProvider, Market, Page, PortfolioSummary,
  LogoProvider, PortfolioEvolutionPoint, Position, QuoteProvider, Transaction, TransactionType, ValuedPosition,
} from "@/lib/finance/contracts";

type Data = Record<string, unknown>;
const LOSSLESS_DECIMAL = /^(-?)(0|[1-9]\d*)(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/;

function object(value: unknown, field = "response"): Data {
  if (!value || typeof value !== "object" || Array.isArray(value)) throw new TypeError(field + " inválido");
  return value as Data;
}
function string(value: unknown, field: string): string {
  if (typeof value !== "string" || !value) throw new TypeError(field + " inválido");
  return value;
}
function nullableString(value: unknown, field: string): string | null {
  return value === null || value === undefined ? null : string(value, field);
}
function decimal(value: unknown, field: string): string {
  if (typeof value !== "string") throw new TypeError(field + " decimal inválido");
  const match = LOSSLESS_DECIMAL.exec(value);
  if (!match) throw new TypeError(field + " decimal inválido");
  if (match[4] === undefined) return value;
  if (match[4].replace(/^[+-]/, "").length > 3) throw new TypeError(field + " decimal inválido");
  const decimalPlaces = (match[3] ?? "").length - Number(match[4]);
  if (decimalPlaces > 100 || decimalPlaces < -100) throw new TypeError(field + " decimal inválido");
  const digits = match[2] + (match[3] ?? "");
  if (decimalPlaces <= 0) return match[1] + digits + "0".repeat(-decimalPlaces);
  const point = digits.length - decimalPlaces;
  if (point > 0) return match[1] + digits.slice(0, point) + "." + digits.slice(point);
  return match[1] + "0." + "0".repeat(-point) + digits;
}
function nullableDecimal(value: unknown, field: string): string | null {
  return value === null || value === undefined ? null : decimal(value, field);
}
function oneOf<T extends string>(value: unknown, values: readonly T[], field: string): T {
  if (typeof value !== "string" || !values.includes(value as T)) throw new TypeError(field + " inválido");
  return value as T;
}
function array(value: unknown, field: string): unknown[] {
  if (!Array.isArray(value)) throw new TypeError(field + " inválido");
  return value;
}
function boolean(value: unknown, field: string): boolean { if (typeof value !== "boolean") throw new TypeError(field + " inválido"); return value; }
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
    tipo: oneOf(d.tipo, ["ACAO", "FII", "ETF"] as const, "tipo"),
    mercado: oneOf<Market>(d.mercado, ["B3", "US"], "mercado"),
    moeda: oneOf<Currency>(d.moeda, ["BRL", "USD"], "moeda"),
    quantidade: decimal(d.quantidade, "quantidade"),
    precoMedioBrl: decimal(d.precoMedioBrl, "precoMedioBrl"),
    totalInvestidoBrl: decimal(d.totalInvestidoBrl, "totalInvestidoBrl"),
    cotacaoAtual: nullableDecimal(d.cotacaoAtual, "cotacaoAtual"),
    providerCotacao: d.providerCotacao === null || d.providerCotacao === undefined ? null : oneOf<QuoteProvider>(d.providerCotacao, ["BRAPI", "ALPHA_VANTAGE", "TWELVE_DATA"], "providerCotacao"),
    instanteCotacao: nullableString(d.instanteCotacao, "instanteCotacao"),
    valorAtualOrigem: nullableDecimal(d.valorAtualOrigem, "valorAtualOrigem"),
    valorAtualBrl: nullableDecimal(d.valorAtualBrl, "valorAtualBrl"),
    lucroNaoRealizadoBrl: nullableDecimal(d.lucroNaoRealizadoBrl, "lucroNaoRealizadoBrl"),
    rentabilidadePercentual: nullableDecimal(d.rentabilidadePercentual, "rentabilidadePercentual"),
    cotacaoDisponivel: d.cotacaoDisponivel !== false,
  };
}

export function mapEvolution(value: unknown): PortfolioEvolutionPoint[] {
  return array(value, "evolução").map((item) => {
    const d = object(item, "ponto de evolução");
    return {
      dataReferencia: string(d.dataReferencia, "dataReferencia"),
      totalInvestidoBrl: decimal(d.totalInvestidoBrl, "totalInvestidoBrl"),
      resultadoNaoRealizadoBrl: decimal(d.resultadoNaoRealizadoBrl, "resultadoNaoRealizadoBrl"),
      valorPosicoesBrl: decimal(d.valorPosicoesBrl, "valorPosicoesBrl"),
      patrimonioTotalBrl: decimal(d.patrimonioTotalBrl, "patrimonioTotalBrl"),
    };
  });
}

export function mapSummary(value: unknown): PortfolioSummary {
  const d = object(value);
  const exchange = d.cambioAtual === null ? null : object(d.cambioAtual, "cambioAtual");
  return {
    valuationInstant: string(d.valuationInstant, "valuationInstant"),
    saldoCaixaBrl: decimal(d.saldoCaixaBrl, "saldoCaixaBrl"),
    totalInvestidoBrl: decimal(d.totalInvestidoBrl, "totalInvestidoBrl"),
    valorPosicoesBrl: decimal(d.valorPosicoesBrl ?? "0", "valorPosicoesBrl"),
    lucroNaoRealizadoBrl: decimal(d.lucroNaoRealizadoBrl ?? "0", "lucroNaoRealizadoBrl"),
    lucroRealizadoAcumuladoBrl: decimal(d.lucroRealizadoAcumuladoBrl, "lucroRealizadoAcumuladoBrl"),
    patrimonioTotalBrl: decimal(d.patrimonioTotalBrl ?? "0", "patrimonioTotalBrl"),
    rentabilidadeNaoRealizadaPercentual: nullableDecimal(d.rentabilidadeNaoRealizadaPercentual, "rentabilidadeNaoRealizadaPercentual"),
    cambioAtual: exchange ? {
      taxaCambioBrl: decimal(exchange.taxaCambioBrl, "taxaCambioBrl"),
      provider: oneOf<ExchangeProvider>(exchange.provider, ["ALPHA_VANTAGE", "TWELVE_DATA"], "provider"),
      instanteCambio: string(exchange.instanteCambio, "instanteCambio"),
    } : null,
    posicoes: array(d.posicoes, "posicoes").map(mapValuedPosition),
    cotacoesDisponiveis: d.cotacoesDisponiveis !== false,
  };
}

export function mapPosition(value: unknown): Position {
  const d = object(value, "posição");
  return {
    id: string(d.id, "id"), ativoId: string(d.ativoId, "ativoId"), ticker: string(d.ticker, "ticker"),
    nome: string(d.nome, "nome"), mercado: oneOf<Market>(d.mercado, ["B3", "US"], "mercado"),
    moeda: oneOf<Currency>(d.moeda, ["BRL", "USD"], "moeda"), ativo: boolean(d.ativo, "ativo"),
    logoProvider: d.logoProvider === null ? null : oneOf<LogoProvider>(d.logoProvider, ["BRAPI", "LOGO_DEV"], "logoProvider"),
    logoReference: nullableString(d.logoReference, "logoReference"),
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
    nome: d.nome === undefined || d.nome === null ? undefined : string(d.nome, "nome"),
    logoProvider: d.logoProvider === undefined || d.logoProvider === null ? null : oneOf<LogoProvider>(d.logoProvider, ["BRAPI", "LOGO_DEV"], "logoProvider"),
    logoReference: d.logoReference === undefined || d.logoReference === null ? null : string(d.logoReference, "logoReference"),
    corretoraId: string(d.corretoraId, "corretoraId"), exchangeRateId: nullableString(d.exchangeRateId, "exchangeRateId"),
    corretoraNome: d.corretoraNome === undefined || d.corretoraNome === null ? null : string(d.corretoraNome, "corretoraNome"),
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
