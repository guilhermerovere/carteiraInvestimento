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
import java.util.Map;
import java.util.UUID;
import java.util.List;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "Cadastra ativo no catalogo canonico",
			description = "ROLE_ADMIN usa ticker/nome/tipo/mercado. ROLE_USER usa somente ticker/mercado e recebe metadados derivados pelo servidor.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(oneOf = {CreateAtivoRequest.class, RegisterAtivoRequest.class}))))
	@ApiResponses({@ApiResponse(responseCode = "201", description = "Ativo canonico criado"),
		@ApiResponse(responseCode = "200", description = "Ticker renomeado reutilizou o ativo canonico atual"),
		@ApiResponse(responseCode = "400", description = "Shape, ticker, mercado ou tipo financeiro invalido"),
		@ApiResponse(responseCode = "409", description = "Ticker canonico duplicado ou inativo"),
		@ApiResponse(responseCode = "502", description = "Validacao financeira indisponivel")})
	public ResponseEntity<AtivoResponse> create(@RequestBody Map<String, Object> body, Authentication authentication) {
		if (body == null) throw new IllegalArgumentException("invalid asset body");
		if (isAdmin(authentication)) {
			if (body.keySet().equals(Set.of("ticker", "mercado"))) {
				var result = useCase.registrar(text(body, "ticker"),
						enumValue(body, "mercado", com.carteira.carteiraInvestimento.domain.asset.Mercado.class));
				return ResponseEntity.status(result.created() ? 201 : 200).body(AtivoResponse.from(result.ativo()));
			}
			ensureFields(body, Set.of("ticker", "nome", "tipo", "mercado"));
			var ativo = useCase.criar(text(body, "ticker"), text(body, "nome"),
					enumValue(body, "tipo", TipoAtivo.class), enumValue(body, "mercado", com.carteira.carteiraInvestimento.domain.asset.Mercado.class));
			return ResponseEntity.status(201).body(AtivoResponse.from(ativo));
		}
		ensureFields(body, Set.of("ticker", "mercado"));
		var result = useCase.registrar(text(body, "ticker"),
				enumValue(body, "mercado", com.carteira.carteiraInvestimento.domain.asset.Mercado.class));
		return ResponseEntity.status(result.created() ? 201 : 200).body(AtivoResponse.from(result.ativo()));
	}

	@GetMapping("/discovery")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public List<AssetDiscoveryResponse> discover(@RequestParam String q,
			@RequestParam(defaultValue = "B3") com.carteira.carteiraInvestimento.domain.asset.Mercado mercado,
			HttpServletRequest request) {
		if (!Set.of("q", "mercado").containsAll(request.getParameterMap().keySet())) {
			throw new IllegalArgumentException("unknown query parameter");
		}
		return useCase.descobrir(q, mercado).stream().map(ticker -> new AssetDiscoveryResponse(ticker, mercado)).toList();
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

	private static void ensureFields(Map<String, Object> body, Set<String> expected) {
		if (!body.keySet().equals(expected)) throw new IllegalArgumentException("invalid role-specific asset body");
	}

	private static String text(Map<String, Object> body, String name) {
		Object value = body.get(name);
		if (!(value instanceof String text) || text.isBlank()) throw new IllegalArgumentException("invalid " + name);
		return text;
	}

	private static <T extends Enum<T>> T enumValue(Map<String, Object> body, String name, Class<T> type) {
		try { return Enum.valueOf(type, text(body, name)); }
		catch (RuntimeException invalid) { throw new IllegalArgumentException("invalid " + name); }
	}
}
