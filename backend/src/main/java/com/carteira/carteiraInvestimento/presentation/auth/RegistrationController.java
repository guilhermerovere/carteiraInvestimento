package com.carteira.carteiraInvestimento.presentation.auth;

import com.carteira.carteiraInvestimento.application.service.RegistrationService;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

	private final RegistrationService registration;

	public RegistrationController(RegistrationService registration) {
		this.registration = registration;
	}

	@PostMapping("/register")
	public ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterRequest request,
			HttpServletRequest servletRequest) {
		Usuario usuario = registration.register(request.nome(), request.email(), request.senha(),
				CorrelationIdFilter.correlationId(servletRequest));
		return ResponseEntity.status(201).body(RegisteredUserResponse.from(usuario));
	}
}
