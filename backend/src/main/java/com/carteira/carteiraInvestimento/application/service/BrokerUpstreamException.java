package com.carteira.carteiraInvestimento.application.service;
public class BrokerUpstreamException extends RuntimeException {
    public BrokerUpstreamException(String message) { super(message); }
    public BrokerUpstreamException(String message, Throwable cause) { super(message, cause); }
}
