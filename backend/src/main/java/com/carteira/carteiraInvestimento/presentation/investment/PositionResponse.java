package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;

@JsonPropertyOrder({"id","ativoId","ticker","nome","mercado","moeda","ativo","logoProvider","logoReference","quantidade","precoMedioBrl","totalInvestidoBrl",
        "lucroRealizadoAcumuladoBrl","ultimaAtualizacao"})
public record PositionResponse(UUID id, UUID ativoId, String ticker, String nome, Mercado mercado, Moeda moeda,
        boolean ativo, LogoProvider logoProvider, String logoReference, BigDecimal quantidade,
        BigDecimal precoMedioBrl, BigDecimal totalInvestidoBrl,
        BigDecimal lucroRealizadoAcumuladoBrl, Instant ultimaAtualizacao) {
    static PositionResponse from(PositionView view) {
        var p = view.position();
        return new PositionResponse(p.id(), p.ativoId(), view.ticker(), view.name(), view.market(), view.currency(),
                view.active(), view.logoProvider(), view.logoReference(), p.quantidade(), p.precoMedioBrl(),
                p.totalInvestidoBrl(), p.lucroRealizadoAcumuladoBrl(), p.ultimaAtualizacao());
    }
}
