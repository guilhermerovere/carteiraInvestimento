package com.carteira.carteiraInvestimento.domain.investment;

import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Transacao(UUID id, UUID carteiraId, UUID usuarioId, UUID ativoId, UUID corretoraId,
        UUID exchangeRateId, TipoTransacao tipo, BigDecimal quantidade, Moeda moeda,
        BigDecimal precoUnitario, BigDecimal taxas, BigDecimal taxaCambioBrl,
        BigDecimal valorTotalBrl, BigDecimal resultadoRealizadoBrl,
        Instant dataNegociacao, Instant dataRegistro) {
    public Transacao {
        Objects.requireNonNull(id); Objects.requireNonNull(carteiraId); Objects.requireNonNull(usuarioId);
        Objects.requireNonNull(ativoId); Objects.requireNonNull(corretoraId); Objects.requireNonNull(tipo);
        Objects.requireNonNull(quantidade); Objects.requireNonNull(moeda); Objects.requireNonNull(precoUnitario);
        Objects.requireNonNull(taxas); Objects.requireNonNull(taxaCambioBrl); Objects.requireNonNull(valorTotalBrl);
        Objects.requireNonNull(dataNegociacao); Objects.requireNonNull(dataRegistro);
        if ((tipo == TipoTransacao.BUY) != (resultadoRealizadoBrl == null))
            throw new FinancialStateException("invalid transaction realized result");
        if (moeda == Moeda.BRL && (exchangeRateId != null || taxaCambioBrl.compareTo(InvestmentNumbers.ONE_8) != 0))
            throw new FinancialStateException("invalid BRL exchange rate");
        if (moeda == Moeda.USD && exchangeRateId == null) throw new FinancialStateException("missing USD exchange rate");
    }
}
