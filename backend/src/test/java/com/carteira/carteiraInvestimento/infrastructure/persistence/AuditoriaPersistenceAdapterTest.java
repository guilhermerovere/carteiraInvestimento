package com.carteira.carteiraInvestimento.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditoriaPersistenceAdapterTest {

	@Test
	void exposesOnlyTheTypedAuditCommandAtItsInternalBoundary() {
		Method record = AuditoriaPort.class.getDeclaredMethods()[0];

		assertThat(record.getParameterTypes()).containsExactly(AuditoriaCommand.class);
		assertThat(record.getParameterTypes()).doesNotContain(Object.class);
	}

	@Test
	void persistsOnlyFixedAuditMetadata() {
		LogAuditoriaJpaRepository repository = mock(LogAuditoriaJpaRepository.class);
		AuditoriaPersistenceAdapter adapter = new AuditoriaPersistenceAdapter(repository);
		UUID correlationId = UUID.randomUUID();

		adapter.record(new AuditoriaCommand(null, TipoEvento.LOGIN_FALHO, ResultadoAuditoria.FALHA,
				SeveridadeAuditoria.AVISO, "/api/v1/auth/login", correlationId));

		ArgumentCaptor<LogAuditoriaJpaEntity> captured = ArgumentCaptor.forClass(LogAuditoriaJpaEntity.class);
		verify(repository).save(captured.capture());
		assertThat(captured.getValue().getUsuarioId()).isNull();
		assertThat(captured.getValue().getCorrelationId()).isEqualTo(correlationId);
		assertThat(captured.getValue().getEndpoint()).isEqualTo("/api/v1/auth/login");
		assertThat(captured.getValue().getDataHora()).isNotNull();
	}
}
