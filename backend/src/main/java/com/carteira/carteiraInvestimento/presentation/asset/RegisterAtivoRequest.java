package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserAssetRegistrationRequest", description = "ROLE_USER: somente ticker e mercado; metadados sao derivados pelo servidor.")
public record RegisterAtivoRequest(String ticker, Mercado mercado) { }
