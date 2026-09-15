package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AccountSettingsPersistencePort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.EmailCanonicalizer;
import com.carteira.carteiraInvestimento.domain.identity.PasswordPolicy;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.transaction.annotation.Transactional;

public class AccountSettingsApplicationService implements AccountSettingsUseCase {
    public static final String CLOSURE_CONFIRMATION = "EXCLUIR MINHA CONTA";
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UsuarioPort users;
    private final AccountSettingsPersistencePort persistence;
    private final PasswordHasher passwords;
    private final AuditoriaPort audit;
    private final Clock clock;

    public AccountSettingsApplicationService(UsuarioPort users, AccountSettingsPersistencePort persistence,
            PasswordHasher passwords, AuditoriaPort audit, Clock clock) {
        this.users = users;
        this.persistence = persistence;
        this.passwords = passwords;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Usuario updateName(UUID userId, String name, UUID correlationId, String endpoint) {
        Usuario current = current(userId);
        return users.save(current.comNome(name, now()));
    }

    @Override
    @Transactional
    public Usuario updateEmail(UUID userId, String email, UUID correlationId, String endpoint) {
        Usuario current = current(userId);
        String canonical = EmailCanonicalizer.canonicalize(email);
        if (canonical.length() > 320 || !EMAIL.matcher(canonical).matches()) {
            throw new IllegalArgumentException("invalid email");
        }
        if (canonical.equals(current.email())) return current;
        Usuario updated = users.save(current.comEmail(canonical, now()));
        audit.record(event(userId, TipoEvento.EMAIL_ALTERADO, endpoint, correlationId));
        return updated;
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword,
            UUID correlationId, String endpoint) {
        Usuario current = current(userId);
        verifyPassword(current, currentPassword);
        PasswordPolicy.validate(newPassword);
        users.save(current.comSenhaHash(passwords.hash(newPassword), now()));
        audit.record(event(userId, TipoEvento.SENHA_ALTERADA, endpoint, correlationId));
    }

    @Override
    @Transactional
    public void closeAccount(UUID userId, String currentPassword, String confirmation,
            UUID correlationId, String endpoint) {
        Usuario current = current(userId);
        verifyPassword(current, currentPassword);
        if (!CLOSURE_CONFIRMATION.equals(confirmation)) {
            throw new IllegalArgumentException("invalid closure confirmation");
        }
        if (current.role() == Role.ROLE_USER) {
            var wallet = persistence.lockWallet(userId).orElseThrow(PrimaryWalletMissingException::new);
            if (wallet.cashBalance().signum() != 0) {
                throw new AccountClosureConflictException(AccountClosureConflictException.Reason.CASH_NOT_ZERO);
            }
            if (persistence.hasOpenPositions(wallet.id())) {
                throw new AccountClosureConflictException(AccountClosureConflictException.Reason.OPEN_POSITIONS);
            }
        }
        String compactId = current.id().toString().replace("-", "");
        Instant instant = now();
        audit.record(event(userId, TipoEvento.CONTA_ENCERRADA, endpoint, correlationId));
        users.save(current.encerrar("Conta encerrada", "conta-encerrada+" + compactId + "@invalid.local",
                passwords.hash(UUID.randomUUID() + "!Aa1"), instant));
    }

    private Usuario current(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
        return persistence.lockUser(userId).filter(Usuario::ativo)
                .orElseThrow(() -> new IllegalStateException("active principal unavailable"));
    }

    private void verifyPassword(Usuario user, String currentPassword) {
        if (currentPassword == null || !passwords.matches(currentPassword, user.senhaHash())) {
            throw new IncorrectCurrentPasswordException();
        }
    }

    private AuditoriaCommand event(UUID userId, TipoEvento type, String endpoint, UUID correlationId) {
        return new AuditoriaCommand(userId, type, ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO,
                endpoint, correlationId, now());
    }

    private Instant now() { return clock.instant(); }
}
