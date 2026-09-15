package com.carteira.carteiraInvestimento.domain.investment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class InvestmentNumbers {
    public static final BigDecimal MAX_18_8 = new BigDecimal("9999999999.99999999");
    public static final BigDecimal MAX_18_2 = new BigDecimal("9999999999999999.99");
    public static final BigDecimal MAX_36_16 = new BigDecimal("99999999999999999999.9999999999999999");
    public static final BigDecimal ZERO_8 = new BigDecimal("0.00000000");
    public static final BigDecimal ZERO_2 = new BigDecimal("0.00");
    public static final BigDecimal ONE_8 = new BigDecimal("1.00000000");

    private InvestmentNumbers() { }

    public static BigDecimal positiveInput(BigDecimal value, String field) {
        BigDecimal normalized = input(value, field);
        if (normalized.signum() <= 0) throw new IllegalArgumentException("invalid " + field);
        return normalized;
    }

    public static BigDecimal positiveIntegerInput(BigDecimal value, String field) {
        BigDecimal normalized = positiveInput(value, field);
        if (normalized.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("invalid integer " + field);
        }
        return normalized;
    }

    public static BigDecimal nonNegativeInput(BigDecimal value, String field) {
        BigDecimal normalized = input(value, field);
        if (normalized.signum() < 0) throw new IllegalArgumentException("invalid " + field);
        return normalized;
    }

    private static BigDecimal input(BigDecimal value, String field) {
        if (value == null) throw new IllegalArgumentException("missing " + field);
        final BigDecimal normalized;
        try {
            normalized = value.setScale(8, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("invalid scale for " + field, exception);
        }
        if (normalized.abs().compareTo(MAX_18_8) > 0) throw new IllegalArgumentException("overflow " + field);
        return normalized;
    }

    public static BigDecimal origin(BigDecimal quantity, BigDecimal unitPrice) {
        BigDecimal result = quantity.multiply(unitPrice);
        if (result.abs().compareTo(MAX_36_16) > 0) throw new FinancialStateException("origin overflow");
        return result.setScale(16, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal derivedMoney(BigDecimal exact, String field) {
        BigDecimal rounded = exact.setScale(2, RoundingMode.HALF_EVEN);
        if (rounded.abs().compareTo(MAX_18_2) > 0) throw new FinancialStateException(field + " overflow");
        return rounded;
    }

    public static BigDecimal derivedQuantity(BigDecimal exact, String field) {
        BigDecimal normalized;
        try { normalized = exact.setScale(8, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException exception) { throw new FinancialStateException(field + " scale overflow"); }
        if (normalized.abs().compareTo(MAX_18_8) > 0) throw new FinancialStateException(field + " overflow");
        return normalized;
    }

    public static BigDecimal derivedAverage(BigDecimal total, BigDecimal quantity) {
        BigDecimal average = total.divide(quantity, 8, RoundingMode.HALF_EVEN);
        if (average.signum() <= 0 || average.compareTo(MAX_18_8) > 0) {
            throw new FinancialStateException("average price overflow");
        }
        return average;
    }

    public static String canonical(BigDecimal value) { return value.stripTrailingZeros().toPlainString(); }
}
