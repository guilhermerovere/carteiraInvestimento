package com.carteira.carteiraInvestimento.presentation.valuation;

import com.carteira.carteiraInvestimento.application.service.PortfolioEvolutionPoint;
import com.carteira.carteiraInvestimento.application.service.PortfolioEvolutionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carteira/graficos/evolucao")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioEvolutionController {
    private final PortfolioEvolutionUseCase useCase;
    public PortfolioEvolutionController(PortfolioEvolutionUseCase useCase) { this.useCase = useCase; }

    @GetMapping
    @Operation(summary = "Lê a evolução patrimonial histórica da carteira",
            description = "Retorna coleção cronológica, inclusive vazia, somente de snapshots da carteira autenticada com valuation materializado: valuationInstant, valorPosicoesBrl, lucroNaoRealizadoBrl e patrimonioTotalBrl não nulos. Não recalcula, consulta provider, preenche datas ou cria pontos.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Série cronológica ou coleção vazia"),
            @ApiResponse(responseCode = "401"), @ApiResponse(responseCode = "403")})
    public List<EvolutionPointResponse> evolution(@AuthenticationPrincipal Jwt jwt) {
        return useCase.read(UUID.fromString(jwt.getSubject())).stream().map(EvolutionPointResponse::from).toList();
    }

    @Schema(description = "Ponto de evolução com valuation materializado")
    public record EvolutionPointResponse(@NotNull LocalDate dataReferencia, @NotNull BigDecimal totalInvestidoBrl,
            @NotNull BigDecimal resultadoNaoRealizadoBrl, @NotNull BigDecimal valorPosicoesBrl,
            @NotNull BigDecimal patrimonioTotalBrl) {
        static EvolutionPointResponse from(PortfolioEvolutionPoint point) {
            return new EvolutionPointResponse(point.dataReferencia(), point.totalInvestidoBrl(),
                    point.resultadoNaoRealizadoBrl(), point.valorPosicoesBrl(), point.patrimonioTotalBrl());
        }
    }
}
