package com.carteira.carteiraInvestimento.presentation.account;

import com.carteira.carteiraInvestimento.application.service.AccountSettingsUseCase;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@SecurityRequirement(name = "bearerAuth")
public class AccountSettingsController {
    private final AccountSettingsUseCase settings;

    public AccountSettingsController(AccountSettingsUseCase settings) {
        this.settings = settings;
    }

    @PatchMapping("/profile")
    public AccountProfileResponse updateProfile(@RequestBody UpdateProfileRequest body,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        if (body == null || (body.nome() == null) == (body.email() == null)) {
            throw new IllegalArgumentException("provide exactly one profile field");
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        Usuario result = body.nome() != null
                ? settings.updateName(userId, body.nome(), correlation(request), request.getRequestURI())
                : settings.updateEmail(userId, body.email(), correlation(request), request.getRequestURI());
        return AccountProfileResponse.from(result);
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        settings.changePassword(UUID.fromString(jwt.getSubject()), body.senhaAtual(), body.novaSenha(),
                correlation(request), request.getRequestURI());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/closure")
    public ResponseEntity<Void> closeAccount(@Valid @RequestBody CloseAccountRequest body,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        settings.closeAccount(UUID.fromString(jwt.getSubject()), body.senhaAtual(), body.confirmacao(),
                correlation(request), request.getRequestURI());
        return ResponseEntity.noContent().build();
    }

    private UUID correlation(HttpServletRequest request) {
        Object value = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        return value instanceof UUID id ? id : UUID.randomUUID();
    }
}
