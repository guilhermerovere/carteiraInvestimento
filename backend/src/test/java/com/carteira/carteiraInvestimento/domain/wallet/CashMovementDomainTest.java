package com.carteira.carteiraInvestimento.domain.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CashMovementDomainTest {
	@Test
	void normalizesEquivalentAmountsAndDescriptionsIntoTheSameFingerprint() {
		String first = CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("100"), "  origem  ");
		assertThat(first).isEqualTo(CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("100.0"), "origem"));
		assertThat(first).isEqualTo(CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("100.00"), "origem"));
		assertThat(CashMovementNormalizer.amount(new BigDecimal("100"))).isEqualTo(new BigDecimal("100.00"));
		assertThat(CashMovementNormalizer.amount(new BigDecimal("100.0"))).isEqualTo(new BigDecimal("100.00"));
		assertThat(CashMovementNormalizer.amount(new BigDecimal("100.00"))).isEqualTo(new BigDecimal("100.00"));
	}

	@Test
	void lengthPrefixesPreventDelimiterAndNullAmbiguity() {
		String delimited = CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("1"), "a|amount:2");
		String other = CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("1"), "a|amount:2|");
		String absent = CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("1"), null);
		String blank = CashMovementNormalizer.fingerprint(TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("1.00"), "   ");
		assertThat(delimited).isNotEqualTo(other);
		assertThat(absent).isEqualTo(blank);
		assertThat(delimited).matches("[0-9a-f]{64}");
	}

	@Test
	void rejectsNonPositiveExcessScaleAndNumericOverflowWithoutRounding() {
		assertThatThrownBy(() -> CashMovementNormalizer.amount(BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> CashMovementNormalizer.amount(new BigDecimal("1.001")))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> CashMovementNormalizer.amount(new BigDecimal("10000000000000000.00")))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
