export type DecimalString = string;
export type Market = "B3" | "US";
export type Currency = "BRL" | "USD";
export type QuoteProvider = "BRAPI" | "ALPHA_VANTAGE" | "TWELVE_DATA";
export type ExchangeProvider = "ALPHA_VANTAGE" | "TWELVE_DATA";
export type TransactionType = "BUY" | "SELL";
export type LogoProvider = "BRAPI" | "LOGO_DEV";
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
  cotacaoDisponivel?: boolean;
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
  cotacoesDisponiveis?: boolean;
};

export type Position = {
  id: string;
  ativoId: string;
  ticker: string;
  nome: string;
  mercado: Market;
  moeda: Currency;
  ativo: boolean;
  logoProvider: LogoProvider | null;
  logoReference: string | null;
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
  nome?: string;
  logoProvider?: LogoProvider | null;
  logoReference?: string | null;
  corretoraId: string;
  corretoraNome?: string | null;
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
export type CashOperationResponse = { movimentacao: CashMovement; saldoResultante: DecimalString };
export type InvestmentOperationResponse = {
  transacao: Transaction;
  valorOrigem: DecimalString;
  saldoCaixaBrl: DecimalString;
  posicao: Position;
};

export type Page<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type SafeProblemCode = "FX_EXPIRED" | "EMAIL_IN_USE" | "CURRENT_PASSWORD_INCORRECT" | "CASH_NOT_ZERO" | "OPEN_POSITIONS" | "BROKER_CNPJ_INVALID" | "BROKER_ALREADY_REGISTERED" | "BROKER_CVM_NOT_REGISTERED" | "BROKER_CVM_NOT_APPROVED" | "BROKER_VALIDATION_UNAVAILABLE";
export type SafeProblemDetail = {
  status: number;
  title: string;
  detail?: string;
  instance?: string;
  code?: SafeProblemCode;
};

export type Asset = { id: string; ticker: string; nome: string; tipo: "ACAO" | "FII" | "ETF"; mercado: Market; moeda: Currency; ativo: boolean; logoProvider: LogoProvider | null; logoReference: string | null };
export type AssetDiscovery = { ticker: string; mercado: Market };
export type Broker = { id: string; razaoSocial: string; nomeFantasia: string | null; logoProvider: LogoProvider | null; logoReference: string | null };
export type MarketQuote = { id: string; ativoId: string; preco: DecimalString; moeda: Currency; instanteCotacao: string; provider: QuoteProvider; recebidoEm: string };
export type ExchangeRate = { id: string; moedaOrigem: "USD"; moedaDestino: "BRL"; taxa: DecimalString; instanteCotacao: string; provider: ExchangeProvider; registradoEm: string };
export type CashPayload = { valor: DecimalString; descricao?: string };
export type TransactionPayload = { ativoId: string; corretoraId: string; tipo: TransactionType; quantidade: DecimalString; precoUnitario: DecimalString; taxas: DecimalString; dataNegociacao: string; exchangeRateId: string | null };
export type OperationKind = "DEPOSIT" | "WITHDRAW" | "BUY" | "SELL";
export type OperationPayload = CashPayload | TransactionPayload;
export type FrozenOperationIntent = { version: 1; kind: OperationKind; key: string; payload: OperationPayload; createdAt: string; ambiguous: boolean };
