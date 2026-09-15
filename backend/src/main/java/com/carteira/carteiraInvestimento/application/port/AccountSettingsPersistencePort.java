package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountSettingsPersistencePort {
    Optional<Usuario> lockUser(UUID userId);
    Optional<WalletState> lockWallet(UUID userId);
    boolean hasOpenPositions(UUID walletId);

    record WalletState(UUID id, BigDecimal cashBalance) { }
}
