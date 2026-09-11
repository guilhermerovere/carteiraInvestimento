package com.carteira.carteiraInvestimento.domain.investment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Posicao(UUID id, UUID carteiraId, UUID ativoId, BigDecimal quantidade,
        BigDecimal precoMedioBrl, BigDecimal totalInvestidoBrl,
        BigDecimal lucroRealizadoAcumuladoBrl, Instant ultimaAtualizacao) {

    public Posicao {
        Objects.requireNonNull(id); Objects.requireNonNull(carteiraId); Objects.requireNonNull(ativoId);
        Objects.requireNonNull(quantidade); Objects.requireNonNull(precoMedioBrl);
        Objects.requireNonNull(totalInvestidoBrl); Objects.requireNonNull(lucroRealizadoAcumuladoBrl);
        Objects.requireNonNull(ultimaAtualizacao);
        boolean zero = quantidade.signum() == 0 && precoMedioBrl.signum() == 0 && totalInvestidoBrl.signum() == 0;
        boolean open = quantidade.signum() > 0 && precoMedioBrl.signum() > 0 && totalInvestidoBrl.signum() > 0;
        if (!zero && !open) throw new FinancialStateException("invalid position state");
    }

    public static Posicao vazia(UUID carteiraId, UUID ativoId, Instant instant) {
        return new Posicao(UUID.randomUUID(), carteiraId, ativoId, InvestmentNumbers.ZERO_8,
                InvestmentNumbers.ZERO_8, InvestmentNumbers.ZERO_2, InvestmentNumbers.ZERO_2, instant);
    }

    public Posicao comprar(BigDecimal bought, BigDecimal cost, Instant instant) {
        BigDecimal newQuantity = InvestmentNumbers.derivedQuantity(quantidade.add(bought), "position quantity");
        BigDecimal newTotal = InvestmentNumbers.derivedMoney(totalInvestidoBrl.add(cost), "invested total");
        BigDecimal average = InvestmentNumbers.derivedAverage(newTotal, newQuantity);
        return new Posicao(id, carteiraId, ativoId, newQuantity, average, newTotal,
                lucroRealizadoAcumuladoBrl, instant);
    }

    public Sale vender(BigDecimal sold, BigDecimal netProceeds, Instant instant) {
        if (quantidade.signum() == 0) throw new FinancialStateException("position does not exist");
        if (sold.compareTo(quantidade) > 0) throw new FinancialStateException("oversell");
        boolean totalSale = sold.compareTo(quantidade) == 0;
        BigDecimal baseCost = totalSale ? totalInvestidoBrl
                : InvestmentNumbers.derivedMoney(sold.multiply(precoMedioBrl), "base cost");
        BigDecimal realized = InvestmentNumbers.derivedMoney(netProceeds.subtract(baseCost), "realized result");
        BigDecimal accumulated = InvestmentNumbers.derivedMoney(
                lucroRealizadoAcumuladoBrl.add(realized), "accumulated realized profit");
        if (totalSale) {
            return new Sale(new Posicao(id, carteiraId, ativoId, InvestmentNumbers.ZERO_8,
                    InvestmentNumbers.ZERO_8, InvestmentNumbers.ZERO_2, accumulated, instant), realized);
        }
        BigDecimal newQuantity = InvestmentNumbers.derivedQuantity(quantidade.subtract(sold), "position quantity");
        BigDecimal newTotal = InvestmentNumbers.derivedMoney(totalInvestidoBrl.subtract(baseCost), "invested total");
        if (newQuantity.signum() <= 0 || newTotal.signum() <= 0) {
            throw new FinancialStateException("invalid open position after sale");
        }
        return new Sale(new Posicao(id, carteiraId, ativoId, newQuantity, precoMedioBrl,
                newTotal, accumulated, instant), realized);
    }

    public record Sale(Posicao position, BigDecimal realizedResult) { }
}
