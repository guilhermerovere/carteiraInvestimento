package com.carteira.carteiraInvestimento.presentation.investment;

import com.carteira.carteiraInvestimento.application.service.InvestmentUseCase;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carteira")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name="Investment transactions and positions",description="Cinco operações ROLE_USER; sem valuation ou providers no POST")
public class InvestmentController {
    private static final Set<String> PAGE_PARAMS = Set.of("page", "size");
    private final InvestmentUseCase useCase;
    public InvestmentController(InvestmentUseCase useCase) { this.useCase = useCase; }

    @PostMapping("/transacoes")
    @Operation(summary = "Confirma uma compra ou venda B3/BRL ou US/USD",
            description = "ROLE_USER. Requer Idempotency-Key. Valores têm até 8 casas; US requer FX local USD/BRL válido por 5 minutos. Não executa providers nem valuation.")
    @ApiResponses({@ApiResponse(responseCode="201",description="Criada ou replay determinístico"),
        @ApiResponse(responseCode="400",description="JSON, key, tipo, UUID, escala ou shape B3/US inválido"),
        @ApiResponse(responseCode="401",description="Não autenticado"),@ApiResponse(responseCode="403",description="ROLE_USER obrigatório"),
        @ApiResponse(responseCode="404",description="Ativo ou corretora inexistente"),
        @ApiResponse(responseCode="409",description="Conflito financeiro, lifecycle, FX, idempotência ou overflow derivado"),
        @ApiResponse(responseCode="500",description="Carteira ausente, auditoria ou persistência inesperada")})
    public ResponseEntity<InvestmentOperationResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Parameter(required = true, description = "Chave idempotente de 1 a 128 caracteres")
            @RequestHeader(name = "Idempotency-Key", required = false) String key,
            @RequestBody InvestmentTransactionRequest body, HttpServletRequest request) {
        validateNoParams(request);
        var result = useCase.execute(userId(jwt), body.command(), key,
                CorrelationIdFilter.correlationId(request), request.getRequestURI());
        return ResponseEntity.status(201).body(InvestmentOperationResponse.from(result));
    }

    @GetMapping("/transacoes")
    @Operation(summary = "Lista transações da carteira autenticada",
            description = "ROLE_USER. Ordem fixa dataRegistro DESC, id DESC; page>=0 e size entre 1 e 100.")
    public TransactionPageResponse transactions(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        validateParams(request); return TransactionPageResponse.from(useCase.transactions(userId(jwt), page, size));
    }

    @GetMapping("/transacoes/{id}")
    @Operation(summary = "Obtém transação própria", description = "Recurso alheio retorna 403; UUID inexistente retorna 404.")
    @ApiResponses({@ApiResponse(responseCode="200"),@ApiResponse(responseCode="401"),
        @ApiResponse(responseCode="403",description="Ownership cruzado; gera ACESSO_NEGADO"),@ApiResponse(responseCode="404")})
    public TransactionResponse transaction(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            HttpServletRequest request) {
        validateNoParams(request);
        return TransactionResponse.from(useCase.transaction(userId(jwt), id));
    }

    @GetMapping("/posicoes")
    @Operation(summary = "Lista posições abertas", description = "Não contém valuation; posições zeradas não aparecem na lista.")
    public PositionPageResponse positions(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        validateParams(request); return PositionPageResponse.from(useCase.positions(userId(jwt), page, size));
    }

    @GetMapping("/posicoes/{ativoId}")
    @Operation(summary = "Obtém posição por ativo", description = "Pode retornar posição zerada; não contém valuation.")
    public PositionResponse position(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID ativoId,
            HttpServletRequest request) {
        validateNoParams(request);
        return PositionResponse.from(useCase.position(userId(jwt), ativoId));
    }

    private static void validateParams(HttpServletRequest request) {
        if (!PAGE_PARAMS.containsAll(request.getParameterMap().keySet()))
            throw new IllegalArgumentException("unknown query parameter");
    }
    private static void validateNoParams(HttpServletRequest request) {
        if (!request.getParameterMap().isEmpty()) throw new IllegalArgumentException("unknown query parameter");
    }
    private static UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
