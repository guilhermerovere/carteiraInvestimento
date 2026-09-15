package com.carteira.carteiraInvestimento.application.service;

public final class AccountClosureConflictException extends RuntimeException {
    public enum Reason { CASH_NOT_ZERO, OPEN_POSITIONS }
    private final Reason reason;

    public AccountClosureConflictException(Reason reason) {
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
