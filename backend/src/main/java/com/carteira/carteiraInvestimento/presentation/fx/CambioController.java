package com.carteira.carteiraInvestimento.presentation.fx;

import com.carteira.carteiraInvestimento.application.service.CambioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Future US BUY/SELL clients must send the returned exchangeRateId, never a numeric browser rate.
 * US will require the id and validate USD/BRL plus now <= registradoEm + 5 minutes; B3 will not use it.
 * A missing, unsuitable, or expired future id will be a 409 and the transaction will copy taxaCambioBrl.
 */
@RestController
@RequestMapping("/api/v1/cambio")
@SecurityRequirement(name = "bearerAuth")
public class CambioController {
	private final CambioUseCase useCase;
	public CambioController(CambioUseCase useCase) { this.useCase = useCase; }

	@GetMapping("/usd-brl")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "Obtém a observação atual USD/BRL",
			description = "Retorna uma observação persistida. Em futura operação US, o cliente enviará apenas exchangeRateId; B3 não usará esse id.")
	@ApiResponse(responseCode = "200", description = "Observação USD/BRL válida")
	@ApiResponse(responseCode = "401", description = "Autenticação necessária")
	@ApiResponse(responseCode = "502", description = "Nenhum provider forneceu observação válida")
	public CambioResponse usdBrl() { return CambioResponse.from(useCase.obterUsdBrl()); }
}
