package com.carteira.carteiraInvestimento.domain.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AtivoDomainTest {
	@Test
	void acceptsB3AndUsTickersAndCanonicalizesInput() {
		Ativo b3 = Ativo.novo(" petr4 ", " Petrobras ", TipoAtivo.ACAO, Mercado.B3);
		Ativo us = Ativo.novo(" aapl ", " Apple ", TipoAtivo.ETF, Mercado.US);
		assertThat(b3.ticker()).isEqualTo("PETR4");
		assertThat(b3.nome()).isEqualTo("Petrobras");
		assertThat(b3.moeda()).isEqualTo(Moeda.BRL);
		assertThat(us.ticker()).isEqualTo("AAPL");
		assertThat(us.moeda()).isEqualTo(Moeda.USD);
		assertThat(b3.ativo()).isTrue();
	}

	@Test
	void rejectsInvalidTickerForEachMarket() {
		assertThatThrownBy(() -> Ativo.novo("PETR", "Petrobras", TipoAtivo.ACAO, Mercado.B3))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Ativo.novo("BRK.B", "Berkshire", TipoAtivo.ACAO, Mercado.US))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejectsBlankAndOversizedNames() {
		assertThatThrownBy(() -> Ativo.novo("PETR4", "   ", TipoAtivo.ACAO, Mercado.B3))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Ativo.novo("PETR4", "x".repeat(161), TipoAtivo.ACAO, Mercado.B3))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void requiresControlledTypeAndConsistentCurrency() {
		assertThatThrownBy(() -> Ativo.novo("PETR4", "Petrobras", null, Mercado.B3))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new Ativo(UUID.randomUUID(), "PETR4", "Petrobras", TipoAtivo.ACAO,
				Mercado.B3, Moeda.USD, true, Instant.now(), Instant.now()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void lifecycleIsIdempotentAndIdentityIsPreservedAcrossAllowedChanges() {
		Ativo original = Ativo.novo("HGLG11", "Fundo", TipoAtivo.FII, Mercado.B3);
		Ativo inactive = original.definirAtivo(false);
		assertThat(inactive.definirAtivo(false)).isSameAs(inactive);
		Ativo renamed = inactive.renomear(" Novo Nome ");
		assertThat(renamed.nome()).isEqualTo("Novo Nome");
		assertThat(renamed.id()).isEqualTo(original.id());
		assertThat(renamed.ticker()).isEqualTo(original.ticker());
		assertThat(renamed.tipo()).isEqualTo(original.tipo());
		assertThat(renamed.mercado()).isEqualTo(original.mercado());
		assertThat(renamed.moeda()).isEqualTo(original.moeda());
	}
}
