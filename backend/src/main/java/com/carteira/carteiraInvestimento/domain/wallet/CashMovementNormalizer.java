package com.carteira.carteiraInvestimento.domain.wallet;

import com.carteira.carteiraInvestimento.domain.shared.CanonicalFingerprint;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CashMovementNormalizer {
	public static final BigDecimal MAX_BRL = new BigDecimal("9999999999999999.99");

	private CashMovementNormalizer() {
	}

	public static BigDecimal amount(BigDecimal value) {
		if (value == null || value.signum() <= 0 || value.scale() > 2 || value.compareTo(MAX_BRL) > 0) {
			throw new IllegalArgumentException("invalid BRL amount");
		}
		return value.setScale(2, RoundingMode.UNNECESSARY);
	}

	public static String description(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		if (normalized.isEmpty()) {
			return null;
		}
		if (normalized.length() > 160) {
			throw new IllegalArgumentException("description is too long");
		}
		return normalized;
	}

	public static String fingerprint(TipoMovimentacaoCaixa type, BigDecimal value, String description) {
		return CanonicalFingerprint.sha256(
				CanonicalFingerprint.field("version", "1"),
				CanonicalFingerprint.field("type", type.name()),
				CanonicalFingerprint.field("amount", amount(value).stripTrailingZeros().toPlainString()),
				CanonicalFingerprint.field("description", description(description)));
	}
}
