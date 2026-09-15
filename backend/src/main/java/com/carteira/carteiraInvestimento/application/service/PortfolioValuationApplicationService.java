package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort;
import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort.LocalState;
import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort.MaterializedTotals;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.carteira.carteiraInvestimento.domain.investment.FinancialStateException;
import com.carteira.carteiraInvestimento.domain.investment.InvestmentNumbers;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.support.TransactionTemplate;

public class PortfolioValuationApplicationService implements PortfolioValuationUseCase {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal MAX_PERCENT = new BigDecimal("99999999999999.9999");
    private final PortfolioValuationPort persistence;
    private final MarketQuoteUseCase quotes;
    private final CambioUseCase exchangeRates;
    private final TransactionTemplate initialRead;
    private final TransactionTemplate finalValidation;
    private final Clock clock;
    private final ZoneId snapshotZone;

    public PortfolioValuationApplicationService(PortfolioValuationPort persistence, MarketQuoteUseCase quotes,
            CambioUseCase exchangeRates, TransactionTemplate initialRead, TransactionTemplate finalValidation,
            Clock clock, ZoneId snapshotZone) {
        this.persistence = persistence;
        this.quotes = quotes;
        this.exchangeRates = exchangeRates;
        this.initialRead = initialRead;
        this.finalValidation = finalValidation;
        this.clock = clock;
        this.snapshotZone = snapshotZone;
    }

    @Override public PortfolioValuationResult summarize(UUID userId) { return execute(userId, false); }
    @Override public PortfolioValuationResult refresh(UUID userId) { return execute(userId, true); }

    private PortfolioValuationResult execute(UUID userId, boolean materialize) {
        Instant previousInstant = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            LocalState local = initialRead.execute(status -> persistence.readLocalState(userId));
            if (local == null) throw new IllegalStateException("local valuation state unavailable");
            Instant valuationInstant = nextInstant(previousInstant);
            PortfolioValuationResult calculated = calculate(local, valuationInstant);
            if (!calculated.marketDataAvailable()) return calculated;
            MaterializedTotals totals = new MaterializedTotals(calculated.investedBrl(),
                    calculated.positionsValueBrl(), calculated.unrealizedProfitBrl(), calculated.totalEquityBrl());
            Boolean valid = finalValidation.execute(status -> persistence.validateAndOptionallyMaterialize(
                    local.walletId(), local.stateVersion(), valuationInstant.atZone(snapshotZone).toLocalDate(),
                    valuationInstant, local.cashBalanceBrl(), totals, materialize));
            if (Boolean.TRUE.equals(valid)) return calculated;
            previousInstant = valuationInstant;
        }
        throw new PortfolioValuationConflictException("wallet state changed during both valuation attempts");
    }

    private Instant nextInstant(Instant previous) {
        Instant current = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (previous != null && !current.isAfter(previous)) return previous.plus(1, ChronoUnit.MICROS);
        return current;
    }

    private PortfolioValuationResult calculate(LocalState local, Instant valuationInstant) {
        ObservacaoCambio fx = null; boolean fxAvailable = true;
        if (local.openPositions().stream().anyMatch(position -> position.market() == Mercado.US)) {
            try { fx = exchangeRates.obterUsdBrl(); } catch (CambioUnavailableException exception) { fxAvailable = false; }
        }
        List<PortfolioValuationResult.ValuedPosition> valued = new ArrayList<>();
        BigDecimal invested = InvestmentNumbers.ZERO_2, positionsValue = InvestmentNumbers.ZERO_2, unrealized = InvestmentNumbers.ZERO_2;
        boolean marketDataAvailable = true;
        for (var position : local.openPositions()) {
            invested = InvestmentNumbers.derivedMoney(invested.add(position.investedBrl()), "invested total");
            if (position.market() == Mercado.US && !fxAvailable) { marketDataAvailable = false; valued.add(unavailable(position)); continue; }
            Cotacao quote;
            try { quote = quotes.cotacaoAtualParaCustodia(position.assetId(), true); }
            catch (QuoteIntegrationException | QuoteNotFoundException exception) { marketDataAvailable = false; valued.add(unavailable(position)); continue; }
            if (quote.moeda() != position.currency()) { marketDataAvailable = false; valued.add(unavailable(position)); continue; }
            BigDecimal sourceValue = position.quantity().multiply(quote.preco());
            BigDecimal exactBrl = position.market() == Mercado.B3 ? sourceValue : sourceValue.multiply(fx.taxa());
            BigDecimal valueBrl = InvestmentNumbers.derivedMoney(exactBrl, "position valuation");
            BigDecimal profit = InvestmentNumbers.derivedMoney(valueBrl.subtract(position.investedBrl()), "unrealized profit");
            BigDecimal percentage = percentage(profit, position.investedBrl());
            positionsValue = InvestmentNumbers.derivedMoney(positionsValue.add(valueBrl), "positions total");
            unrealized = InvestmentNumbers.derivedMoney(unrealized.add(profit), "unrealized total");
            valued.add(new PortfolioValuationResult.ValuedPosition(position.assetId(), position.ticker(), position.type(), position.market(),
                    position.currency(), position.quantity(), position.averagePriceBrl(), position.investedBrl(), quote.preco(),
                    quote.provider(), quote.instanteCotacao(), sourceValue, valueBrl, profit, percentage, true));
        }
        BigDecimal realized = InvestmentNumbers.derivedMoney(local.accumulatedRealizedProfitBrl(), "realized profit total");
        BigDecimal equity = marketDataAvailable ? InvestmentNumbers.derivedMoney(local.cashBalanceBrl().add(positionsValue), "total equity") : null;
        BigDecimal aggregatePercentage = marketDataAvailable && invested.signum() != 0 ? percentage(unrealized, invested) : null;
        PortfolioValuationResult.CurrentExchangeRate currentFx = fx == null ? null : new PortfolioValuationResult.CurrentExchangeRate(fx.taxa(), fx.provider(), fx.instanteCotacao());
        return new PortfolioValuationResult(valuationInstant, local.cashBalanceBrl(), invested, marketDataAvailable ? positionsValue : null,
                marketDataAvailable ? unrealized : null, realized, equity, aggregatePercentage, currentFx, List.copyOf(valued), marketDataAvailable);
    }

    private static PortfolioValuationResult.ValuedPosition unavailable(PortfolioValuationPort.OpenPosition position) {
        return new PortfolioValuationResult.ValuedPosition(position.assetId(), position.ticker(), position.type(), position.market(), position.currency(),
                position.quantity(), position.averagePriceBrl(), position.investedBrl(), null, null, null, null, null, null, null, false);
    }

    private static BigDecimal percentage(BigDecimal profit, BigDecimal invested) {
        if (invested.signum() == 0) return null;
        BigDecimal result = profit.multiply(ONE_HUNDRED).divide(invested, 4, RoundingMode.HALF_EVEN);
        if (result.abs().compareTo(MAX_PERCENT) > 0) throw new FinancialStateException("return overflow");
        return result;
    }
}
