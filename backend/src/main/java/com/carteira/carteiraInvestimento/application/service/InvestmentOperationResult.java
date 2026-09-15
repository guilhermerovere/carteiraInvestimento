package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.TransactionView;
import java.math.BigDecimal;

public record InvestmentOperationResult(TransactionView transaction, BigDecimal sourceValue,
        BigDecimal resultingCashBalance, PositionView position) { }
