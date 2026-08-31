package com.carteira.carteiraInvestimento.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary permissive security for the technical foundation.
 *
 * <p>This configuration must be replaced by the identity/authentication
 * OpenSpec change. It intentionally provides no login, user, JWT or RBAC.</p>
 */
@Configuration(proxyBeanMethods = false)
public class FoundationSecurityConfiguration {

	@Bean
	SecurityFilterChain foundationSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.build();
	}
}

