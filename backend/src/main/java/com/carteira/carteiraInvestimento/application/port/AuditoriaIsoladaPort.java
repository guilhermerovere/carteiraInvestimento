package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;

/**
 * Persists audit events that must survive the rollback of the originating operation.
 */
public interface AuditoriaIsoladaPort {
	void recordIsoladamente(AuditoriaCommand event);
}
