package com.carteira.carteiraInvestimento.presentation.valuation;

import com.carteira.carteiraInvestimento.application.service.PortfolioValuationResult;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioValuationResponse(Instant valuationInstant, BigDecimal saldoCaixaBrl,
        BigDecimal totalInvestidoBrl, BigDecimal valorPosicoesBrl, BigDecimal lucroNaoRealizadoBrl,
        BigDecimal lucroRealizadoAcumuladoBrl, BigDecimal patrimonioTotalBrl,
        BigDecimal rentabilidadeNaoRealizadaPercentual, CambioAtualResponse cambioAtual,
        List<ValuedPositionResponse> posicoes, boolean cotacoesDisponiveis) {

    public static PortfolioValuationResponse from(PortfolioValuationResult value) {
        CambioAtualResponse exchange = value.currentExchangeRate() == null ? null
                : CambioAtualResponse.from(value.currentExchangeRate());
        return new PortfolioValuationResponse(value.valuationInstant(), value.cashBalanceBrl(), value.investedBrl(),
                value.positionsValueBrl(), value.unrealizedProfitBrl(), value.accumulatedRealizedProfitBrl(),
                value.totalEquityBrl(), value.unrealizedReturnPercentage(), exchange,
                value.positions().stream().map(ValuedPositionResponse::from).toList(), value.marketDataAvailable());
    }

    public record CambioAtualResponse(BigDecimal taxaCambioBrl, CambioProvider provider, Instant instanteCambio) {
        static CambioAtualResponse from(PortfolioValuationResult.CurrentExchangeRate value) {
            return new CambioAtualResponse(value.rateBrl(), value.provider(), value.exchangeInstant());
        }
    }

    public record ValuedPositionResponse(UUID ativoId, String ticker, Mercado mercado, Moeda moeda,
            BigDecimal quantidade, BigDecimal precoMedioBrl, BigDecimal totalInvestidoBrl,
            BigDecimal cotacaoAtual, QuoteProvider providerCotacao, Instant instanteCotacao,
            BigDecimal valorAtualOrigem, BigDecimal valorAtualBrl, BigDecimal lucroNaoRealizadoBrl,
            BigDecimal rentabilidadePercentual, boolean cotacaoDisponivel) {
        static ValuedPositionResponse from(PortfolioValuationResult.ValuedPosition value) {
            return new ValuedPositionResponse(value.assetId(), value.ticker(), value.market(), value.currency(),
                    value.quantity(), value.averagePriceBrl(), value.investedBrl(), value.currentQuote(),
                    value.quoteProvider(), value.quoteInstant(), value.currentValueSource(), value.currentValueBrl(),
                    value.unrealizedProfitBrl(), value.returnPercentage(), value.marketDataAvailable());
        }
    }
}
