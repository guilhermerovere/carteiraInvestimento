package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.TransactionView;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonPropertyOrder({"id","ativoId","ticker","nome","logoProvider","logoReference","corretoraId","corretoraNome","exchangeRateId","tipo","quantidade","moeda",
        "precoUnitario","taxas","taxaCambioBrl","valorTotalBrl","resultadoRealizadoBrl","dataNegociacao","dataRegistro"})
public record TransactionResponse(UUID id, UUID ativoId, String ticker, String nome,
        com.carteira.carteiraInvestimento.domain.shared.LogoProvider logoProvider, String logoReference,
        UUID corretoraId, String corretoraNome,
        UUID exchangeRateId, TipoTransacao tipo, BigDecimal quantidade, Moeda moeda,
        BigDecimal precoUnitario, BigDecimal taxas, BigDecimal taxaCambioBrl,
        BigDecimal valorTotalBrl, BigDecimal resultadoRealizadoBrl,
        Instant dataNegociacao, Instant dataRegistro) {
    static TransactionResponse from(TransactionView view) {
        var t = view.transaction();
        return new TransactionResponse(t.id(), t.ativoId(), view.ticker(), view.assetName(), view.assetLogoProvider(), view.assetLogoReference(), t.corretoraId(), view.brokerName(), t.exchangeRateId(),
                t.tipo(), t.quantidade(), t.moeda(), t.precoUnitario(), t.taxas(), t.taxaCambioBrl(),
                t.valorTotalBrl(), t.resultadoRealizadoBrl(), t.dataNegociacao(), t.dataRegistro());
    }
}
