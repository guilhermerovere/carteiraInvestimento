package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.service.InvestmentOperationResult;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;

@JsonPropertyOrder({"transacao","valorOrigem","saldoCaixaBrl","posicao"})
public record InvestmentOperationResponse(TransactionResponse transacao, BigDecimal valorOrigem,
        BigDecimal saldoCaixaBrl, PositionResponse posicao) {
    static InvestmentOperationResponse from(InvestmentOperationResult result) {
        return new InvestmentOperationResponse(TransactionResponse.from(result.transaction()), result.sourceValue(),
                result.resultingCashBalance(), PositionResponse.from(result.position()));
    }
}
