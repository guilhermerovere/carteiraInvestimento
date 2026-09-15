package com.carteira.carteiraInvestimento.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.carteira.carteiraInvestimento.infrastructure.security.PersistedUserJwtAuthenticationConverter;
import com.carteira.carteiraInvestimento.infrastructure.security.SecurityProblemDetailHandler;
import org.springframework.http.HttpMethod;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class FoundationSecurityConfiguration {
	@Bean
	PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
		provider.setPasswordEncoder(passwordEncoder);
		return provider::authenticate;
	}

	@Bean
	SecurityFilterChain foundationSecurityFilterChain(HttpSecurity http,
			PersistedUserJwtAuthenticationConverter persistedUserConverter,
			SecurityProblemDetailHandler securityProblems) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
						.requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.authenticationEntryPoint(securityProblems)
						.jwt(jwt -> jwt.jwtAuthenticationConverter(persistedUserConverter)))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(securityProblems)
						.accessDeniedHandler(securityProblems))
				.build();
	}
}

