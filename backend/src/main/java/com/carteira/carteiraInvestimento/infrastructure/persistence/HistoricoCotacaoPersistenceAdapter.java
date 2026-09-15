package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPort;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HistoricoCotacaoPersistenceAdapter implements HistoricoCotacaoPort {
	private final HistoricoCotacaoJpaRepository repository;

	public HistoricoCotacaoPersistenceAdapter(HistoricoCotacaoJpaRepository repository) { this.repository = repository; }

	@Override
	@Transactional
	public Cotacao save(Cotacao cotacao) {
		return toDomain(repository.saveAndFlush(toEntity(cotacao)));
	}

	@Override
	@Transactional(readOnly = true)
	public HistoricoCotacaoPage findByAtivoId(UUID ativoId, int page, int size) {
		Sort sort = Sort.by(Sort.Order.desc("instanteCotacao"), Sort.Order.desc("recebidoEm"), Sort.Order.desc("id"));
		var result = repository.findByAtivoId(ativoId, PageRequest.of(page, size, sort));
		return new HistoricoCotacaoPage(result.getContent().stream().map(this::toDomain).toList(),
				result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private HistoricoCotacaoJpaEntity toEntity(Cotacao quote) {
		return new HistoricoCotacaoJpaEntity(quote.id(), quote.ativoId(), quote.preco(), quote.moeda(),
				quote.instanteCotacao(), quote.provider(), quote.recebidoEm());
	}

	private Cotacao toDomain(HistoricoCotacaoJpaEntity entity) {
		return new Cotacao(entity.getId(), entity.getAtivoId(), entity.getPreco(), entity.getMoeda(),
				entity.getInstanteCotacao(), entity.getProvider(), entity.getRecebidoEm());
	}
}
