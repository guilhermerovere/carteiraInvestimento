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
import com.carteira.carteiraInvestimento.application.port.AssetMetadataProviderPort;
import com.carteira.carteiraInvestimento.application.port.SortDirection;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
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

	@Test
	void userRegistrationDerivesFinancialMetadataAndBranding() {
		AssetMetadataProviderPort provider = Mockito.mock(AssetMetadataProviderPort.class);
		when(provider.market()).thenReturn(Mercado.B3);
		when(provider.validate("PETR4")).thenReturn(new AssetMetadataProviderPort.AssetMetadata(
				"PETR4", "PETR4", "Petróleo Brasileiro S.A.", TipoAtivo.ACAO, Mercado.B3,
				LogoProvider.BRAPI, "https://icons.brapi.dev/icons/PETR4.svg"));
		when(port.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		AtivoRegistrationResult result = new AtivoApplicationService(port, List.of(provider)).registrar(" petr4 ", Mercado.B3);
		assertThat(result.created()).isTrue();
		assertThat(result.ativo().ticker()).isEqualTo("PETR4");
		assertThat(result.ativo().nome()).isEqualTo("Petróleo Brasileiro S.A.");
		assertThat(result.ativo().logoProvider()).isEqualTo(LogoProvider.BRAPI);
	}

	@Test
	void renamedTickerReusesOnlyCurrentCanonicalAsset() {
		AssetMetadataProviderPort provider = Mockito.mock(AssetMetadataProviderPort.class);
		when(provider.market()).thenReturn(Mercado.B3);
		when(provider.validate("VVAR3")).thenReturn(new AssetMetadataProviderPort.AssetMetadata(
				"VVAR3", "BHIA3", "Grupo Casas Bahia", TipoAtivo.ACAO, Mercado.B3, null, null));
		Ativo canonical = Ativo.novo("BHIA3", "Grupo Casas Bahia", TipoAtivo.ACAO, Mercado.B3);
		when(port.findByTicker("BHIA3", AtivoReadScope.ALL)).thenReturn(Optional.of(canonical));
		AtivoRegistrationResult result = new AtivoApplicationService(port, List.of(provider)).registrar("VVAR3", Mercado.B3);
		assertThat(result.created()).isFalse();
		assertThat(result.requestedTicker()).isEqualTo("VVAR3");
		assertThat(result.ativo()).isSameAs(canonical);
		verify(port, Mockito.never()).save(any());
	}

	@Test
	void invalidMarketAndInactiveDuplicateNeverInvokeProvider() {
		AssetMetadataProviderPort provider = Mockito.mock(AssetMetadataProviderPort.class);
		when(provider.market()).thenReturn(Mercado.B3);
		assertThatThrownBy(() -> new AtivoApplicationService(port, List.of(provider)).registrar("AAPL", Mercado.B3))
				.isInstanceOf(IllegalArgumentException.class);
		Ativo inactive = Ativo.novo("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3).definirAtivo(false);
		when(port.findByTicker("PETR4", AtivoReadScope.ALL)).thenReturn(Optional.of(inactive));
		assertThatThrownBy(() -> new AtivoApplicationService(port, List.of(provider)).registrar("PETR4", Mercado.B3))
				.isInstanceOf(DuplicateTickerException.class);
		verify(provider, Mockito.never()).validate(any());
	}
}
