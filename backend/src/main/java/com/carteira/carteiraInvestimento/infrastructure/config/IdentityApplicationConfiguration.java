package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CarteiraPort;
import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService;
import com.carteira.carteiraInvestimento.application.service.LoginService;
import com.carteira.carteiraInvestimento.application.service.RegistrationService;
import com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService;
import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer;
import com.carteira.carteiraInvestimento.application.port.AccountSettingsPersistencePort;
import com.carteira.carteiraInvestimento.application.service.AccountSettingsApplicationService;
import com.carteira.carteiraInvestimento.application.service.AccountSettingsUseCase;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.service.AtivoApplicationService;
import com.carteira.carteiraInvestimento.application.service.AtivoUseCase;
import com.carteira.carteiraInvestimento.application.port.AssetMetadataProviderPort;
import com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class IdentityApplicationConfiguration {

	@Bean
	RegistrationService registrationService(UsuarioPort usuarios, CarteiraPort carteiras,
			AuditoriaPort auditoria, PasswordHasher passwordHasher) {
		return new RegistrationService(usuarios, carteiras, auditoria, passwordHasher);
	}

	@Bean
	LoginService loginService(UsuarioPort usuarios, PasswordHasher passwordHasher,
			AuditoriaPort auditoria, AuditoriaIsoladaPort auditoriaIsolada, AccessTokenIssuer tokenIssuer) {
		return new LoginService(usuarios, passwordHasher, auditoria, auditoriaIsolada, tokenIssuer);
	}

	@Bean
	CurrentPrincipalService currentPrincipalService(UsuarioPort usuarios) {
		return new CurrentPrincipalService(usuarios);
	}

	@Bean
	AccountSettingsUseCase accountSettingsUseCase(UsuarioPort usuarios, AccountSettingsPersistencePort persistence,
			PasswordHasher passwordHasher, AuditoriaPort auditoria, Clock clock) {
		return new AccountSettingsApplicationService(usuarios, persistence, passwordHasher, auditoria, clock);
	}

	@Bean
	InitialAdminBootstrapService initialAdminBootstrapService(UsuarioPort usuarios, PasswordHasher passwordHasher,
			AuditoriaPort auditoria) {
		return new InitialAdminBootstrapService(usuarios, passwordHasher, auditoria);
	}

	@Bean
	CommandLineRunner initialAdminRunner(InitialAdminBootstrapService bootstrap, AdminProperties admin) {
		return arguments -> bootstrap.provision(admin.name(), admin.email(), admin.password());
	}

	@Bean
	AtivoUseCase ativoUseCase(AtivoPort ativos, List<AssetMetadataProviderPort> metadataProviders,
			List<AssetDiscoveryProviderPort> discoveryProviders) {
		return new AtivoApplicationService(ativos, metadataProviders, discoveryProviders);
	}
}
