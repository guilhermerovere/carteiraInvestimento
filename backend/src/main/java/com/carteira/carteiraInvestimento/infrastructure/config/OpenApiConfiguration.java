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
						.description("API de carteira com autenticação Bearer, caixa BRL e transações/posições B3 e US. As posições públicas não incluem valuation de mercado.")
						.version("v1"))
				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT Bearer para rotas protegidas.")));
	}
}

