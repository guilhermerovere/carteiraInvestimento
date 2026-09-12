package com.carteira.carteiraInvestimento.application.service;

import java.util.UUID;

public interface PortfolioValuationUseCase {
    PortfolioValuationResult summarize(UUID userId);
    PortfolioValuationResult refresh(UUID userId);
}
