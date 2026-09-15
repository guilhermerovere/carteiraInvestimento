package com.carteira.carteiraInvestimento.domain.identity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Usuario(
		UUID id,
		String nome,
		String email,
		String senhaHash,
		Role role,
		boolean ativo,
		Instant criadoEm,
		Instant atualizadoEm) {

	public Usuario {
		Objects.requireNonNull(id, "id must not be null");
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome must not be blank");
		}
		email = EmailCanonicalizer.canonicalize(email);
		if (senhaHash == null || senhaHash.isBlank()) {
			throw new IllegalArgumentException("senhaHash must not be blank");
		}
		Objects.requireNonNull(role, "role must not be null");
		Objects.requireNonNull(criadoEm, "criadoEm must not be null");
		Objects.requireNonNull(atualizadoEm, "atualizadoEm must not be null");
	}

	public static Usuario novoUsuario(String nome, String email, String senhaHash, Role role) {
		Instant now = Instant.now();
		return new Usuario(UUID.randomUUID(), nome.trim(), email, senhaHash, role, true, now, now);
	}

	public Usuario comNome(String novoNome, Instant instante) {
		if (novoNome == null || novoNome.trim().isEmpty() || novoNome.trim().length() > 255) {
			throw new IllegalArgumentException("nome must contain between 1 and 255 characters");
		}
		return new Usuario(id, novoNome.trim(), email, senhaHash, role, ativo, criadoEm, instante);
	}

	public Usuario comEmail(String novoEmail, Instant instante) {
		return new Usuario(id, nome, novoEmail, senhaHash, role, ativo, criadoEm, instante);
	}

	public Usuario comSenhaHash(String novoHash, Instant instante) {
		return new Usuario(id, nome, email, novoHash, role, ativo, criadoEm, instante);
	}

	public Usuario encerrar(String nomeAnonimo, String emailAnonimo, String hashInutilizavel, Instant instante) {
		return new Usuario(id, nomeAnonimo, emailAnonimo, hashInutilizavel, role, false, criadoEm, instante);
	}
}
