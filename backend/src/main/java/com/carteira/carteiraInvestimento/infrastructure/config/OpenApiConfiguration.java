package com.carteira.carteiraInvestimento.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

	@Bean
	OpenAPI foundationOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Carteira Investimento API")
						.description("Fundação técnica da API; endpoints de negócio serão adicionados em changes futuras.")
						.version("v1"))
				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Esquema apenas preparatório; JWT ainda não está implementado.")));
	}
}

