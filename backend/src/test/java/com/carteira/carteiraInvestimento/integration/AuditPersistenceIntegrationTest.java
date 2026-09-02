package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditPersistenceIntegrationTest extends PostgreSqlContainerSupport {

	@Autowired
	private AuditoriaPort auditoria;

	@Autowired
	private javax.sql.DataSource dataSource;

	@Test
	void persistsTypedAnonymousAuditMetadataWithoutHttpPayload() throws Exception {
		UUID correlationId = UUID.randomUUID();
		auditoria.record(new AuditoriaCommand(null, TipoEvento.LOGIN_FALHO, ResultadoAuditoria.FALHA,
				SeveridadeAuditoria.AVISO, null, correlationId));

		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT usuario_id, endpoint, correlation_id, tipo_evento FROM logs_auditoria WHERE correlation_id = ?")) {
			statement.setObject(1, correlationId);
			try (var resultSet = statement.executeQuery()) {
				assertThat(resultSet.next()).isTrue();
				assertThat(resultSet.getObject("usuario_id")).isNull();
				assertThat(resultSet.getString("endpoint")).isNull();
				assertThat(resultSet.getObject("correlation_id", UUID.class)).isEqualTo(correlationId);
				assertThat(resultSet.getString("tipo_evento")).isEqualTo("LOGIN_FALHO");
			}
		}
	}
}
