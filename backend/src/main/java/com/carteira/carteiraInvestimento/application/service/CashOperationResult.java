package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import java.math.BigDecimal;

public record CashOperationResult(MovimentacaoCaixa movement, BigDecimal resultingBalance) { }
