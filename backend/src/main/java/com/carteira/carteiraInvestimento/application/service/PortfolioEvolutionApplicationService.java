package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.PortfolioEvolutionPort;
import java.util.List;
import java.util.UUID;

public class PortfolioEvolutionApplicationService implements PortfolioEvolutionUseCase {
    private final PortfolioEvolutionPort persistence;

    public PortfolioEvolutionApplicationService(PortfolioEvolutionPort persistence) { this.persistence = persistence; }

    @Override
    public List<PortfolioEvolutionPoint> read(UUID userId) {
        return persistence.findMaterializedSnapshots(userId).stream()
                .map(snapshot -> new PortfolioEvolutionPoint(snapshot.referenceDate(), snapshot.investedBrl(),
                        snapshot.unrealizedProfitBrl(), snapshot.positionsValueBrl(), snapshot.totalNetWorthBrl()))
                .toList();
    }
}
