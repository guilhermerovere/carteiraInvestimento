package com.carteira.carteiraInvestimento.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtConfiguration;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtProperties;
import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController.class)
@Import({JwtConfiguration.class, ProblemDetailFactory.class, PersistedUserJwtAuthenticationConverter.class,
		PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController.class,
		PersistedUserJwtAuthenticationConverterTest.AdminProbeController.class,
		PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration.class})
@TestPropertySource(properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api"})
class PersistedUserJwtAuthenticationConverterTest {
	@Autowired private MockMvc mockMvc;
	@Autowired private JwtEncoder encoder;
	@Autowired private TestUsuarios usuarios;

	@BeforeEach
	void resetUser() {
		usuarios.user = usuario(Role.ROLE_USER, true);
	}

	@Test
	void buildsAuthoritiesFromTheCurrentPersistedRole() throws Exception {
		mockMvc.perform(get("/protected").header(HttpHeaders.AUTHORIZATION, bearer(token(usuarios.user.id(), Role.ROLE_USER))))
				.andExpect(status().isOk())
				.andExpect(content().string("ROLE_USER"));
	}

	@Test
	void returnsUnauthorizedWhenThePersistedUserWasRemoved() throws Exception {
		UUID id = usuarios.user.id();
		usuarios.user = null;
		assertUnauthorized(id, Role.ROLE_USER);
	}

	@Test
	void returnsUnauthorizedWhenThePersistedUserIsInactive() throws Exception {
		usuarios.user = usuario(Role.ROLE_USER, false);
		assertUnauthorized(usuarios.user.id(), Role.ROLE_USER);
	}

	@Test
	void returnsUnauthorizedWhenThePersistedRoleDivergesFromTheTokenClaim() throws Exception {
		usuarios.user = usuario(Role.ROLE_ADMIN, true);
		assertUnauthorized(usuarios.user.id(), Role.ROLE_USER);
	}

	@Test
	void returnsForbiddenForAnAuthenticatedPrincipalWithoutTheRequiredRole() throws Exception {
		mockMvc.perform(get("/admin-probe").header(HttpHeaders.AUTHORIZATION, bearer(token(usuarios.user.id(), Role.ROLE_USER))))
				.andExpect(status().isForbidden());
	}

	private void assertUnauthorized(UUID subject, Role tokenRole) throws Exception {
		mockMvc.perform(get("/protected").header(HttpHeaders.AUTHORIZATION, bearer(token(subject, tokenRole))))
				.andExpect(status().isUnauthorized());
	}

	private String token(UUID subject, Role role) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(subject.toString())
				.claim("role", role.name())
				.issuedAt(now)
				.expiresAt(now.plusSeconds(3600))
				.id(UUID.randomUUID().toString())
				.issuer("carteira-investimento-backend")
				.audience(java.util.List.of("carteira-investimento-api"))
				.build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

	private static Usuario usuario(Role role, boolean ativo) {
		Instant now = Instant.now();
		return new Usuario(UUID.randomUUID(), "Usuario", "usuario@example.test", "hash", role, ativo, now, now);
	}

	@RestController
	static class ProtectedProbeController {
		@GetMapping("/protected")
		String protectedEndpoint(org.springframework.security.core.Authentication authentication) {
			return authentication.getAuthorities().iterator().next().getAuthority();
		}
	}

	@RestController
	static class AdminProbeController {
		@GetMapping("/admin-probe")
		@PreAuthorize("hasRole('ADMIN')")
		String adminOnly() {
			return "admin";
		}
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableConfigurationProperties(JwtProperties.class)
	@EnableWebSecurity
	@EnableMethodSecurity
	static class SecurityTestConfiguration {
		@Bean
		TestUsuarios usuarioPort() {
			return new TestUsuarios();
		}

		@Bean
		SecurityFilterChain securityFilterChain(HttpSecurity http,
				PersistedUserJwtAuthenticationConverter converter) throws Exception {
			return http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
				.build();
		}
	}

	static class TestUsuarios implements UsuarioPort {
		private Usuario user;

		@Override
		public Optional<Usuario> findByEmail(String email) {
			return user != null && user.email().equals(email) ? Optional.of(user) : Optional.empty();
		}

		@Override
		public Optional<Usuario> findById(UUID id) {
			return user != null && user.id().equals(id) ? Optional.of(user) : Optional.empty();
		}

		@Override
		public Usuario save(Usuario usuario) {
			user = usuario;
			return usuario;
		}
	}
}
