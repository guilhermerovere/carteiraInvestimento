package com.carteira.carteiraInvestimento.presentation.account;

import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;

public record AccountProfileResponse(UUID id, String nome, String email, Role role, boolean ativo) {
    static AccountProfileResponse from(Usuario user) {
        return new AccountProfileResponse(user.id(), user.nome(), user.email(), user.role(), user.ativo());
    }
}
