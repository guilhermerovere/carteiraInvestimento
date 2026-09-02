package com.carteira.carteiraInvestimento.domain.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Carteira(UUID id, UUID usuarioId, String nome, BigDecimal saldoCaixaBrl, Instant dataCriacao) {

	public Carteira {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(usuarioId, "usuarioId must not be null");
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome must not be blank");
		}
		Objects.requireNonNull(saldoCaixaBrl, "saldoCaixaBrl must not be null");
		Objects.requireNonNull(dataCriacao, "dataCriacao must not be null");
	}

	public static Carteira principal(UUID usuarioId) {
		return new Carteira(UUID.randomUUID(), usuarioId, "Carteira Principal", BigDecimal.ZERO, Instant.now());
	}
}
