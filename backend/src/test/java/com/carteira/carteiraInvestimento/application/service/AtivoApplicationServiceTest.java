package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.application.port.AtivoSort;
import com.carteira.carteiraInvestimento.application.port.SortDirection;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AtivoApplicationServiceTest {
	private AtivoPort port;
	private AtivoApplicationService service;

	@BeforeEach
	void setUp() {
		port = Mockito.mock(AtivoPort.class);
		service = new AtivoApplicationService(port);
	}

	@Test
	void createsCanonicalAsset() {
		when(port.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		Ativo result = service.criar(" petr4 ", " Petrobras ", TipoAtivo.ACAO, Mercado.B3);
		assertThat(result.ticker()).isEqualTo("PETR4");
		assertThat(result.nome()).isEqualTo("Petrobras");
	}

	@Test
	void getsTickerCanonicallyWithinProvidedScope() {
		Ativo ativo = Ativo.novo("AAPL", "Apple", TipoAtivo.ACAO, Mercado.US);
		when(port.findByTicker("AAPL", AtivoReadScope.ACTIVE_ONLY)).thenReturn(Optional.of(ativo));
		assertThat(service.consultarPorTicker(" aapl ", AtivoReadScope.ACTIVE_ONLY)).isEqualTo(ativo);
		assertThatThrownBy(() -> service.consultarPorTicker("BAD!", AtivoReadScope.ALL))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void listsUsingControlledQueryAndNormalizesQ() {
		AtivoQuery query = new AtivoQuery("  pet  ", null, AtivoReadScope.ALL, 0, 20, AtivoSort.TICKER,
				SortDirection.ASC);
		when(port.list(query)).thenReturn(new AtivoPage(List.of(), 0, 20, 0, 0));
		service.listar(query);
		ArgumentCaptor<AtivoQuery> capture = ArgumentCaptor.forClass(AtivoQuery.class);
		verify(port).list(capture.capture());
		assertThat(capture.getValue().q()).isEqualTo("pet");
	}

	@Test
	void updatesOnlyNameAndLifecycle() {
		UUID id = UUID.randomUUID();
		Ativo original = Ativo.novo("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3);
		original = new Ativo(id, original.ticker(), original.nome(), original.tipo(), original.mercado(),
				original.moeda(), original.ativo(), original.criadoEm(), original.atualizadoEm());
		when(port.findById(id)).thenReturn(Optional.of(original));
		when(port.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		assertThat(service.atualizarNome(id, "Petrobras PN").nome()).isEqualTo("Petrobras PN");
		assertThat(service.definirLifecycle(id, false).ativo()).isFalse();
	}

	@Test
	void reportsNotFoundAndPropagatesDuplicateConflict() {
		UUID id = UUID.randomUUID();
		when(port.findById(id)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.atualizarNome(id, "Nome")).isInstanceOf(AtivoNotFoundException.class);
		when(port.save(any())).thenThrow(new DuplicateTickerException());
		assertThatThrownBy(() -> service.criar("AAPL", "Apple", TipoAtivo.ACAO, Mercado.US))
				.isInstanceOf(DuplicateTickerException.class);
	}
}
