package com.carteira.carteiraInvestimento.application.service;

public class InvestmentNotFoundException extends RuntimeException {
    public InvestmentNotFoundException() { super("investment resource not found"); }
}
