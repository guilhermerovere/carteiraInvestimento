package com.carteira.carteiraInvestimento.presentation.auth;

import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer.IssuedAccessToken;
import com.carteira.carteiraInvestimento.application.service.LoginService;
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
public class LoginController {
	private final LoginService login;

	public LoginController(LoginService login) {
		this.login = login;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest) {
		IssuedAccessToken token = login.login(request.email(), request.senha(),
				CorrelationIdFilter.correlationId(servletRequest));
		return ResponseEntity.ok(LoginResponse.from(token));
	}
}
