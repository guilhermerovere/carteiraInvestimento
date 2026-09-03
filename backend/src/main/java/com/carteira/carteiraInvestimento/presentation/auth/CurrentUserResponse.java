package com.carteira.carteiraInvestimento.presentation.auth;

import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;

public record CurrentUserResponse(UUID id, String nome, String email, Role role, boolean ativo) {

	static CurrentUserResponse from(Usuario usuario) {
		return new CurrentUserResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.role(), usuario.ativo());
	}
}
