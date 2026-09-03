package com.carteira.carteiraInvestimento.presentation.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtConfiguration;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtProperties;
import com.carteira.carteiraInvestimento.infrastructure.security.PersistedUserJwtAuthenticationConverter;
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CurrentPrincipalController.class)
@Import({JwtConfiguration.class, ProblemDetailFactory.class, PersistedUserJwtAuthenticationConverter.class, CurrentPrincipalController.class,
		CurrentPrincipalControllerTest.SecurityTestConfiguration.class})
@TestPropertySource(properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api"})
class CurrentPrincipalControllerTest {
	@Autowired private MockMvc mockMvc;
	@Autowired private JwtEncoder encoder;
	@Autowired private TestUsuarios usuarios;

	@BeforeEach
	void setUp() {
		Instant now = Instant.now();
		usuarios.user = new Usuario(UUID.randomUUID(), "Ana", "ana@example.test", "hash", Role.ROLE_USER,
				true, now, now);
	}

	@Test
	void returnsOnlyThePersistedPrincipalAndIgnoresClientSuppliedUserId() throws Exception {
		UUID suppliedId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/auth/me")
				.param("usuarioId", suppliedId.toString())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token(usuarios.user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(usuarios.user.id().toString()))
				.andExpect(jsonPath("$.nome").value("Ana"))
				.andExpect(jsonPath("$.email").value("ana@example.test"))
				.andExpect(jsonPath("$.role").value("ROLE_USER"))
				.andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist())
				.andExpect(jsonPath("$.usuarioId").doesNotExist());
	}

	private String token(Usuario usuario) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(usuario.id().toString())
				.claim("role", usuario.role().name())
				.issuedAt(now)
				.expiresAt(now.plusSeconds(3600))
				.id(UUID.randomUUID().toString())
				.issuer("carteira-investimento-backend")
				.audience(java.util.List.of("carteira-investimento-api"))
				.build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableConfigurationProperties(JwtProperties.class)
	@EnableWebSecurity
	static class SecurityTestConfiguration {
		@Bean
		TestUsuarios usuarioPort() {
			return new TestUsuarios();
		}

		@Bean
		CurrentPrincipalService currentPrincipalService(TestUsuarios usuarios) {
			return new CurrentPrincipalService(usuarios);
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
