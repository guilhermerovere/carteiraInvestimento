package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AuditoriaPersistenceAdapter implements AuditoriaPort {

	private final LogAuditoriaJpaRepository repository;

	public AuditoriaPersistenceAdapter(LogAuditoriaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public void record(AuditoriaCommand command) {
		repository.save(new LogAuditoriaJpaEntity(UUID.randomUUID(), command.usuarioId(), command.tipo(),
				command.resultado(), command.severidade(), command.endpoint(), command.correlationId(), Instant.now()));
	}
}
