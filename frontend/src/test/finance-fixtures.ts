import type { CashBalance, CashMovement, Page, PortfolioSummary, Position, Transaction } from "@/lib/finance/contracts";

export const assetId = "11111111-1111-4111-8111-111111111111";
export const secondAssetId = "22222222-2222-4222-8222-222222222222";
export const correlationId = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";

export const summary: PortfolioSummary = {
  valuationInstant: "2026-09-12T13:30:00Z",
  saldoCaixaBrl: "1200.10000001",
  totalInvestidoBrl: "123456789.12345678",
  valorPosicoesBrl: "125000000.12345678",
  lucroNaoRealizadoBrl: "1553211.00000000",
  lucroRealizadoAcumuladoBrl: "-123.45000000",
  patrimonioTotalBrl: "125001200.22345679",
  rentabilidadeNaoRealizadaPercentual: "1.25781234",
  cambioAtual: {
    taxaCambioBrl: "5.12345678",
    provider: "ALPHA_VANTAGE",
    instanteCambio: "2026-09-12T13:29:00Z",
  },
  posicoes: [{
    ativoId: assetId,
    ticker: "ACME3",
    mercado: "B3",
    moeda: "BRL",
    quantidade: "0.10000001",
    precoMedioBrl: "123456789.12345678",
    totalInvestidoBrl: "12345678.91234567",
    cotacaoAtual: "125000000.12345678",
    providerCotacao: "BRAPI",
    instanteCotacao: "2026-09-12T13:28:00Z",
    valorAtualOrigem: "12500000.26250001",
    valorAtualBrl: "12500000.26250001",
    lucroNaoRealizadoBrl: "154321.35015434",
    rentabilidadePercentual: "1.25000001",
  }],
};

export const position: Position = {
  id: "33333333-3333-4333-8333-333333333333",
  ativoId: assetId,
  ticker: "ACME3",
  nome: "ACME S.A.",
  mercado: "B3",
  moeda: "BRL",
  ativo: true,
  logoProvider: null,
  logoReference: null,
  quantidade: "0.10000001",
  precoMedioBrl: "123456789.12345678",
  totalInvestidoBrl: "12345678.91234567",
  lucroRealizadoAcumuladoBrl: "-123456789.12345678",
  ultimaAtualizacao: "2026-09-12T12:00:00Z",
};

export const transaction: Transaction = {
  id: "44444444-4444-4444-8444-444444444444",
  ativoId: assetId,
  ticker: "ACME3",
  nome: "ACME S.A.",
  logoProvider: null,
  logoReference: null,
  corretoraId: "55555555-5555-4555-8555-555555555555",
  corretoraNome: "XP Investimentos",
  exchangeRateId: null,
  tipo: "SELL",
  quantidade: "0.00000001",
  moeda: "BRL",
  precoUnitario: "123456789.12345678",
  taxas: "0.10000001",
  taxaCambioBrl: "1.00000000",
  valorTotalBrl: "1.23456789",
  resultadoRealizadoBrl: "-0.10000001",
  dataNegociacao: "2026-09-11T15:00:00Z",
  dataRegistro: "2026-09-11T15:01:00Z",
};

export const cashBalance: CashBalance = { saldoCaixaBrl: "1200.10000001" };
export const cashMovement: CashMovement = {
  id: "66666666-6666-4666-8666-666666666666",
  tipo: "DEPOSITO",
  valorBrl: "500.00000000",
  descricao: "Aporte mensal",
  dataHora: "2026-09-10T12:00:00Z",
};

export function pageOf<T>(item: T): Page<T> {
  return { items: [item], page: 0, size: 20, totalElements: 1, totalPages: 1 };
}
