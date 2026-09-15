package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.PortfolioEvolutionPort;
import com.carteira.carteiraInvestimento.application.service.PortfolioEvolutionApplicationService;
import com.carteira.carteiraInvestimento.application.service.PortfolioEvolutionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PortfolioEvolutionConfiguration {
    @Bean
    PortfolioEvolutionUseCase portfolioEvolutionUseCase(PortfolioEvolutionPort persistence) {
        return new PortfolioEvolutionApplicationService(persistence);
    }
}
