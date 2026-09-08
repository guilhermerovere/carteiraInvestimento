package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.util.UUID;

public interface AtivoUseCase {
	Ativo criar(String ticker, String nome, TipoAtivo tipo, Mercado mercado);
	AtivoPage listar(AtivoQuery query);
	Ativo consultarPorTicker(String ticker, AtivoReadScope scope);
	Ativo atualizarNome(UUID id, String nome);
	Ativo definirLifecycle(UUID id, boolean ativo);
}
