package com.carteira.carteiraInvestimento.domain.wallet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (DataOutputStream record = new DataOutputStream(bytes)) {
				write(record, "version", "1");
				write(record, "type", type.name());
				write(record, "amount", amount(value).stripTrailingZeros().toPlainString());
				write(record, "description", description(description));
			}
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()));
		} catch (IOException | NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 fingerprint unavailable", exception);
		}
	}

	private static void write(DataOutputStream target, String tag, String value) throws IOException {
		byte[] tagBytes = tag.getBytes(StandardCharsets.UTF_8);
		target.writeInt(tagBytes.length);
		target.write(tagBytes);
		if (value == null) {
			target.writeInt(-1);
			return;
		}
		byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
		target.writeInt(valueBytes.length);
		target.write(valueBytes);
	}
}
