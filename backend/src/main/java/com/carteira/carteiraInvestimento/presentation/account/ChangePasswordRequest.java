package com.carteira.carteiraInvestimento.presentation.account;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String senhaAtual, @NotBlank String novaSenha) { }
