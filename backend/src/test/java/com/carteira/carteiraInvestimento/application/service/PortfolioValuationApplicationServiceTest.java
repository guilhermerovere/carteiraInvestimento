package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.carteira.carteiraInvestimento.domain.investment.FinancialStateException;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

class PortfolioValuationApplicationServiceTest {
    private static final UUID USER = UUID.randomUUID();
    private static final UUID WALLET = UUID.randomUUID();
    private final MarketQuoteUseCase quotes = mock(MarketQuoteUseCase.class);
    private final CambioUseCase exchange = mock(CambioUseCase.class);
    private final FakePort persistence = new FakePort();
    private final Instant now = Instant.parse("2026-09-12T12:00:00.123456789Z");
    private PortfolioValuationApplicationService service;

    @BeforeEach
    void setup() {
        service = new PortfolioValuationApplicationService(persistence, quotes, exchange, passthrough(),
                passthrough(), Clock.fixed(now, ZoneId.of("UTC")), ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    void emptyWalletUsesNoProviderAndGetDoesNotMaterialize() {
        persistence.states.add(state("25.00", 0, List.of(), "7.50"));
        var result = service.summarize(USER);
        assertThat(result.valuationInstant()).isEqualTo(Instant.parse("2026-09-12T12:00:00.123456Z"));
        assertThat(result.investedBrl()).isEqualByComparingTo("0.00");
        assertThat(result.positionsValueBrl()).isEqualByComparingTo("0.00");
        assertThat(result.unrealizedProfitBrl()).isEqualByComparingTo("0.00");
        assertThat(result.accumulatedRealizedProfitBrl()).isEqualByComparingTo("7.50");
        assertThat(result.totalEquityBrl()).isEqualByComparingTo("25.00");
        assertThat(result.unrealizedReturnPercentage()).isNull();
        assertThat(result.currentExchangeRate()).isNull();
        assertThat(result.positions()).isEmpty();
        assertThat(persistence.materializeFlags).containsExactly(false);
        verifyNoInteractions(quotes, exchange);
    }

    @Test
    void calculatesB3AndUsWithOneSharedFxHalfEvenAndHistoricalRealizedProfit() {
        UUID b3 = UUID.randomUUID(); UUID us = UUID.randomUUID();
        persistence.states.add(state("100.00", 4, List.of(
                position(b3, "PETR4", Mercado.B3, Moeda.BRL, "2.00000000", "20.00000000", "40.00"),
                position(us, "AAPL", Mercado.US, Moeda.USD, "3.00000000", "25.00000000", "75.00")), "12.34"));
        when(quotes.cotacaoAtualParaCustodia(b3, true)).thenReturn(quote(b3, Moeda.BRL, "20.0025", QuoteProvider.BRAPI));
        when(quotes.cotacaoAtualParaCustodia(us, true)).thenReturn(quote(us, Moeda.USD, "10.0000", QuoteProvider.TWELVE_DATA));
        when(exchange.obterUsdBrl()).thenReturn(fx("5.00000000"));
        var result = service.refresh(USER);
        assertThat(result.positions()).hasSize(2);
        assertThat(result.positions().get(0).currentValueSource()).isEqualByComparingTo("40.005000000000");
        assertThat(result.positions().get(0).currentValueBrl()).isEqualByComparingTo("40.00");
        assertThat(result.positions().get(1).currentValueBrl()).isEqualByComparingTo("150.00");
        assertThat(result.investedBrl()).isEqualByComparingTo("115.00");
        assertThat(result.positionsValueBrl()).isEqualByComparingTo("190.00");
        assertThat(result.unrealizedProfitBrl()).isEqualByComparingTo("75.00");
        assertThat(result.totalEquityBrl()).isEqualByComparingTo("290.00");
        assertThat(result.unrealizedReturnPercentage()).isEqualByComparingTo("65.2174");
        assertThat(result.accumulatedRealizedProfitBrl()).isEqualByComparingTo("12.34");
        assertThat(result.currentExchangeRate().rateBrl()).isEqualByComparingTo("5.00000000");
        verify(exchange, times(1)).obterUsdBrl();
        assertThat(persistence.materializeFlags).containsExactly(true);
    }

    @Test
    void retriesCompletelyWithNewInstantAndNewProviderInvocation() {
        UUID asset = UUID.randomUUID();
        var local = state("10.00", 1, List.of(position(asset, "PETR4", Mercado.B3, Moeda.BRL,
                "1.00000000", "10.00000000", "10.00")), "0.00");
        persistence.states.add(local); persistence.states.add(local);
        persistence.validationResults.add(false); persistence.validationResults.add(true);
        when(quotes.cotacaoAtualParaCustodia(asset, true)).thenReturn(quote(asset, Moeda.BRL, "11.0000", QuoteProvider.BRAPI));
        var result = service.summarize(USER);
        verify(quotes, times(2)).cotacaoAtualParaCustodia(asset, true);
        assertThat(persistence.instants).containsExactly(now.truncatedTo(java.time.temporal.ChronoUnit.MICROS),
                now.truncatedTo(java.time.temporal.ChronoUnit.MICROS).plusNanos(1_000));
        assertThat(result.valuationInstant()).isEqualTo(persistence.instants.get(1));
    }

    @Test
    void secondVersionLossReturnsConflict() {
        persistence.states.add(state("1.00", 1, List.of(), "0.00"));
        persistence.states.add(state("2.00", 2, List.of(), "0.00"));
        persistence.validationResults.add(false); persistence.validationResults.add(false);
        assertThatThrownBy(() -> service.refresh(USER)).isInstanceOf(PortfolioValuationConflictException.class);
        assertThat(persistence.materializeFlags).containsExactly(true, true);
    }

    @Test
    void quoteFailureReturnsPartialPersistedSummaryAndRecoversOnRefetch() {
        UUID asset = UUID.randomUUID();
        var local = state("10.00", 1, List.of(position(asset, "PETR4", Mercado.B3, Moeda.BRL,
                "1.00000000", "10.00000000", "10.00")), "0.00");
        persistence.states.add(local); persistence.states.add(local);
        when(quotes.cotacaoAtualParaCustodia(asset, true)).thenThrow(new QuoteNotFoundException())
                .thenReturn(quote(asset, Moeda.BRL, "12.0000", QuoteProvider.BRAPI));

        var partial = service.refresh(USER);
        assertThat(partial.marketDataAvailable()).isFalse();
        assertThat(partial.cashBalanceBrl()).isEqualByComparingTo("10.00");
        assertThat(partial.investedBrl()).isEqualByComparingTo("10.00");
        assertThat(partial.positionsValueBrl()).isNull();
        assertThat(partial.unrealizedProfitBrl()).isNull();
        assertThat(partial.totalEquityBrl()).isNull();
        assertThat(partial.positions()).singleElement().satisfies(position -> {
            assertThat(position.assetId()).isEqualTo(asset);
            assertThat(position.quantity()).isEqualByComparingTo("1.00000000");
            assertThat(position.averagePriceBrl()).isEqualByComparingTo("10.00000000");
            assertThat(position.investedBrl()).isEqualByComparingTo("10.00");
            assertThat(position.marketDataAvailable()).isFalse();
            assertThat(position.currentQuote()).isNull();
            assertThat(position.quoteProvider()).isNull();
            assertThat(position.currentValueBrl()).isNull();
        });
        assertThat(persistence.instants).isEmpty();

        var recovered = service.summarize(USER);
        assertThat(recovered.marketDataAvailable()).isTrue();
        assertThat(recovered.positions()).singleElement().satisfies(position -> {
            assertThat(position.currentQuote()).isEqualByComparingTo("12.0000");
            assertThat(position.quoteProvider()).isEqualTo(QuoteProvider.BRAPI);
            assertThat(position.currentValueBrl()).isEqualByComparingTo("12.00");
        });
        assertThat(recovered.totalEquityBrl()).isEqualByComparingTo("22.00");
        assertThat(persistence.materializeFlags).containsExactly(false);
        verify(quotes, times(2)).cotacaoAtualParaCustodia(asset, true);
    }

    @Test
    void derivedOverflowIsConflictAndDoesNotMaterialize() {
        UUID asset = UUID.randomUUID();
        persistence.states.add(state("0.00", 1, List.of(position(asset, "MAXI4", Mercado.B3, Moeda.BRL,
                "9999999999.99999999", "1.00000000", "1.00")), "0.00"));
        when(quotes.cotacaoAtualParaCustodia(asset, true)).thenReturn(quote(asset, Moeda.BRL,
                "99999999999.9999", QuoteProvider.BRAPI));
        assertThatThrownBy(() -> service.refresh(USER)).isInstanceOf(FinancialStateException.class);
        assertThat(persistence.instants).isEmpty();
    }

    private PortfolioValuationPort.LocalState state(String cash, long version,
            List<PortfolioValuationPort.OpenPosition> positions, String realized) {
        return new PortfolioValuationPort.LocalState(WALLET, new BigDecimal(cash), version, positions,
                new BigDecimal(realized));
    }
    private PortfolioValuationPort.OpenPosition position(UUID id, String ticker, Mercado market, Moeda currency,
            String quantity, String average, String invested) {
        return new PortfolioValuationPort.OpenPosition(id, ticker, market, currency, true,
                new BigDecimal(quantity), new BigDecimal(average), new BigDecimal(invested));
    }
    private Cotacao quote(UUID id, Moeda currency, String price, QuoteProvider provider) {
        return new Cotacao(UUID.randomUUID(), id, new BigDecimal(price), currency, now.minusSeconds(1), provider, now);
    }
    private ObservacaoCambio fx(String rate) {
        return new ObservacaoCambio(UUID.randomUUID(), MoedaCambio.USD, MoedaCambio.BRL, new BigDecimal(rate),
                CambioProvider.TWELVE_DATA, now.minusSeconds(1), now);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TransactionTemplate passthrough() {
        TransactionTemplate template = mock(TransactionTemplate.class);
        when(template.execute(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation ->
                ((TransactionCallback) invocation.getArgument(0)).doInTransaction(mock(TransactionStatus.class)));
        return template;
    }

    private static final class FakePort implements PortfolioValuationPort {
        final Queue<LocalState> states = new ArrayDeque<>();
        final Queue<Boolean> validationResults = new ArrayDeque<>();
        final List<Boolean> materializeFlags = new ArrayList<>();
        final List<Instant> instants = new ArrayList<>();
        @Override public LocalState readLocalState(UUID userId) { return states.remove(); }
        @Override public boolean validateAndOptionallyMaterialize(UUID walletId, long expectedVersion,
                java.time.LocalDate referenceDate, Instant valuationInstant, BigDecimal cashBalanceBrl,
                MaterializedTotals totals, boolean materialize) {
            materializeFlags.add(materialize); instants.add(valuationInstant);
            return validationResults.isEmpty() || validationResults.remove();
        }
    }
}
