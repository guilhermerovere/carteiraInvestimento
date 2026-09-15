package com.carteira.carteiraInvestimento.application.port;

import java.util.Optional;

public interface BrokerBrandingPort {
	Optional<String> resolve(String legalName, String tradeName);
}
