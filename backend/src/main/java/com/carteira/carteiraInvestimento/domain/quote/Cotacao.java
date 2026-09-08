package com.carteira.carteiraInvestimento.domain.quote;

import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Cotacao(UUID id, UUID ativoId, BigDecimal preco, Moeda moeda,
		Instant instanteCotacao, QuoteProvider provider, Instant recebidoEm) {
	private static final int SCALE = 4;
	private static final int INTEGER_DIGITS = 11;

	public Cotacao {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(ativoId, "ativoId must not be null");
		Objects.requireNonNull(moeda, "moeda must not be null");
		Objects.requireNonNull(instanteCotacao, "instanteCotacao must not be null");
		Objects.requireNonNull(provider, "provider must not be null");
		Objects.requireNonNull(recebidoEm, "recebidoEm must not be null");
		preco = normalizarPreco(preco);
	}

	public static Cotacao aceitar(Ativo ativo, CotacaoExterna externa, Instant recebidoEm) {
		Objects.requireNonNull(ativo, "ativo must not be null");
		Objects.requireNonNull(externa, "external quote must not be null");
		if (externa.moeda() != ativo.moeda()) {
			throw new IllegalArgumentException("quote currency is inconsistent with asset");
		}
		return new Cotacao(UUID.randomUUID(), ativo.id(), externa.preco(), externa.moeda(),
				externa.instanteCotacao(), externa.provider(), recebidoEm);
	}

	public static BigDecimal normalizarPreco(BigDecimal value) {
		Objects.requireNonNull(value, "price must not be null");
		if (value.signum() <= 0) {
			throw new IllegalArgumentException("price must be positive");
		}
		BigDecimal normalized = value.setScale(SCALE, RoundingMode.HALF_UP);
		if (normalized.signum() <= 0 || normalized.precision() - normalized.scale() > INTEGER_DIGITS) {
			throw new IllegalArgumentException("price exceeds NUMERIC(15,4)");
		}
		return normalized;
	}
}
