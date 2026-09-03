package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.presentation.auth.CurrentPrincipalController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.junit.jupiter.api.Test;

class OpenApiConfigurationTest {
	@Test
	void publishesBearerForProtectedRoutes() {
		var openApi = new OpenApiConfiguration().foundationOpenApi();

		assertThat(openApi.getComponents().getSecuritySchemes().get("bearerAuth").getType())
				.isEqualTo(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
		assertThat(openApi.getComponents().getSecuritySchemes().get("bearerAuth").getScheme()).isEqualTo("bearer");
		assertThat(CurrentPrincipalController.class.getAnnotation(SecurityRequirement.class).name()).isEqualTo("bearerAuth");
	}
}
