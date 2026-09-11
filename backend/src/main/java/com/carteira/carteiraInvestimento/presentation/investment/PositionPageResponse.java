package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.Page;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import java.util.List;

public record PositionPageResponse(List<PositionResponse> items, int page, int size,
        long totalElements, int totalPages) {
    static PositionPageResponse from(Page<PositionView> page) {
        return new PositionPageResponse(page.items().stream().map(PositionResponse::from).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
