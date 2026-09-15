package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import java.util.Optional;
import java.util.UUID;

public interface AtivoPort {
	Ativo save(Ativo ativo);
	Optional<Ativo> findById(UUID id);
	Optional<Ativo> findByTicker(String ticker, AtivoReadScope scope);
	AtivoPage list(AtivoQuery query);
}
