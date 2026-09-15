package com.carteira.carteiraInvestimento.presentation.account;

import jakarta.validation.constraints.NotBlank;

public record CloseAccountRequest(@NotBlank String senhaAtual, @NotBlank String confirmacao) { }
