package com.carteira.carteiraInvestimento.presentation.wallet;

import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carteira/caixa")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class CashMovementController {
	private static final Set<String> HISTORY_PARAMS = Set.of("page", "size");
	private final CashMovementUseCase useCase;

	public CashMovementController(CashMovementUseCase useCase) { this.useCase = useCase; }

	@PostMapping("/deposito")
	@Operation(summary = "Deposita BRL na carteira principal")
	public ResponseEntity<CashMovementResponse> deposit(@AuthenticationPrincipal Jwt jwt,
			@RequestHeader(name = "Idempotency-Key", required = false) String key,
			@RequestBody CashMovementRequest body, HttpServletRequest request) {
		return execute(jwt, key, body, request, TipoMovimentacaoCaixa.DEPOSITO);
	}

	@PostMapping("/saque")
	@Operation(summary = "Saca BRL da carteira principal")
	public ResponseEntity<CashMovementResponse> withdraw(@AuthenticationPrincipal Jwt jwt,
			@RequestHeader(name = "Idempotency-Key", required = false) String key,
			@RequestBody CashMovementRequest body, HttpServletRequest request) {
		return execute(jwt, key, body, request, TipoMovimentacaoCaixa.SAQUE);
	}

	@GetMapping
	@Operation(summary = "Consulta o saldo de caixa BRL")
	public CashBalanceResponse balance(@AuthenticationPrincipal Jwt jwt) {
		return new CashBalanceResponse(useCase.balance(userId(jwt)));
	}

	@GetMapping("/movimentacoes")
	@Operation(summary = "Consulta o histórico paginado de caixa")
	public CashMovementHistoryResponse history(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			HttpServletRequest request) {
		if (!HISTORY_PARAMS.containsAll(request.getParameterMap().keySet())) {
			throw new IllegalArgumentException("unknown query parameter");
		}
		return CashMovementHistoryResponse.from(useCase.history(userId(jwt), page, size));
	}

	private ResponseEntity<CashMovementResponse> execute(Jwt jwt, String key, CashMovementRequest body,
			HttpServletRequest request, TipoMovimentacaoCaixa type) {
		var result = useCase.execute(userId(jwt), type, body.valor(), body.descricao(), key,
				CorrelationIdFilter.correlationId(request), request.getRequestURI());
		return ResponseEntity.status(201).body(CashMovementResponse.from(result));
	}

	private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
