package com.carteira.carteiraInvestimento.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioEvolutionPort {
    record MaterializedSnapshot(LocalDate referenceDate, BigDecimal investedBrl, BigDecimal unrealizedProfitBrl,
            BigDecimal positionsValueBrl, BigDecimal totalNetWorthBrl) { }

    List<MaterializedSnapshot> findMaterializedSnapshots(UUID userId);
}
