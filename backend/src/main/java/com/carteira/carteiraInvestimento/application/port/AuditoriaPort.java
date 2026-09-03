package com.carteira.carteiraInvestimento.application.port;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
public interface AuditoriaPort { void record(AuditoriaCommand event); }
