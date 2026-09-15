package com.carteira.carteiraInvestimento.presentation.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.service.AccountSettingsUseCase;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

class AccountSettingsControllerTest {
    private AccountSettingsUseCase useCase;
    private AccountSettingsController controller;
    private UUID userId;
    private Jwt jwt;
    private MockHttpServletRequest request;
    private Usuario user;

    @BeforeEach
    void setUp() {
        useCase = mock(AccountSettingsUseCase.class);
        controller = new AccountSettingsController(useCase);
        userId = UUID.randomUUID();
        jwt = new Jwt("server-only-token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", userId.toString(), "role", "ROLE_USER"));
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/account/profile");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID());
        user = new Usuario(userId, "Ada", "ada@example.test", "hash-never-returned", Role.ROLE_USER,
                true, Instant.now(), Instant.now());
    }

    @Test
    void profileUsesOnlyJwtSubjectAndNeverReturnsHash() {
        when(useCase.updateName(eq(userId), eq("Ada Lovelace"), any(), eq("/api/v1/account/profile")))
                .thenReturn(new Usuario(userId, "Ada Lovelace", user.email(), user.senhaHash(), user.role(), true,
                        user.criadoEm(), Instant.now()));
        AccountProfileResponse response = controller.updateProfile(new UpdateProfileRequest("Ada Lovelace", null), jwt, request);
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.nome()).isEqualTo("Ada Lovelace");
        assertThat(AccountProfileResponse.class.getRecordComponents()).extracting(component -> component.getName())
                .containsExactly("id", "nome", "email", "role", "ativo")
                .doesNotContain("senha", "senhaHash");
    }

    @Test
    void passwordAndClosureUseJwtSubjectAndReturnNoContent() {
        request.setRequestURI("/api/v1/account/password");
        var passwordResponse = controller.changePassword(new ChangePasswordRequest("Atual123!", "NovaSenha123!"), jwt, request);
        assertThat(passwordResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(passwordResponse.getBody()).isNull();
        verify(useCase).changePassword(eq(userId), eq("Atual123!"), eq("NovaSenha123!"), any(), eq("/api/v1/account/password"));

        request.setRequestURI("/api/v1/account/closure");
        var closureResponse = controller.closeAccount(new CloseAccountRequest("Atual123!", "EXCLUIR MINHA CONTA"), jwt, request);
        assertThat(closureResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(closureResponse.getBody()).isNull();
        verify(useCase).closeAccount(eq(userId), eq("Atual123!"), eq("EXCLUIR MINHA CONTA"), any(), eq("/api/v1/account/closure"));
    }
}
