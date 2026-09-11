package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.presentation.auth.CurrentPrincipalController;
import com.carteira.carteiraInvestimento.presentation.fx.CambioController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.junit.jupiter.api.Test;

class OpenApiConfigurationTest {
	@Test
	void publishesBearerForProtectedRoutes() throws Exception {
		var openApi = new OpenApiConfiguration().foundationOpenApi();

		assertThat(openApi.getComponents().getSecuritySchemes().get("bearerAuth").getType())
				.isEqualTo(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
		assertThat(openApi.getComponents().getSecuritySchemes().get("bearerAuth").getScheme()).isEqualTo("bearer");
		assertThat(CurrentPrincipalController.class.getAnnotation(SecurityRequirement.class).name()).isEqualTo("bearerAuth");
		assertThat(CambioController.class.getAnnotation(SecurityRequirement.class).name()).isEqualTo("bearerAuth");
		assertThat(CambioController.class.getDeclaredMethod("usdBrl").getAnnotation(io.swagger.v3.oas.annotations.Operation.class)
				.description()).contains("exchangeRateId", "B3");
	}
}
