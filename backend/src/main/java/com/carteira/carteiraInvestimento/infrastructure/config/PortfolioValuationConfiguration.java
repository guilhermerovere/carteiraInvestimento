package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort;
import com.carteira.carteiraInvestimento.application.service.CambioUseCase;
import com.carteira.carteiraInvestimento.application.service.MarketQuoteUseCase;
import com.carteira.carteiraInvestimento.application.service.PortfolioValuationApplicationService;
import com.carteira.carteiraInvestimento.application.service.PortfolioValuationUseCase;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class PortfolioValuationConfiguration {
    @Bean
    PortfolioValuationUseCase portfolioValuationUseCase(PortfolioValuationPort persistence,
            MarketQuoteUseCase quotes, CambioUseCase exchangeRates, PlatformTransactionManager transactionManager,
            Clock clock) {
        TransactionTemplate initialRead = new TransactionTemplate(transactionManager);
        initialRead.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        initialRead.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        initialRead.setReadOnly(true);
        TransactionTemplate finalValidation = new TransactionTemplate(transactionManager);
        finalValidation.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new PortfolioValuationApplicationService(persistence, quotes, exchangeRates, initialRead,
                finalValidation, clock, ZoneId.of("America/Sao_Paulo"));
    }
}
