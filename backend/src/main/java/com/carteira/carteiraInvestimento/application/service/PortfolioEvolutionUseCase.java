package com.carteira.carteiraInvestimento.application.service;

import java.util.List;
import java.util.UUID;

public interface PortfolioEvolutionUseCase {
    List<PortfolioEvolutionPoint> read(UUID userId);
}
