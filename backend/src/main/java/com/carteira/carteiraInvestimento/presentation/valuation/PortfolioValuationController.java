package com.carteira.carteiraInvestimento.presentation.valuation;

import com.carteira.carteiraInvestimento.application.service.PortfolioValuationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carteira/resumo")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Portfolio valuation", description = "Valuation B3/US em BRL sem parcial e sem identificadores do browser")
public class PortfolioValuationController {
    private final PortfolioValuationUseCase useCase;
    public PortfolioValuationController(PortfolioValuationUseCase useCase) { this.useCase = useCase; }

    @GetMapping
    @Operation(summary = "Calcula o resumo atual sem persistir",
            description = "Usa fotografia REPEATABLE_READ, quotes atuais, uma única FX quando há US e precisão HALF_EVEN. Carteira vazia não chama providers.")
    @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "400"),
        @ApiResponse(responseCode = "401"), @ApiResponse(responseCode = "403"),
        @ApiResponse(responseCode = "409", description = "Overflow ou segunda perda de versão"),
        @ApiResponse(responseCode = "502", description = "Quote ou FX necessário indisponível")})
    public PortfolioValuationResponse summarize(@AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) byte[] body, HttpServletRequest request) {
        validateStrict(body, request);
        return PortfolioValuationResponse.from(useCase.summarize(userId(jwt)));
    }

    @PostMapping("/atualizar")
    @Operation(summary = "Atualiza e materializa o resumo diário",
            description = "POST sem corpo. Só substitui valuation diária quando valuationInstant é estritamente maior após normalização em microssegundos.")
    @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "400"),
        @ApiResponse(responseCode = "401"), @ApiResponse(responseCode = "403"),
        @ApiResponse(responseCode = "409", description = "Overflow ou segunda perda de versão"),
        @ApiResponse(responseCode = "502", description = "Quote ou FX necessário indisponível")})
    public PortfolioValuationResponse refresh(@AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) byte[] body, HttpServletRequest request) {
        validateStrict(body, request);
        return PortfolioValuationResponse.from(useCase.refresh(userId(jwt)));
    }

    private static void validateStrict(byte[] body, HttpServletRequest request) {
        if (body != null && body.length > 0) throw new IllegalArgumentException("request body is not allowed");
        if (!request.getParameterMap().isEmpty()) throw new IllegalArgumentException("query parameters are not allowed");
    }
    private static UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
