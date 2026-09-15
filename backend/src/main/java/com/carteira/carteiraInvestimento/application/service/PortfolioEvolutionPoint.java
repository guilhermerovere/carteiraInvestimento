package com.carteira.carteiraInvestimento.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioEvolutionPoint(LocalDate dataReferencia, BigDecimal totalInvestidoBrl,
        BigDecimal resultadoNaoRealizadoBrl, BigDecimal valorPosicoesBrl, BigDecimal patrimonioTotalBrl) { }
