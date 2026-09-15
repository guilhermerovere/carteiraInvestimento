package com.carteira.carteiraInvestimento.presentation.auth;

import com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirement(name = "bearerAuth")
public class CurrentPrincipalController {
	private final CurrentPrincipalService currentPrincipal;

	public CurrentPrincipalController(CurrentPrincipalService currentPrincipal) {
		this.currentPrincipal = currentPrincipal;
	}

	@GetMapping("/me")
	public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(CurrentUserResponse.from(currentPrincipal.current(UUID.fromString(jwt.getSubject()))));
	}
}
