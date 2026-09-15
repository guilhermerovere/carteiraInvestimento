package com.carteira.carteiraInvestimento.domain.asset;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;

public record Ativo(
		UUID id,
		String ticker,
		String nome,
		TipoAtivo tipo,
		Mercado mercado,
		Moeda moeda,
		boolean ativo,
		LogoProvider logoProvider,
		String logoReference,
		Instant criadoEm,
		Instant atualizadoEm) {

	public Ativo {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(tipo, "tipo must not be null");
		Objects.requireNonNull(mercado, "mercado must not be null");
		Objects.requireNonNull(moeda, "moeda must not be null");
		Objects.requireNonNull(criadoEm, "criadoEm must not be null");
		Objects.requireNonNull(atualizadoEm, "atualizadoEm must not be null");
		ticker = TickerCanonicalizer.canonicalize(ticker);
		nome = NomeAtivoCanonicalizer.canonicalize(nome);
		if (!mercado.aceitaTicker(ticker)) {
			throw new IllegalArgumentException("ticker format is invalid for mercado");
		}
		if (moeda != mercado.moeda()) {
			throw new IllegalArgumentException("moeda is inconsistent with mercado");
		}
		if ((logoProvider == null) != (logoReference == null)) {
			throw new IllegalArgumentException("logo provider and reference must be supplied together");
		}
		if (logoReference != null) {
			logoReference = logoReference.trim();
			if (logoReference.isEmpty() || logoReference.length() > 512) {
				throw new IllegalArgumentException("invalid logo reference");
			}
		}
	}

	public Ativo(UUID id, String ticker, String nome, TipoAtivo tipo, Mercado mercado, Moeda moeda,
			boolean ativo, Instant criadoEm, Instant atualizadoEm) {
		this(id, ticker, nome, tipo, mercado, moeda, ativo, null, null, criadoEm, atualizadoEm);
	}

	public static Ativo novo(String ticker, String nome, TipoAtivo tipo, Mercado mercado) {
		Objects.requireNonNull(mercado, "mercado must not be null");
		Instant now = Instant.now();
		return new Ativo(UUID.randomUUID(), ticker, nome, tipo, mercado, mercado.moeda(), true, null, null, now, now);
	}

	public static Ativo novo(String ticker, String nome, TipoAtivo tipo, Mercado mercado,
			LogoProvider logoProvider, String logoReference) {
		Instant now = Instant.now();
		return new Ativo(UUID.randomUUID(), ticker, nome, tipo, mercado, mercado.moeda(), true,
				logoProvider, logoReference, now, now);
	}

	public Ativo renomear(String novoNome) {
		String canonical = NomeAtivoCanonicalizer.canonicalize(novoNome);
		if (nome.equals(canonical)) {
			return this;
		}
		return new Ativo(id, ticker, canonical, tipo, mercado, moeda, ativo, logoProvider, logoReference,
				criadoEm, Instant.now());
	}

	public Ativo definirAtivo(boolean novoEstado) {
		if (ativo == novoEstado) {
			return this;
		}
		return new Ativo(id, ticker, nome, tipo, mercado, moeda, novoEstado, logoProvider, logoReference,
				criadoEm, Instant.now());
	}
}
