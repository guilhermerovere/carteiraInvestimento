package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.*;
import com.carteira.carteiraInvestimento.domain.identity.*;
import com.carteira.carteiraInvestimento.domain.wallet.Carteira;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class IdentityApplicationServiceTest {
	@Test void registrationOrchestratesUserWalletAndAuditWithoutFrameworks() {
		Fakes f = new Fakes(); Usuario user = new RegistrationService(f, f, f, f).register(" User ", " User@Example.Test ", "Valid@123", UUID.randomUUID());
		assertThat(user.role()).isEqualTo(Role.ROLE_USER); assertThat(f.wallets).hasSize(1); assertThat(f.events.getFirst().tipo()).isEqualTo(TipoEvento.CADASTRO_USUARIO);
	}
	@Test void loginCanonicalizesAuditsBeforeIssuingAndRejectsInactive() {
		Fakes f = new Fakes(); Usuario active = Usuario.novoUsuario("User", "user@example.test", "hash", Role.ROLE_USER); f.users.put(active.email(), active);
		AccessTokenIssuer issuer = user -> { assertThat(f.events.getLast().tipo()).isEqualTo(TipoEvento.LOGIN_SUCESSO); return new AccessTokenIssuer.IssuedAccessToken("token", 60); };
		assertThat(new LoginService(f, f, f, issuer).login(" USER@example.test ", "Valid@123", UUID.randomUUID()).value()).isEqualTo("token");
		assertThatThrownBy(() -> new LoginService(f, f, f, issuer).login("missing@example.test", "x", UUID.randomUUID())).isInstanceOf(AuthenticationFailedException.class);
		assertThat(f.events.getLast().usuarioId()).isNull();
		Usuario inactive = new Usuario(UUID.randomUUID(), "Inactive", "inactive@example.test", "hash", Role.ROLE_USER, false, Instant.now(), Instant.now()); f.users.put(inactive.email(), inactive);
		assertThatThrownBy(() -> new LoginService(f, f, f, issuer).login(inactive.email(), "Valid@123", UUID.randomUUID())).isInstanceOf(AuthenticationFailedException.class);
		assertThat(f.events.getLast().tipo()).isEqualTo(TipoEvento.USUARIO_INATIVO);
	}
	@Test void bootstrapIsIdempotentCreatesAdminWithoutWalletAndRejectsUserConflict() {
		Fakes f = new Fakes(); InitialAdminBootstrapService service = new InitialAdminBootstrapService(f, f, f);
		Usuario admin = service.provision("Admin", " ADMIN@example.test ", "Valid@123");
		assertThat(admin.role()).isEqualTo(Role.ROLE_ADMIN); assertThat(f.wallets).isEmpty(); assertThat(service.provision("Other", admin.email(), "Other@123").id()).isEqualTo(admin.id()); assertThat(f.users).hasSize(1);
		f.users.put("user@example.test", Usuario.novoUsuario("User", "user@example.test", "hash", Role.ROLE_USER));
		assertThatThrownBy(() -> service.provision("Admin", "user@example.test", "Valid@123")).isInstanceOf(IllegalStateException.class);
	}
	@Test void currentPrincipalUsesOnlyTheUserPort() {
		Fakes f = new Fakes(); Usuario user = Usuario.novoUsuario("User", "user@example.test", "hash", Role.ROLE_USER); f.save(user);
		assertThat(new CurrentPrincipalService(f).current(user.id())).isEqualTo(user);
	}
	private static final class Fakes implements UsuarioPort, CarteiraPort, AuditoriaPort, PasswordHasher {
		final Map<String, Usuario> users = new HashMap<>(); final List<Carteira> wallets = new ArrayList<>(); final List<AuditoriaCommand> events = new ArrayList<>();
		public Optional<Usuario> findByEmail(String email){ return Optional.ofNullable(users.get(email)); } public Optional<Usuario> findById(UUID id){ return users.values().stream().filter(u->u.id().equals(id)).findFirst(); } public Usuario save(Usuario u){users.put(u.email(),u);return u;} public Carteira save(Carteira c){wallets.add(c);return c;} public void record(AuditoriaCommand e){events.add(e);} public String hash(String p){return "hash";} public boolean matches(String p,String h){return "Valid@123".equals(p);}
	}
}
