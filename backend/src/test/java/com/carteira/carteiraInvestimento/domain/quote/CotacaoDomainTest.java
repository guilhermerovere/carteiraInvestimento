package com.carteira.carteiraInvestimento.domain.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CotacaoDomainTest {
	private static final Instant MARKET = Instant.parse("2026-09-08T18:30:00Z");
	private static final Instant RECEIVED = Instant.parse("2026-09-08T18:30:01Z");

	@Test
	void normalizesWithFourDecimalsAndHalfUp() {
		assertThat(Cotacao.normalizarPreco(new BigDecimal("123.456789"))).isEqualByComparingTo("123.4568");
		assertThat(Cotacao.normalizarPreco(new BigDecimal("1"))).isEqualByComparingTo("1.0000");
		assertThat(Cotacao.normalizarPreco(new BigDecimal("1.23445"))).isEqualByComparingTo("1.2345");
	}

	@Test
	void rejectsNonPositiveAndNumericOverflow() {
		for (String value : java.util.List.of("0", "-1", "100000000000.0000")) {
			assertThatThrownBy(() -> Cotacao.normalizarPreco(new BigDecimal(value)))
					.isInstanceOf(IllegalArgumentException.class);
		}
		assertThat(Cotacao.normalizarPreco(new BigDecimal("99999999999.99994")))
				.isEqualByComparingTo("99999999999.9999");
		assertThatThrownBy(() -> Cotacao.normalizarPreco(new BigDecimal("99999999999.99995")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void enforcesAssetCurrencyAndRequiredTimestamps() {
		Ativo b3 = Ativo.novo("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3);
		assertThatThrownBy(() -> Cotacao.aceitar(b3,
				new CotacaoExterna(BigDecimal.TEN, Moeda.USD, MARKET, QuoteProvider.ALPHA_VANTAGE), RECEIVED))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new CotacaoExterna(BigDecimal.TEN, Moeda.BRL, null, QuoteProvider.BRAPI))
				.isInstanceOf(NullPointerException.class);
		Cotacao accepted = Cotacao.aceitar(b3,
				new CotacaoExterna(new BigDecimal("10.12555"), Moeda.BRL, MARKET, QuoteProvider.BRAPI), RECEIVED);
		assertThat(accepted.preco()).isEqualByComparingTo("10.1256");
		assertThat(accepted.instanteCotacao()).isEqualTo(MARKET);
		assertThat(accepted.recebidoEm()).isEqualTo(RECEIVED);
	}
}
