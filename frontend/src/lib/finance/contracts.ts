export type DecimalString = string;
export type Market = "B3" | "US";
export type Currency = "BRL" | "USD";
export type QuoteProvider = "BRAPI" | "ALPHA_VANTAGE" | "TWELVE_DATA";
export type ExchangeProvider = "ALPHA_VANTAGE" | "TWELVE_DATA";
export type TransactionType = "BUY" | "SELL";
export type CashMovementType = "DEPOSITO" | "SAQUE";

export type ExchangeRateSnapshot = {
  taxaCambioBrl: DecimalString;
  provider: ExchangeProvider;
  instanteCambio: string;
};

export type ValuedPosition = {
  ativoId: string;
  ticker: string;
  mercado: Market;
  moeda: Currency;
  quantidade: DecimalString;
  precoMedioBrl: DecimalString;
  totalInvestidoBrl: DecimalString;
  cotacaoAtual: DecimalString;
  providerCotacao: QuoteProvider;
  instanteCotacao: string;
  valorAtualOrigem: DecimalString;
  valorAtualBrl: DecimalString;
  lucroNaoRealizadoBrl: DecimalString;
  rentabilidadePercentual: DecimalString;
};

export type PortfolioSummary = {
  valuationInstant: string;
  saldoCaixaBrl: DecimalString;
  totalInvestidoBrl: DecimalString;
  valorPosicoesBrl: DecimalString;
  lucroNaoRealizadoBrl: DecimalString;
  lucroRealizadoAcumuladoBrl: DecimalString;
  patrimonioTotalBrl: DecimalString;
  rentabilidadeNaoRealizadaPercentual: DecimalString | null;
  cambioAtual: ExchangeRateSnapshot | null;
  posicoes: ValuedPosition[];
};

export type Position = {
  id: string;
  ativoId: string;
  ticker: string;
  quantidade: DecimalString;
  precoMedioBrl: DecimalString;
  totalInvestidoBrl: DecimalString;
  lucroRealizadoAcumuladoBrl: DecimalString;
  ultimaAtualizacao: string;
};

export type Transaction = {
  id: string;
  ativoId: string;
  ticker: string;
  corretoraId: string;
  exchangeRateId: string | null;
  tipo: TransactionType;
  quantidade: DecimalString;
  moeda: Currency;
  precoUnitario: DecimalString;
  taxas: DecimalString;
  taxaCambioBrl: DecimalString;
  valorTotalBrl: DecimalString;
  resultadoRealizadoBrl: DecimalString | null;
  dataNegociacao: string;
  dataRegistro: string;
};

export type CashBalance = { saldoCaixaBrl: DecimalString };
export type CashMovement = {
  id: string;
  tipo: CashMovementType;
  valorBrl: DecimalString;
  descricao: string | null;
  dataHora: string;
};

export type Page<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type SafeProblemDetail = {
  status: number;
  title: string;
  detail?: string;
  instance?: string;
};
