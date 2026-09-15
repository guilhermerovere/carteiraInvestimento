package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.Page;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.TransactionView;
import java.util.UUID;

public interface InvestmentUseCase {
    InvestmentOperationResult execute(UUID userId, InvestmentTransactionCommand command, String idempotencyKey,
            UUID correlationId, String endpoint);
    Page<TransactionView> transactions(UUID userId, int page, int size);
    TransactionView transaction(UUID userId, UUID transactionId);
    Page<PositionView> positions(UUID userId, int page, int size);
    PositionView position(UUID userId, UUID assetId);
}
