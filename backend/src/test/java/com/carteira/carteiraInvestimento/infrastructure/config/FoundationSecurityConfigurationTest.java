package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.security.PersistedUserJwtAuthenticationConverter;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import com.carteira.carteiraInvestimento.infrastructure.security.SecurityProblemDetailHandler;
import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = FoundationSecurityConfigurationTest.SecurityProbeController.class)
@Import({FoundationSecurityConfiguration.class, JwtConfiguration.class, PersistedUserJwtAuthenticationConverter.class,
		ProblemDetailFactory.class, AccessDeniedAuditingService.class, SecurityProblemDetailHandler.class,
		FoundationSecurityConfigurationTest.SecurityProbeController.class,
		FoundationSecurityConfigurationTest.SecurityTestConfiguration.class})
@TestPropertySource(properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api"})
class FoundationSecurityConfigurationTest {
	@Autowired private MockMvc mockMvc;

	@Test
	void permitsOnlyTheExplicitPublicRoutesForAnonymousRequests() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register"))
				.andExpect(status().isOk()).andExpect(header().exists("X-Correlation-ID"));
		mockMvc.perform(post("/api/v1/auth/login"))
				.andExpect(status().isOk()).andExpect(header().exists("X-Correlation-ID"));
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk()).andExpect(header().exists("X-Correlation-ID"));
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
		mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
		mockMvc.perform(get("/protected"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(header().exists("X-Correlation-ID"));
	}

	@RestController
	static class SecurityProbeController {
		@PostMapping("/api/v1/auth/register") void register() { }
		@PostMapping("/api/v1/auth/login") void login() { }
		@GetMapping("/actuator/health") void health() { }
		@GetMapping("/v3/api-docs") void openApi() { }
		@GetMapping("/swagger-ui.html") void swagger() { }
		@GetMapping("/protected") void protectedEndpoint() { }
	}

	@TestConfiguration(proxyBeanMethods = false)
	@EnableConfigurationProperties(JwtProperties.class)
	@EnableWebSecurity
	static class SecurityTestConfiguration {
		@Bean
		UsuarioPort usuarioPort() {
			return new UsuarioPort() {
				@Override public Optional<Usuario> findByEmail(String email) { return Optional.empty(); }
				@Override public Optional<Usuario> findById(UUID id) { return Optional.empty(); }
				@Override public Usuario save(Usuario usuario) { return usuario; }
			};
		}

		@Bean
		UserDetailsService userDetailsService() {
			return email -> { throw new UsernameNotFoundException("not found"); };
		}

		@Bean
		AuditoriaIsoladaPort auditoriaIsoladaPort() {
			return event -> { };
		}
	}
}
