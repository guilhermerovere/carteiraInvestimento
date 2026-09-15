package com.carteira.carteiraInvestimento.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.service.DuplicateTickerException;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

class AtivoPersistenceAdapterTest {
	@Test
	void translatesOnlyTheNamedTickerUniqueConstraint() {
		AtivoJpaRepository repository = Mockito.mock(AtivoJpaRepository.class);
		AtivoPersistenceAdapter adapter = new AtivoPersistenceAdapter(repository);
		when(repository.saveAndFlush(any())).thenThrow(violation("uk_acoes_ticker"));
		assertThatThrownBy(() -> adapter.save(asset())).isInstanceOf(DuplicateTickerException.class);

		AtivoJpaRepository otherRepository = Mockito.mock(AtivoJpaRepository.class);
		AtivoPersistenceAdapter otherAdapter = new AtivoPersistenceAdapter(otherRepository);
		when(otherRepository.saveAndFlush(any())).thenThrow(violation("ck_acoes_nome"));
		assertThatThrownBy(() -> otherAdapter.save(asset())).isInstanceOf(DataIntegrityViolationException.class)
				.isNotInstanceOf(DuplicateTickerException.class);
	}

	private DataIntegrityViolationException violation(String constraint) {
		return new DataIntegrityViolationException("sanitized wrapper",
				new ConstraintViolationException("database failure", new SQLException(), constraint));
	}

	private Ativo asset() {
		return Ativo.novo("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3);
	}
}
