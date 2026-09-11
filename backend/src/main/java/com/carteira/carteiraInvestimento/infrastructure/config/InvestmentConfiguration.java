package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort;
import com.carteira.carteiraInvestimento.application.service.InvestmentApplicationService;
import com.carteira.carteiraInvestimento.application.service.InvestmentUseCase;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InvestmentConfiguration {
    @Bean
    InvestmentUseCase investmentUseCase(InvestmentPersistencePort persistence, AuditoriaPort audit, Clock clock) {
        return new InvestmentApplicationService(persistence, audit, clock, ZoneId.of("America/Sao_Paulo"));
    }
}
