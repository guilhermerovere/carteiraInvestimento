package com.carteira.carteiraInvestimento.domain.broker;

public final class CnpjCanonicalizer {
    private CnpjCanonicalizer() { }

    public static String canonicalize(String value) {
        if (value == null) throw new IllegalArgumentException("cnpj is required");
        String input = value.trim();
        if (!input.matches("[0-9]{14}") && !input.matches("[0-9]{2}\\.[0-9]{3}\\.[0-9]{3}/[0-9]{4}-[0-9]{2}")) {
            throw new IllegalArgumentException("invalid cnpj");
        }
        String canonical = input.replaceAll("[.\\-/]", "");
        if (!canonical.matches("[0-9]{14}") || repeated(canonical) || !checksum(canonical)) {
            throw new IllegalArgumentException("invalid cnpj");
        }
        return canonical;
    }

    public static String canonicalPath(String value) {
        if (value == null || !value.matches("[0-9]{14}")) throw new IllegalArgumentException("invalid cnpj path");
        return canonicalize(value);
    }

    private static boolean repeated(String value) {
        return value.chars().allMatch(character -> character == value.charAt(0));
    }

    private static boolean checksum(String value) {
        return digit(value, 12) == value.charAt(12) - '0' && digit(value, 13) == value.charAt(13) - '0';
    }

    private static int digit(String value, int length) {
        int weight = length == 12 ? 5 : 6;
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += (value.charAt(index) - '0') * weight;
            weight = weight == 2 ? 9 : weight - 1;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
