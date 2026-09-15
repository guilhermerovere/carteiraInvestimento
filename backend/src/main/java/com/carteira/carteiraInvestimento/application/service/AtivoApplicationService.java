package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TickerCanonicalizer;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.util.UUID;
import java.util.List;
import com.carteira.carteiraInvestimento.application.port.AssetMetadataProviderPort;
import com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort;
import org.springframework.transaction.annotation.Transactional;

public class AtivoApplicationService implements AtivoUseCase {
	private final AtivoPort ativos;
	private final List<AssetMetadataProviderPort> metadataProviders;
	private final List<AssetDiscoveryProviderPort> discoveryProviders;

	public AtivoApplicationService(AtivoPort ativos) {
		this(ativos, List.of(), List.of());
	}

	public AtivoApplicationService(AtivoPort ativos, List<AssetMetadataProviderPort> metadataProviders) {
		this(ativos, metadataProviders, List.of());
	}

	public AtivoApplicationService(AtivoPort ativos, List<AssetMetadataProviderPort> metadataProviders,
			List<AssetDiscoveryProviderPort> discoveryProviders) {
		this.ativos = ativos;
		this.metadataProviders = List.copyOf(metadataProviders);
		this.discoveryProviders = List.copyOf(discoveryProviders);
	}

	@Override
	@Transactional
	public Ativo criar(String ticker, String nome, TipoAtivo tipo, Mercado mercado) {
		return ativos.save(Ativo.novo(ticker, nome, tipo, mercado));
	}

	@Override
	public AtivoRegistrationResult registrar(String ticker, Mercado mercado) {
		if (mercado == null) throw new IllegalArgumentException("market is required");
		String requested = TickerCanonicalizer.canonicalize(ticker);
		if (!mercado.aceitaTicker(requested)) throw new IllegalArgumentException("invalid ticker for market");
		if (ativos.findByTicker(requested, AtivoReadScope.ALL).isPresent()) throw new DuplicateTickerException();
		AssetMetadataProviderPort provider = metadataProviders.stream().filter(p -> p.market() == mercado)
				.findFirst().orElseThrow(() -> new AssetValidationUnavailableException("Asset provider is unavailable"));
		var metadata = provider.validate(requested);
		String canonical = TickerCanonicalizer.canonicalize(metadata.ticker());
		if (metadata.market() != mercado || !mercado.aceitaTicker(canonical)) {
			throw new IllegalArgumentException("provider market does not match request");
		}
		boolean renamed = !requested.equals(canonical);
		var existing = ativos.findByTicker(canonical, AtivoReadScope.ALL);
		if (existing.isPresent()) {
			if (renamed && existing.get().ativo()) return new AtivoRegistrationResult(existing.get(), requested, false);
			throw new DuplicateTickerException();
		}
		String name = metadata.name() == null || metadata.name().isBlank() ? canonical : metadata.name();
		Ativo candidate = Ativo.novo(canonical, name, metadata.type(), mercado,
				metadata.logoProvider(), metadata.logoReference());
		try {
			return new AtivoRegistrationResult(ativos.save(candidate), requested, true);
		} catch (DuplicateTickerException duplicate) {
			if (renamed) {
				var winner = ativos.findByTicker(canonical, AtivoReadScope.ALL);
				if (winner.isPresent() && winner.get().ativo()) return new AtivoRegistrationResult(winner.get(), requested, false);
			}
			throw duplicate;
		}
	}

	@Override
	public List<String> descobrir(String busca, Mercado mercado) {
		if (mercado == null || busca == null || busca.isBlank()) throw new IllegalArgumentException("market and search are required");
		return discoveryProviders.stream().filter(provider -> provider.market() == mercado).findFirst()
				.orElseThrow(() -> new AssetValidationUnavailableException("Asset discovery provider is unavailable"))
				.discover(busca.trim());
	}

	@Override
	@Transactional(readOnly = true)
	public AtivoPage listar(AtivoQuery query) {
		return ativos.list(query);
	}

	@Override
	@Transactional(readOnly = true)
	public Ativo consultarPorTicker(String ticker, AtivoReadScope scope) {
		String canonical = TickerCanonicalizer.canonicalizeForLookup(ticker);
		return ativos.findByTicker(canonical, scope).orElseThrow(AtivoNotFoundException::new);
	}

	@Override
	@Transactional
	public Ativo atualizarNome(UUID id, String nome) {
		Ativo atual = ativos.findById(id).orElseThrow(AtivoNotFoundException::new);
		return ativos.save(atual.renomear(nome));
	}

	@Override
	@Transactional
	public Ativo definirLifecycle(UUID id, boolean ativo) {
		Ativo atual = ativos.findById(id).orElseThrow(AtivoNotFoundException::new);
		return ativos.save(atual.definirAtivo(ativo));
	}
}
