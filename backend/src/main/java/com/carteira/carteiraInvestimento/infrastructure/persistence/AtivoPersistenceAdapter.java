package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.application.service.DuplicateTickerException;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class AtivoPersistenceAdapter implements AtivoPort {
	private static final String TICKER_UNIQUE_CONSTRAINT = "uk_acoes_ticker";
	private final AtivoJpaRepository repository;

	public AtivoPersistenceAdapter(AtivoJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Ativo save(Ativo ativo) {
		try {
			return toDomain(repository.saveAndFlush(toEntity(ativo)));
		} catch (DataIntegrityViolationException exception) {
			if (isTickerUniqueViolation(exception)) {
				throw new DuplicateTickerException(exception);
			}
			throw exception;
		}
	}

	@Override
	public Optional<Ativo> findById(UUID id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Ativo> findByTicker(String ticker, AtivoReadScope scope) {
		if (scope == AtivoReadScope.ACTIVE_ONLY) {
			return repository.findByTickerAndAtivoTrue(ticker).map(this::toDomain);
		}
		return repository.findByTicker(ticker)
				.filter(entity -> scope != AtivoReadScope.INACTIVE_ONLY || !entity.isAtivo())
				.map(this::toDomain);
	}

	@Override
	public AtivoPage list(AtivoQuery query) {
		Specification<AtivoJpaEntity> filters = (root, criteriaQuery, builder) -> {
			var predicates = new ArrayList<Predicate>();
			if (query.scope() == AtivoReadScope.ACTIVE_ONLY) {
				predicates.add(builder.isTrue(root.get("ativo")));
			} else if (query.scope() == AtivoReadScope.INACTIVE_ONLY) {
				predicates.add(builder.isFalse(root.get("ativo")));
			}
			if (query.tipo() != null) {
				predicates.add(builder.equal(root.get("tipo"), query.tipo()));
			}
			if (query.q() != null) {
				String pattern = "%" + query.q().toLowerCase(Locale.ROOT) + "%";
				predicates.add(builder.or(
						builder.like(builder.lower(root.get("ticker")), pattern),
						builder.like(builder.lower(root.get("nome")), pattern)));
			}
			return builder.and(predicates.toArray(Predicate[]::new));
		};
		String property = query.sort().name().toLowerCase(Locale.ROOT);
		Sort.Direction direction = query.direction().name().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
		var result = repository.findAll(filters, PageRequest.of(query.page(), query.size(), Sort.by(direction, property)));
		return new AtivoPage(result.getContent().stream().map(this::toDomain).toList(), result.getNumber(),
				result.getSize(), result.getTotalElements(), result.getTotalPages());
	}

	private boolean isTickerUniqueViolation(Throwable failure) {
		for (Throwable current = failure; current != null; current = current.getCause()) {
			if (current instanceof ConstraintViolationException violation
					&& TICKER_UNIQUE_CONSTRAINT.equals(violation.getConstraintName())) {
				return true;
			}
		}
		return false;
	}

	private AtivoJpaEntity toEntity(Ativo ativo) {
		return new AtivoJpaEntity(ativo.id(), ativo.ticker(), ativo.nome(), ativo.tipo(), ativo.mercado(),
				ativo.moeda(), ativo.ativo(), ativo.criadoEm(), ativo.atualizadoEm());
	}

	private Ativo toDomain(AtivoJpaEntity entity) {
		return new Ativo(entity.getId(), entity.getTicker(), entity.getNome(), entity.getTipo(), entity.getMercado(),
				entity.getMoeda(), entity.isAtivo(), entity.getCriadoEm(), entity.getAtualizadoEm());
	}
}
