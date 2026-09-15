package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;

public interface AccountSettingsUseCase {
    Usuario updateName(UUID userId, String name, UUID correlationId, String endpoint);
    Usuario updateEmail(UUID userId, String email, UUID correlationId, String endpoint);
    void changePassword(UUID userId, String currentPassword, String newPassword, UUID correlationId, String endpoint);
    void closeAccount(UUID userId, String currentPassword, String confirmation, UUID correlationId, String endpoint);
}
