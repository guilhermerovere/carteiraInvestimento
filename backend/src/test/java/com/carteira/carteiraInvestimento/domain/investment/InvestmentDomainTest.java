package com.carteira.carteiraInvestimento.domain.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InvestmentDomainTest {
    private static final UUID WALLET = UUID.randomUUID();
    private static final UUID ASSET = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-09-11T15:30:00.123456Z");

    @Test
    void acceptsEquivalentEightScaleInputsAndRejectsSignificantExcessAndOverflow() {
        for (String value : new String[]{"10", "10.0", "10.00000000", "10.0000000000"})
            assertThat(InvestmentNumbers.positiveInput(new BigDecimal(value), "quantity"))
                    .isEqualByComparingTo("10.00000000");
        assertThatThrownBy(() -> InvestmentNumbers.positiveInput(new BigDecimal("10.123456789"), "quantity"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvestmentNumbers.positiveInput(new BigDecimal("10000000000"), "quantity"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvestmentNumbers.positiveInput(BigDecimal.ZERO, "quantity"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvestmentNumbers.nonNegativeInput(new BigDecimal("-0.01"), "fees"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void firstAndSuccessiveBuysUseInvestedTotalAsTruthAndWeightedAverage() {
        Posicao first = empty().comprar(new BigDecimal("10.00000000"), new BigDecimal("1000.00"), NOW);
        assertThat(first.quantidade()).isEqualByComparingTo("10");
        assertThat(first.totalInvestidoBrl()).isEqualByComparingTo("1000.00");
        assertThat(first.precoMedioBrl()).isEqualByComparingTo("100.00000000");

        Posicao second = first.comprar(new BigDecimal("10.00000000"), new BigDecimal("2001.00"), NOW.plusSeconds(1));
        assertThat(second.totalInvestidoBrl()).isEqualByComparingTo("3001.00");
        assertThat(second.precoMedioBrl()).isEqualByComparingTo("150.05000000");
    }

    @Test
    void halfEvenIsAppliedOnlyToDerivedMoneyAndAverage() {
        assertThat(InvestmentNumbers.derivedMoney(new BigDecimal("10.005"), "cost"))
                .isEqualByComparingTo("10.00");
        assertThat(InvestmentNumbers.derivedMoney(new BigDecimal("10.015"), "cost"))
                .isEqualByComparingTo("10.02");
        Posicao result = empty().comprar(new BigDecimal("3.00000000"), new BigDecimal("100.00"), NOW);
        assertThat(result.precoMedioBrl()).isEqualByComparingTo("33.33333333");
    }

    @Test
    void buyArithmeticCoversB3UsFxFeesFractionsResiduesAndExactWeightedAverage() {
        BigDecimal quantity = new BigDecimal("0.33333333");
        BigDecimal unitPrice = new BigDecimal("10.00000000");
        BigDecimal origin = InvestmentNumbers.origin(quantity, unitPrice);
        assertThat(origin).isEqualByComparingTo("3.3333333000000000");

        BigDecimal b3Cost = InvestmentNumbers.derivedMoney(origin.add(new BigDecimal("0.00500000")), "cost");
        BigDecimal usCostAtFour = InvestmentNumbers.derivedMoney(
                origin.multiply(new BigDecimal("4.00000000")).add(new BigDecimal("0.00500000")), "cost");
        BigDecimal usCostAtFive = InvestmentNumbers.derivedMoney(
                origin.multiply(new BigDecimal("5.00000000")).add(new BigDecimal("0.00500000")), "cost");
        assertThat(b3Cost).isEqualByComparingTo("3.34");
        assertThat(usCostAtFour).isEqualByComparingTo("13.34");
        assertThat(usCostAtFive).isEqualByComparingTo("16.67");

        Posicao position = empty().comprar(quantity, b3Cost, NOW)
                .comprar(quantity, usCostAtFour, NOW.plusSeconds(1))
                .comprar(quantity, usCostAtFive, NOW.plusSeconds(2));
        assertThat(position.quantidade()).isEqualByComparingTo("0.99999999");
        assertThat(position.totalInvestidoBrl()).isEqualByComparingTo("33.35");
        assertThat(position.precoMedioBrl()).isEqualByComparingTo("33.35000033");
        assertThatThrownBy(() -> InvestmentNumbers.derivedMoney(InvestmentNumbers.origin(
                InvestmentNumbers.MAX_18_8, InvestmentNumbers.MAX_18_8), "cost"))
                .isInstanceOf(FinancialStateException.class);
    }

    @Test
    void fractionalPartialSaleKeepsAverageAndAccumulatesProfit() {
        Posicao held = empty().comprar(new BigDecimal("10.00000000"), new BigDecimal("1000.00"), NOW);
        Posicao.Sale first = held.vender(new BigDecimal("4.00000000"), new BigDecimal("470.00"), NOW.plusSeconds(1));
        assertThat(first.realizedResult()).isEqualByComparingTo("70.00");
        assertThat(first.position().quantidade()).isEqualByComparingTo("6");
        assertThat(first.position().totalInvestidoBrl()).isEqualByComparingTo("600.00");
        assertThat(first.position().precoMedioBrl()).isEqualByComparingTo("100.00000000");
        assertThat(first.position().lucroRealizadoAcumuladoBrl()).isEqualByComparingTo("70.00");

        Posicao.Sale loss = first.position().vender(new BigDecimal("1.50000000"), new BigDecimal("100.00"), NOW.plusSeconds(2));
        assertThat(loss.realizedResult()).isEqualByComparingTo("-50.00");
        assertThat(loss.position().lucroRealizadoAcumuladoBrl()).isEqualByComparingTo("20.00");
    }

    @Test
    void totalSaleAbsorbsRoundingResidueAndPersistsZeroPositionForRepurchase() {
        Posicao held = new Posicao(UUID.randomUUID(), WALLET, ASSET, new BigDecimal("3.00000000"),
                new BigDecimal("33.33666667"), new BigDecimal("100.01"), BigDecimal.ZERO.setScale(2), NOW);
        Posicao.Sale sale = held.vender(new BigDecimal("3.00000000"), new BigDecimal("100.01"), NOW.plusSeconds(1));
        assertThat(sale.realizedResult()).isEqualByComparingTo("0.00");
        assertThat(sale.position().quantidade()).isZero();
        assertThat(sale.position().precoMedioBrl()).isZero();
        assertThat(sale.position().totalInvestidoBrl()).isZero();
        Posicao repurchased = sale.position().comprar(new BigDecimal("2.00000000"), new BigDecimal("25.00"), NOW.plusSeconds(2));
        assertThat(repurchased.id()).isEqualTo(held.id());
        assertThat(repurchased.precoMedioBrl()).isEqualByComparingTo("12.50000000");
    }

    @Test
    void totalSaleAndRepurchaseReusePositionAndPreservePositiveZeroOrNegativeLifetimeProfit() {
        BigDecimal[] histories = {new BigDecimal("25.00"), new BigDecimal("-10.00"), new BigDecimal("-25.00")};
        BigDecimal[] expected = {new BigDecimal("35.00"), BigDecimal.ZERO.setScale(2), new BigDecimal("-15.00")};
        for (int index = 0; index < histories.length; index++) {
            UUID positionId = UUID.randomUUID();
            Posicao held = new Posicao(positionId, WALLET, ASSET, new BigDecimal("2.00000000"),
                    new BigDecimal("10.00000000"), new BigDecimal("20.00"), histories[index], NOW);
            Posicao.Sale sale = held.vender(new BigDecimal("2.00000000"), new BigDecimal("30.00"), NOW.plusSeconds(1));
            assertThat(sale.realizedResult()).isEqualByComparingTo("10.00");
            assertThat(sale.position().id()).isEqualTo(positionId);
            assertThat(sale.position().quantidade()).isZero();
            assertThat(sale.position().totalInvestidoBrl()).isZero();
            assertThat(sale.position().precoMedioBrl()).isZero();
            assertThat(sale.position().lucroRealizadoAcumuladoBrl()).isEqualByComparingTo(expected[index]);

            Posicao repurchased = sale.position().comprar(
                    new BigDecimal("3.00000000"), new BigDecimal("45.00"), NOW.plusSeconds(2));
            assertThat(repurchased.id()).isEqualTo(positionId);
            assertThat(repurchased.quantidade()).isEqualByComparingTo("3.00000000");
            assertThat(repurchased.totalInvestidoBrl()).isEqualByComparingTo("45.00");
            assertThat(repurchased.precoMedioBrl()).isEqualByComparingTo("15.00000000");
            assertThat(repurchased.lucroRealizadoAcumuladoBrl()).isEqualByComparingTo(expected[index]);
        }
    }

    @Test
    void rejectsOversellMissingPositionExtremePartialStateAndDerivedOverflow() {
        Posicao held = empty().comprar(new BigDecimal("1.00000000"), new BigDecimal("10.00"), NOW);
        assertThatThrownBy(() -> held.vender(new BigDecimal("2.00000000"), BigDecimal.ONE, NOW))
                .isInstanceOf(FinancialStateException.class);
        assertThatThrownBy(() -> empty().vender(BigDecimal.ONE.setScale(8), BigDecimal.ONE, NOW))
                .isInstanceOf(FinancialStateException.class);
        Posicao extreme = new Posicao(UUID.randomUUID(), WALLET, ASSET, new BigDecimal("2.00000000"),
                new BigDecimal("0.01000000"), new BigDecimal("0.01"), BigDecimal.ZERO.setScale(2), NOW);
        assertThatThrownBy(() -> extreme.vender(new BigDecimal("1.00000000"), BigDecimal.ZERO.setScale(2), NOW))
                .isInstanceOf(FinancialStateException.class);
        assertThatThrownBy(() -> InvestmentNumbers.derivedMoney(new BigDecimal("10000000000000000.00"), "value"))
                .isInstanceOf(FinancialStateException.class);
        Posicao atProfitLimit = new Posicao(UUID.randomUUID(), WALLET, ASSET, BigDecimal.ONE.setScale(8),
                BigDecimal.ONE.setScale(8), BigDecimal.ONE.setScale(2), InvestmentNumbers.MAX_18_2, NOW);
        assertThatThrownBy(() -> atProfitLimit.vender(BigDecimal.ONE.setScale(8), new BigDecimal("2.00"), NOW))
                .isInstanceOf(FinancialStateException.class);
    }

    @Test
    void fingerprintNormalizesDecimalScaleOffsetAndPostgresqlMicroseconds() {
        UUID broker = UUID.randomUUID();
        UUID fx = UUID.randomUUID();
        Instant instant = Instant.parse("2026-09-11T12:00:00.123456Z");
        String first = InvestmentFingerprint.create(ASSET, broker, TipoTransacao.BUY, new BigDecimal("10"),
                new BigDecimal("20.0"), new BigDecimal("0.00000000"), instant, fx);
        String second = InvestmentFingerprint.create(ASSET, broker, TipoTransacao.BUY, new BigDecimal("10.00000000"),
                new BigDecimal("20.00000000"), BigDecimal.ZERO, instant, fx);
        assertThat(second).isEqualTo(first).hasSize(64);
        assertThat(InvestmentFingerprint.create(ASSET, broker, TipoTransacao.BUY, BigDecimal.TEN,
                new BigDecimal("21"), BigDecimal.ZERO, instant, fx)).isNotEqualTo(first);
    }

    private static Posicao empty() { return Posicao.vazia(WALLET, ASSET, NOW); }
}
