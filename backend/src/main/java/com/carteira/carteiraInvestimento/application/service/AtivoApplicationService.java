package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TickerCanonicalizer;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class AtivoApplicationService implements AtivoUseCase {
	private final AtivoPort ativos;

	public AtivoApplicationService(AtivoPort ativos) {
		this.ativos = ativos;
	}

	@Override
	@Transactional
	public Ativo criar(String ticker, String nome, TipoAtivo tipo, Mercado mercado) {
		return ativos.save(Ativo.novo(ticker, nome, tipo, mercado));
	}

	@Override
	@Transactional(readOnly = true)
	public AtivoPage listar(AtivoQuery query) {
		return ativos.list(query);
	}

	@Override
	@Transactional(readOnly = true)
	public Ativo consultarPorTicker(String ticker, AtivoReadScope scope) {
		String canonical = TickerCanonicalizer.canonicalizeForLookup(ticker);
		return ativos.findByTicker(canonical, scope).orElseThrow(AtivoNotFoundException::new);
	}

	@Override
	@Transactional
	public Ativo atualizarNome(UUID id, String nome) {
		Ativo atual = ativos.findById(id).orElseThrow(AtivoNotFoundException::new);
		return ativos.save(atual.renomear(nome));
	}

	@Override
	@Transactional
	public Ativo definirLifecycle(UUID id, boolean ativo) {
		Ativo atual = ativos.findById(id).orElseThrow(AtivoNotFoundException::new);
		return ativos.save(atual.definirAtivo(ativo));
	}
}
