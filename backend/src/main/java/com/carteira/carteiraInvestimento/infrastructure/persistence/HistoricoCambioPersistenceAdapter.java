package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HistoricoCambioPersistenceAdapter implements HistoricoCambioPort {
	private final HistoricoCambioJpaRepository repository;
	public HistoricoCambioPersistenceAdapter(HistoricoCambioJpaRepository repository) { this.repository = repository; }

	@Override
	@Transactional
	public ObservacaoCambio save(ObservacaoCambio value) {
		var saved = repository.saveAndFlush(new HistoricoCambioJpaEntity(value.id(), value.moedaOrigem(),
				value.moedaDestino(), value.taxa(), value.provider(), value.instanteCotacao(), value.registradoEm()));
		return new ObservacaoCambio(saved.getId(), saved.getMoedaOrigem(), saved.getMoedaDestino(), saved.getTaxa(),
				saved.getProvider(), saved.getInstanteCotacao(), saved.getRegistradoEm());
	}
}
