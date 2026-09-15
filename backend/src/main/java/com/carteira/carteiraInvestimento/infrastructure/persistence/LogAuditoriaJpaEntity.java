package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "logs_auditoria")
class LogAuditoriaJpaEntity {

	@Id
	private UUID id;

	@Column(name = "usuario_id")
	private UUID usuarioId;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_evento", nullable = false)
	private TipoEvento tipoEvento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResultadoAuditoria resultado;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SeveridadeAuditoria severidade;

	@Column
	private String endpoint;

	@Column(name = "correlation_id", nullable = false)
	private UUID correlationId;

	@Column(name = "data_hora", nullable = false)
	private Instant dataHora;

	protected LogAuditoriaJpaEntity() {
	}

	LogAuditoriaJpaEntity(UUID id, UUID usuarioId, TipoEvento tipoEvento, ResultadoAuditoria resultado,
			SeveridadeAuditoria severidade, String endpoint, UUID correlationId, Instant dataHora) {
		this.id = id;
		this.usuarioId = usuarioId;
		this.tipoEvento = tipoEvento;
		this.resultado = resultado;
		this.severidade = severidade;
		this.endpoint = endpoint;
		this.correlationId = correlationId;
		this.dataHora = dataHora;
	}

	UUID getId() {
		return id;
	}

	UUID getUsuarioId() {
		return usuarioId;
	}

	TipoEvento getTipoEvento() {
		return tipoEvento;
	}

	ResultadoAuditoria getResultado() {
		return resultado;
	}

	SeveridadeAuditoria getSeveridade() {
		return severidade;
	}

	String getEndpoint() {
		return endpoint;
	}

	UUID getCorrelationId() {
		return correlationId;
	}

	Instant getDataHora() {
		return dataHora;
	}
}
