package com.carteira.carteiraInvestimento.domain.shared;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CanonicalFingerprint {
    public record Field(String type, String value) { }

    private CanonicalFingerprint() { }

    public static Field field(String type, Object value) {
        return new Field(type, value == null ? null : value.toString());
    }

    public static String sha256(Field... fields) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream record = new DataOutputStream(bytes)) {
                for (Field field : fields) write(record, field.type(), field.value());
            }
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()));
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 fingerprint unavailable", exception);
        }
    }

    private static void write(DataOutputStream target, String type, String value) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.UTF_8);
        target.writeInt(typeBytes.length);
        target.write(typeBytes);
        if (value == null) {
            target.writeInt(-1);
            return;
        }
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        target.writeInt(valueBytes.length);
        target.write(valueBytes);
    }
}
