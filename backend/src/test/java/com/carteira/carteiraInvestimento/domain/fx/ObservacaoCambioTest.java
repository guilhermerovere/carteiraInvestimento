package com.carteira.carteiraInvestimento.domain.fx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ObservacaoCambioTest {
	private static final Instant QUOTED = Instant.parse("2026-09-10T12:00:00Z");
	private static final Instant RECORDED = Instant.parse("2026-09-10T12:00:01Z");

	@Test void preservesEightDecimalsAndPadsShorterValues() {
		assertThat(rate("5.12345678").taxa().toPlainString()).isEqualTo("5.12345678");
		assertThat(rate("5.12").taxa().toPlainString()).isEqualTo("5.12000000");
	}

	@Test void roundsMoreThanEightDecimalsOnceUsingHalfEvenIncludingTies() {
		assertThat(rate("5.123456789").taxa().toPlainString()).isEqualTo("5.12345679");
		assertThat(rate("1.234567845").taxa().toPlainString()).isEqualTo("1.23456784");
		assertThat(rate("1.234567855").taxa().toPlainString()).isEqualTo("1.23456786");
	}

	@Test void rejectsRoundToZeroOverflowZeroAndNegative() {
		for (String invalid : new String[]{"0.000000004", "10000000000.00000000", "0", "-1"}) {
			assertThatThrownBy(() -> rate(invalid)).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test void requiresCanonicalPairAndBothTimestamps() {
		assertThatThrownBy(() -> new ObservacaoCambio(UUID.randomUUID(), MoedaCambio.BRL, MoedaCambio.USD,
				BigDecimal.ONE, CambioProvider.ALPHA_VANTAGE, QUOTED, RECORDED)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ObservacaoCambio(UUID.randomUUID(), MoedaCambio.USD, MoedaCambio.BRL,
				BigDecimal.ONE, CambioProvider.ALPHA_VANTAGE, null, RECORDED)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new ObservacaoCambio(UUID.randomUUID(), MoedaCambio.USD, MoedaCambio.BRL,
				BigDecimal.ONE, CambioProvider.ALPHA_VANTAGE, QUOTED, null)).isInstanceOf(NullPointerException.class);
	}

	private ObservacaoCambio rate(String value) {
		return new ObservacaoCambio(UUID.randomUUID(), MoedaCambio.USD, MoedaCambio.BRL, new BigDecimal(value),
				CambioProvider.ALPHA_VANTAGE, QUOTED, RECORDED);
	}
}
