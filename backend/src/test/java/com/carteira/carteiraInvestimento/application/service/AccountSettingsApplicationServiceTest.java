package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AccountSettingsPersistencePort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountSettingsApplicationServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-13T12:00:00Z");
    private final UUID correlationId = UUID.randomUUID();
    private Fakes fakes;
    private AccountSettingsApplicationService service;
    private Usuario user;

    @BeforeEach
    void setUp() {
        fakes = new Fakes();
        user = new Usuario(UUID.randomUUID(), "Ada", "ada@example.test", "hash:Atual123!", Role.ROLE_USER,
                true, NOW.minusSeconds(3600), NOW.minusSeconds(3600));
        fakes.users.put(user.id(), user);
        fakes.wallet = new AccountSettingsPersistencePort.WalletState(UUID.randomUUID(), BigDecimal.ZERO);
        service = new AccountSettingsApplicationService(fakes, fakes, fakes, fakes,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void userUpdatesOnlyOwnNameWithDomainTrimAndLengthValidation() {
        Usuario updated = service.updateName(user.id(), "  Ada Lovelace  ", correlationId, "/api/v1/account/profile");
        assertThat(updated.nome()).isEqualTo("Ada Lovelace");
        assertThat(updated.email()).isEqualTo(user.email());
        assertThat(updated.atualizadoEm()).isEqualTo(NOW);
        assertThatThrownBy(() -> service.updateName(UUID.randomUUID(), "Outra", correlationId, "/profile"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.updateName(user.id(), " ", correlationId, "/profile"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emailIsCanonicalValidUniqueAndImmediatelyPersisted() {
        Usuario updated = service.updateEmail(user.id(), "  NOVO@EXAMPLE.TEST  ", correlationId,
                "/api/v1/account/profile");
        assertThat(updated.email()).isEqualTo("novo@example.test");
        assertThat(fakes.events).extracting(AuditoriaCommand::tipo).containsExactly(TipoEvento.EMAIL_ALTERADO);
        assertThatThrownBy(() -> service.updateEmail(user.id(), "invalido", correlationId, "/profile"))
                .isInstanceOf(IllegalArgumentException.class);

        Usuario other = new Usuario(UUID.randomUUID(), "Outra", "ocupado@example.test", "hash:x", Role.ROLE_USER,
                true, NOW, NOW);
        fakes.users.put(other.id(), other);
        assertThatThrownBy(() -> service.updateEmail(user.id(), "OCUPADO@EXAMPLE.TEST", correlationId, "/profile"))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void passwordRequiresCurrentBcryptEquivalentAndExistingPolicyThenStoresOnlyHash() {
        service.changePassword(user.id(), "Atual123!", "NovaSenha123!", correlationId,
                "/api/v1/account/password");
        Usuario updated = fakes.users.get(user.id());
        assertThat(updated.senhaHash()).isEqualTo("hash:NovaSenha123!").doesNotContain("Atual123!");
        assertThat(fakes.events).extracting(AuditoriaCommand::tipo).containsExactly(TipoEvento.SENHA_ALTERADA);
        assertThatThrownBy(() -> service.changePassword(user.id(), "incorreta", "OutraSenha123!", correlationId, "/password"))
                .isInstanceOf(IncorrectCurrentPasswordException.class);
        assertThatThrownBy(() -> service.changePassword(user.id(), "NovaSenha123!", "fraca", correlationId, "/password"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closureRevalidatesLockedCashAndBlocksNonZeroBalance() {
        fakes.wallet = new AccountSettingsPersistencePort.WalletState(fakes.wallet.id(), new BigDecimal("0.00000001"));
        assertThatThrownBy(() -> close(user.id())).isInstanceOfSatisfying(AccountClosureConflictException.class,
                exception -> assertThat(exception.reason()).isEqualTo(AccountClosureConflictException.Reason.CASH_NOT_ZERO));
        assertThat(fakes.lockWalletCalls).isEqualTo(1);
        assertThat(fakes.saveCalls).isZero();
    }

    @Test
    void closureBlocksOpenPositionButIgnoresHistoricalZeroPositions() {
        fakes.openPositions = true;
        assertThatThrownBy(() -> close(user.id())).isInstanceOfSatisfying(AccountClosureConflictException.class,
                exception -> assertThat(exception.reason()).isEqualTo(AccountClosureConflictException.Reason.OPEN_POSITIONS));
        fakes.openPositions = false;
        close(user.id());
        assertThat(fakes.users.get(user.id()).ativo()).isFalse();
    }

    @Test
    void safeClosureAnonymizesIdentityPreservesRoleAndAuditsWithoutSecrets() {
        close(user.id());
        Usuario closed = fakes.users.get(user.id());
        assertThat(closed.ativo()).isFalse();
        assertThat(closed.nome()).isEqualTo("Conta encerrada");
        assertThat(closed.email()).isEqualTo("conta-encerrada+" + user.id().toString().replace("-", "") + "@invalid.local");
        assertThat(closed.role()).isEqualTo(Role.ROLE_USER);
        assertThat(closed.senhaHash()).startsWith("hash:").doesNotContain("Atual123!");
        assertThat(fakes.events).singleElement().satisfies(event -> {
            assertThat(event.tipo()).isEqualTo(TipoEvento.CONTA_ENCERRADA);
            assertThat(event.usuarioId()).isEqualTo(user.id());
            assertThat(event.endpoint()).isEqualTo("/api/v1/account/closure");
            assertThat(event.correlationId()).isEqualTo(correlationId);
            assertThat(event.toString()).doesNotContain("Atual123!", "hash:", "Authorization", "JWT");
        });
        assertThatThrownBy(() -> close(user.id())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void confirmationAndCurrentPasswordAreMandatory() {
        assertThatThrownBy(() -> service.closeAccount(user.id(), "incorreta", "EXCLUIR MINHA CONTA", correlationId, "/closure"))
                .isInstanceOf(IncorrectCurrentPasswordException.class);
        assertThatThrownBy(() -> service.closeAccount(user.id(), "Atual123!", "sim", correlationId, "/closure"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(fakes.saveCalls).isZero();
    }

    @Test
    void adminCanCloseOwnIdentityWithoutCreatingPersonalWallet() {
        Usuario admin = new Usuario(UUID.randomUUID(), "Admin", "admin@example.test", "hash:Atual123!",
                Role.ROLE_ADMIN, true, NOW, NOW);
        fakes.users.put(admin.id(), admin);
        close(admin.id());
        assertThat(fakes.users.get(admin.id()).ativo()).isFalse();
        assertThat(fakes.lockWalletCalls).isZero();
    }

    private void close(UUID id) {
        service.closeAccount(id, "Atual123!", AccountSettingsApplicationService.CLOSURE_CONFIRMATION,
                correlationId, "/api/v1/account/closure");
    }

    private static final class Fakes implements UsuarioPort, AccountSettingsPersistencePort, PasswordHasher, AuditoriaPort {
        final Map<UUID, Usuario> users = new LinkedHashMap<>();
        final List<AuditoriaCommand> events = new ArrayList<>();
        WalletState wallet;
        boolean openPositions;
        int lockWalletCalls;
        int saveCalls;

        @Override public Optional<Usuario> findByEmail(String email) { return users.values().stream().filter(u -> u.email().equals(email)).findFirst(); }
        @Override public Optional<Usuario> findById(UUID id) { return Optional.ofNullable(users.get(id)); }
        @Override public Optional<Usuario> lockUser(UUID userId) { return Optional.ofNullable(users.get(userId)); }
        @Override public Optional<WalletState> lockWallet(UUID userId) { lockWalletCalls++; return Optional.ofNullable(wallet); }
        @Override public boolean hasOpenPositions(UUID walletId) { return openPositions; }
        @Override public Usuario save(Usuario usuario) {
            boolean duplicate = users.values().stream().anyMatch(existing -> !existing.id().equals(usuario.id()) && existing.email().equalsIgnoreCase(usuario.email()));
            if (duplicate) throw new DuplicateEmailException();
            users.put(usuario.id(), usuario); saveCalls++; return usuario;
        }
        @Override public String hash(String password) { return "hash:" + password; }
        @Override public boolean matches(String password, String hash) { return ("hash:" + password).equals(hash); }
        @Override public void record(AuditoriaCommand event) { events.add(event); }
    }
}
