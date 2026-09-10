package com.carteira.carteiraInvestimento.domain.broker;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record Corretora(UUID id, String cnpj, String razaoSocial, String nomeFantasia, String cep,
        String logradouro, String bairro, String cidade, String uf, String numero, String complemento,
        boolean ativo, Instant criadoEm, Instant atualizadoEm) {

    public Corretora {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(criadoEm, "criadoEm is required");
        Objects.requireNonNull(atualizadoEm, "atualizadoEm is required");
        cnpj = CnpjCanonicalizer.canonicalize(cnpj);
        razaoSocial = required(razaoSocial, 255, "razaoSocial");
        nomeFantasia = optional(nomeFantasia, 255, "nomeFantasia");
        cep = digits(cep, 8, "cep");
        logradouro = required(logradouro, 255, "logradouro");
        bairro = required(bairro, 160, "bairro");
        cidade = required(cidade, 160, "cidade");
        uf = required(uf, 2, "uf").toUpperCase(Locale.ROOT);
        if (!uf.matches("[A-Z]{2}")) throw new IllegalArgumentException("invalid uf");
        numero = normalizeNumero(numero);
        complemento = normalizeComplemento(complemento);
        if (atualizadoEm.isBefore(criadoEm)) throw new IllegalArgumentException("invalid timestamps");
    }

    public static Corretora nova(String cnpj, String razaoSocial, String nomeFantasia, String cep,
            String logradouro, String bairro, String cidade, String uf, String numero, String complemento,
            Clock clock) {
        Instant now = clock.instant();
        return new Corretora(UUID.randomUUID(), cnpj, razaoSocial, nomeFantasia, cep, logradouro, bairro,
                cidade, uf, numero, complemento, true, now, now);
    }

    public Corretora editar(String novoNumero, String novoComplemento, Clock clock) {
        String normalizedNumber = normalizeNumero(novoNumero);
        String normalizedComplement = normalizeComplemento(novoComplemento);
        if (Objects.equals(numero, normalizedNumber) && Objects.equals(complemento, normalizedComplement)) return this;
        return new Corretora(id, cnpj, razaoSocial, nomeFantasia, cep, logradouro, bairro, cidade, uf,
                normalizedNumber, normalizedComplement, ativo, criadoEm, nextInstant(clock));
    }

    public Corretora definirAtivo(boolean novoEstado, Clock clock) {
        if (ativo == novoEstado) return this;
        return new Corretora(id, cnpj, razaoSocial, nomeFantasia, cep, logradouro, bairro, cidade, uf,
                numero, complemento, novoEstado, criadoEm, nextInstant(clock));
    }

    public static String normalizeNumero(String value) { return optional(value, 20, "numero"); }
    public static String normalizeComplemento(String value) { return optional(value, 160, "complemento"); }
    private Instant nextInstant(Clock clock) { Instant candidate=clock.instant(); return candidate.isAfter(atualizadoEm)?candidate:atualizadoEm.plusNanos(1); }

    private static String required(String value, int max, String field) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > max) throw new IllegalArgumentException("invalid " + field);
        return normalized;
    }
    private static String optional(String value, int max, String field) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > max) throw new IllegalArgumentException("invalid " + field);
        return normalized;
    }
    private static String digits(String value, int length, String field) {
        String normalized = value == null ? "" : value.replaceAll("\\D", "");
        if (!normalized.matches("[0-9]{" + length + "}")) throw new IllegalArgumentException("invalid " + field);
        return normalized;
    }
}
