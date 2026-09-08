package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.application.port.AtivoSort;
import com.carteira.carteiraInvestimento.application.port.SortDirection;
import com.carteira.carteiraInvestimento.application.service.AtivoUseCase;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/acoes")
public class AtivoController {
	private static final Set<String> LIST_PARAMS = Set.of("page", "size", "sort", "direction", "q", "tipo", "ativo");
	private final AtivoUseCase useCase;

	public AtivoController(AtivoUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public AtivoListResponse list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "ticker") String sort,
			@RequestParam(defaultValue = "asc") String direction,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) TipoAtivo tipo,
			@RequestParam(required = false) Boolean ativo,
			Authentication authentication,
			HttpServletRequest request) {
		ensureOnlyListParameters(request);
		AtivoReadScope scope = resolveScope(isAdmin(authentication), ativo);
		AtivoQuery query = new AtivoQuery(q, tipo, scope, page, size, parseSort(sort), parseDirection(direction));
		return AtivoListResponse.from(useCase.listar(query));
	}

	@GetMapping("/ticker/{ticker}")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public AtivoResponse getByTicker(@PathVariable String ticker, Authentication authentication) {
		AtivoReadScope scope = isAdmin(authentication) ? AtivoReadScope.ALL : AtivoReadScope.ACTIVE_ONLY;
		return AtivoResponse.from(useCase.consultarPorTicker(ticker, scope));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AtivoResponse> create(@Valid @RequestBody CreateAtivoRequest request) {
		var ativo = useCase.criar(request.ticker(), request.nome(), request.tipo(), request.mercado());
		return ResponseEntity.status(201).body(AtivoResponse.from(ativo));
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AtivoResponse updateName(@PathVariable UUID id, @Valid @RequestBody UpdateAtivoNameRequest request) {
		return AtivoResponse.from(useCase.atualizarNome(id, request.nome()));
	}

	@PatchMapping("/{id}/ativo")
	@PreAuthorize("hasRole('ADMIN')")
	public AtivoResponse updateLifecycle(@PathVariable UUID id,
			@Valid @RequestBody UpdateAtivoLifecycleRequest request) {
		return AtivoResponse.from(useCase.definirLifecycle(id, request.ativo()));
	}

	private void ensureOnlyListParameters(HttpServletRequest request) {
		if (!LIST_PARAMS.containsAll(request.getParameterMap().keySet())) {
			throw new IllegalArgumentException("unknown query parameter");
		}
	}

	private AtivoReadScope resolveScope(boolean admin, Boolean ativo) {
		if (!admin || Boolean.TRUE.equals(ativo)) {
			return AtivoReadScope.ACTIVE_ONLY;
		}
		return Boolean.FALSE.equals(ativo) ? AtivoReadScope.INACTIVE_ONLY : AtivoReadScope.ALL;
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}

	private AtivoSort parseSort(String value) {
		return switch (value) {
			case "ticker" -> AtivoSort.TICKER;
			case "nome" -> AtivoSort.NOME;
			default -> throw new IllegalArgumentException("invalid sort");
		};
	}

	private SortDirection parseDirection(String value) {
		return switch (value) {
			case "asc" -> SortDirection.ASC;
			case "desc" -> SortDirection.DESC;
			default -> throw new IllegalArgumentException("invalid direction");
		};
	}
}
