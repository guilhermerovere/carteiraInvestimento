package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioValuationPort {
    record OpenPosition(UUID assetId, String ticker, TipoAtivo type, Mercado market, Moeda currency, boolean active,
            BigDecimal quantity, BigDecimal averagePriceBrl, BigDecimal investedBrl) { }
    record LocalState(UUID walletId, BigDecimal cashBalanceBrl, long stateVersion,
            List<OpenPosition> openPositions, BigDecimal accumulatedRealizedProfitBrl) { }
    record MaterializedTotals(BigDecimal investedBrl, BigDecimal positionsValueBrl,
            BigDecimal unrealizedProfitBrl, BigDecimal totalEquityBrl) { }

    LocalState readLocalState(UUID userId);
    boolean validateAndOptionallyMaterialize(UUID walletId, long expectedVersion, LocalDate referenceDate,
            Instant valuationInstant, BigDecimal cashBalanceBrl, MaterializedTotals totals, boolean materialize);
}
