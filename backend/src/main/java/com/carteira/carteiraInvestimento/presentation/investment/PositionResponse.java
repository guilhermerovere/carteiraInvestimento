package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonPropertyOrder({"id","ativoId","ticker","quantidade","precoMedioBrl","totalInvestidoBrl",
        "lucroRealizadoAcumuladoBrl","ultimaAtualizacao"})
public record PositionResponse(UUID id, UUID ativoId, String ticker, BigDecimal quantidade,
        BigDecimal precoMedioBrl, BigDecimal totalInvestidoBrl,
        BigDecimal lucroRealizadoAcumuladoBrl, Instant ultimaAtualizacao) {
    static PositionResponse from(PositionView view) {
        var p = view.position();
        return new PositionResponse(p.id(), p.ativoId(), view.ticker(), p.quantidade(), p.precoMedioBrl(),
                p.totalInvestidoBrl(), p.lucroRealizadoAcumuladoBrl(), p.ultimaAtualizacao());
    }
}
