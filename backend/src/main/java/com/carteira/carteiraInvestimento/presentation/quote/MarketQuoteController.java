package com.carteira.carteiraInvestimento.presentation.quote;

import com.carteira.carteiraInvestimento.application.service.MarketQuoteUseCase;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/acoes/{id}")
public class MarketQuoteController {
	private final MarketQuoteUseCase useCase;
	public MarketQuoteController(MarketQuoteUseCase useCase) { this.useCase = useCase; }

	@GetMapping("/cotacao")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public CotacaoResponse current(@PathVariable UUID id) {
		return CotacaoResponse.from(useCase.cotacaoAtual(id));
	}

	@GetMapping("/historico")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public HistoricoCotacaoResponse history(@PathVariable UUID id,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Authentication authentication) {
		return HistoricoCotacaoResponse.from(useCase.historico(id, page, size, isAdmin(authentication)));
	}

	@PutMapping("/atualizar-cotacao")
	@PreAuthorize("hasRole('ADMIN')")
	public CotacaoResponse refresh(@PathVariable UUID id) {
		return CotacaoResponse.from(useCase.atualizarCotacao(id));
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}
