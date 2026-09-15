package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InvestmentTransactionCommand(UUID assetId, UUID brokerId, TipoTransacao type,
        BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, OffsetDateTime tradeDate,
        UUID exchangeRateId) { }
