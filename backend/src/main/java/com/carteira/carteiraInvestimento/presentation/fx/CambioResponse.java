package com.carteira.carteiraInvestimento.presentation.fx;

import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Observação persistida USD/BRL válida por cinco minutos desde registradoEm")
public record CambioResponse(UUID id, MoedaCambio moedaOrigem, MoedaCambio moedaDestino,
		@Schema(example = "5.12345678") BigDecimal taxa, Instant instanteCotacao,
		CambioProvider provider, Instant registradoEm) {
	static CambioResponse from(ObservacaoCambio value) {
		return new CambioResponse(value.id(), value.moedaOrigem(), value.moedaDestino(), value.taxa(),
				value.instanteCotacao(), value.provider(), value.registradoEm());
	}
}
