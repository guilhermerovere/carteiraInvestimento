package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.Page;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.TransactionView;
import java.util.List;

public record TransactionPageResponse(List<TransactionResponse> items, int page, int size,
        long totalElements, int totalPages) {
    static TransactionPageResponse from(Page<TransactionView> page) {
        return new TransactionPageResponse(page.items().stream().map(TransactionResponse::from).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
