package com.carteira.carteiraInvestimento.application.service;

public class PortfolioValuationUpstreamException extends RuntimeException {
    public PortfolioValuationUpstreamException(Throwable cause) { super("valuation provider unavailable", cause); }
}
