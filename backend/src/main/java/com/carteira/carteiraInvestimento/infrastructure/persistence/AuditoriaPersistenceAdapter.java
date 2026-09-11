package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuditoriaPersistenceAdapter implements AuditoriaPort, AuditoriaIsoladaPort {

	private final LogAuditoriaJpaRepository repository;

	public AuditoriaPersistenceAdapter(LogAuditoriaJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public void record(AuditoriaCommand command) {
		repository.save(new LogAuditoriaJpaEntity(UUID.randomUUID(), command.usuarioId(), command.tipo(),
				command.resultado(), command.severidade(), command.endpoint(), command.correlationId(),
				command.instante() == null ? Instant.now() : command.instante()));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordIsoladamente(AuditoriaCommand command) {
		record(command);
	}
}
