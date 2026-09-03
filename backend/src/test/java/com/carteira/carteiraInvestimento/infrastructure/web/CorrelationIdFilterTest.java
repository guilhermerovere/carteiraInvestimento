package com.carteira.carteiraInvestimento.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpServletRequest;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.mockito.ArgumentCaptor;

class CorrelationIdFilterTest {

	private MockMvc mockMvc;

	private AuditoriaPort auditoria;

	@BeforeEach
	void setUp() {
		auditoria = mock(AuditoriaPort.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new CorrelationProbeController(auditoria))
				.addFilters(new CorrelationIdFilter())
				.build();
	}

	@Test
	void acceptsOnlyACanonicalBoundedUuidFromTheHeader() throws Exception {
		String correlationId = UUID.randomUUID().toString();

		MvcResult result = mockMvc.perform(get("/test/correlation").header(CorrelationIdFilter.HEADER, correlationId))
				.andExpect(status().isOk())
				.andExpect(header().string(CorrelationIdFilter.HEADER, correlationId))
				.andReturn();

		assertThat(result.getResponse().getContentAsString()).isEqualTo(correlationId);
	}

	@Test
	void generatesAUuidWhenTheHeaderIsMissingOrInvalid() throws Exception {
		MvcResult missing = mockMvc.perform(get("/test/correlation"))
				.andExpect(status().isOk())
				.andReturn();
		MvcResult invalid = mockMvc.perform(get("/test/correlation").header(CorrelationIdFilter.HEADER, "not-a-uuid"))
				.andExpect(status().isOk())
				.andReturn();

		assertGeneratedCorrelationId(missing);
		assertGeneratedCorrelationId(invalid);
		assertThat(invalid.getResponse().getHeader(CorrelationIdFilter.HEADER)).isNotEqualTo("not-a-uuid");
	}

	@Test
	void makesTheGeneratedCorrelationIdAvailableToAnAuditedRequest() throws Exception {
		MvcResult result = mockMvc.perform(get("/test/audited-correlation").header(CorrelationIdFilter.HEADER, "invalid"))
				.andExpect(status().isOk())
				.andReturn();

		ArgumentCaptor<AuditoriaCommand> captured = ArgumentCaptor.forClass(AuditoriaCommand.class);
		verify(auditoria).record(captured.capture());
		assertThat(captured.getValue().correlationId().toString())
				.isEqualTo(result.getResponse().getHeader(CorrelationIdFilter.HEADER));
	}

	private void assertGeneratedCorrelationId(MvcResult result) throws Exception {
		String header = result.getResponse().getHeader(CorrelationIdFilter.HEADER);
		assertThat(header).isNotNull();
		assertThat(UUID.fromString(header).toString()).isEqualTo(header);
		assertThat(result.getResponse().getContentAsString()).isEqualTo(header);
	}

	@RestController
	private static class CorrelationProbeController {
		private final AuditoriaPort auditoria;

		CorrelationProbeController(AuditoriaPort auditoria) {
			this.auditoria = auditoria;
		}

		@GetMapping("/test/correlation")
		String correlation(HttpServletRequest request) {
			return CorrelationIdFilter.correlationId(request).toString();
		}

		@GetMapping("/test/audited-correlation")
		void auditedCorrelation(HttpServletRequest request) {
			auditoria.record(new AuditoriaCommand(null, TipoEvento.LOGIN_FALHO, ResultadoAuditoria.FALHA,
					SeveridadeAuditoria.AVISO, "/test/audited-correlation", CorrelationIdFilter.correlationId(request)));
		}
	}
}
