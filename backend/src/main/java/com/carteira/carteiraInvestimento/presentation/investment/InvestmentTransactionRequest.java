package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.service.InvestmentTransactionCommand;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({"ativoId","corretoraId","tipo","quantidade","precoUnitario","taxas","dataNegociacao","exchangeRateId"})
public final class InvestmentTransactionRequest {
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED) private final UUID ativoId;
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED) private final UUID corretoraId;
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED,allowableValues={"BUY","SELL"}) private final TipoTransacao tipo;
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED,minimum="0",exclusiveMinimum=true,maximum="9999999999.99999999",description="Até 8 casas decimais, sem arredondamento de input") private final BigDecimal quantidade;
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED,minimum="0",exclusiveMinimum=true,maximum="9999999999.99999999",description="Preço confirmado pelo usuário; até 8 casas") private final BigDecimal precoUnitario;
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED,minimum="0",maximum="9999999999.99999999",description="Taxas em BRL; até 8 casas") private final BigDecimal taxas;
    @Schema(requiredMode=Schema.RequiredMode.REQUIRED,type="string",format="date-time",description="ISO-8601 com offset obrigatório; normalizado para microssegundos") private final OffsetDateTime dataNegociacao;
    @Schema(description="Ausente para B3; obrigatório para US e resolvido apenas no histórico local USD/BRL") private final UUID exchangeRateId;

    @JsonCreator
    public InvestmentTransactionRequest(@JsonProperty("ativoId") UUID ativoId,
            @JsonProperty("corretoraId") UUID corretoraId, @JsonProperty("tipo") TipoTransacao tipo,
            @JsonProperty("quantidade") BigDecimal quantidade,
            @JsonProperty("precoUnitario") BigDecimal precoUnitario,
            @JsonProperty("taxas") BigDecimal taxas,
            @JsonProperty("dataNegociacao") OffsetDateTime dataNegociacao,
            @JsonProperty("exchangeRateId") UUID exchangeRateId) {
        this.ativoId=ativoId; this.corretoraId=corretoraId; this.tipo=tipo; this.quantidade=quantidade;
        this.precoUnitario=precoUnitario; this.taxas=taxas; this.dataNegociacao=dataNegociacao;
        this.exchangeRateId=exchangeRateId;
    }

    InvestmentTransactionCommand command() {
        return new InvestmentTransactionCommand(ativoId, corretoraId, tipo, quantidade, precoUnitario,
                taxas, dataNegociacao, exchangeRateId);
    }

    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("unknown investment transaction field");
    }
}
