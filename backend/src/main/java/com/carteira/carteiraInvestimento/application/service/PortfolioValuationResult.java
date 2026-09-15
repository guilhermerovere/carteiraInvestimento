package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioValuationResult(Instant valuationInstant, BigDecimal cashBalanceBrl,
        BigDecimal investedBrl, BigDecimal positionsValueBrl, BigDecimal unrealizedProfitBrl,
        BigDecimal accumulatedRealizedProfitBrl, BigDecimal totalEquityBrl,
        BigDecimal unrealizedReturnPercentage, CurrentExchangeRate currentExchangeRate,
        List<ValuedPosition> positions, boolean marketDataAvailable) {
    public record CurrentExchangeRate(BigDecimal rateBrl, CambioProvider provider, Instant exchangeInstant) { }
    public record ValuedPosition(UUID assetId, String ticker, TipoAtivo type, Mercado market, Moeda currency,
            BigDecimal quantity, BigDecimal averagePriceBrl, BigDecimal investedBrl,
            BigDecimal currentQuote, QuoteProvider quoteProvider, Instant quoteInstant,
            BigDecimal currentValueSource, BigDecimal currentValueBrl, BigDecimal unrealizedProfitBrl,
            BigDecimal returnPercentage, boolean marketDataAvailable) { }
}
