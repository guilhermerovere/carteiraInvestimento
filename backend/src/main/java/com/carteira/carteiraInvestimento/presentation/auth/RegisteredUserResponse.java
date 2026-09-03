package com.carteira.carteiraInvestimento.presentation.auth;

import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;

public record RegisteredUserResponse(UUID id, String nome, String email, Role role, boolean ativo) {
	static RegisteredUserResponse from(Usuario usuario) {
		return new RegisteredUserResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.role(), usuario.ativo());
	}
}
