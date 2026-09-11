package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CashWalletPort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.ExistingReservation;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.NewReservation;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.ProtectedAsset;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.ProtectedBroker;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.TransactionView;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.Wallet;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.investment.InvestmentNumbers;
import com.carteira.carteiraInvestimento.domain.investment.Posicao;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.carteira.carteiraInvestimento.domain.investment.Transacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class InvestmentApplicationServiceTest {
    private final InvestmentPersistencePort persistence = mock(InvestmentPersistencePort.class);
    private final AuditoriaPort audit = mock(AuditoriaPort.class);
    private final Clock clock = mock(Clock.class);
    private final UUID user = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();
    private final UUID assetId = UUID.randomUUID();
    private final UUID brokerId = UUID.randomUUID();
    private final Instant operation = Instant.parse("2026-09-11T15:00:00.123456789Z");
    private final InvestmentApplicationService service = new InvestmentApplicationService(persistence, audit, clock,
            ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void wallet() {
        when(persistence.findPrimaryWallet(user)).thenReturn(Optional.of(new Wallet(walletId, user, new BigDecimal("10000.00"))));
    }

    @Test
    void buyB3UsesOneInstantDoesNotReadFxAndPersistsAtomicProjection() {
        stubNew(Mercado.B3, true, null);
        when(clock.instant()).thenReturn(operation);
        when(persistence.lockPosition(walletId, assetId)).thenReturn(Optional.empty());
        when(persistence.debit(walletId, new BigDecimal("1000.50"))).thenReturn(new BigDecimal("8999.50"));

        var result = service.execute(user, command(TipoTransacao.BUY, "10", "100", "0.50", null),
                "buy-1", UUID.randomUUID(), "/api/v1/carteira/transacoes");

        assertThat(result.sourceValue()).isEqualByComparingTo("1000.0000000000000000");
        assertThat(result.transaction().transaction().valorTotalBrl()).isEqualByComparingTo("1000.50");
        assertThat(result.transaction().transaction().resultadoRealizadoBrl()).isNull();
        assertThat(result.transaction().transaction().dataRegistro()).isEqualTo(Instant.parse("2026-09-11T15:00:00.123456Z"));
        assertThat(result.position().position().ultimaAtualizacao()).isEqualTo(result.transaction().transaction().dataRegistro());
        verify(persistence, never()).findExchangeRate(any());
        ArgumentCaptor<AuditoriaCommand> command = ArgumentCaptor.forClass(AuditoriaCommand.class);
        verify(audit).record(command.capture());
        assertThat(command.getValue().tipo()).isEqualTo(TipoEvento.COMPRA);
        assertThat(command.getValue().instante()).isEqualTo(result.transaction().transaction().dataRegistro());
        verify(persistence).complete(any(), any(), eq(result.sourceValue()), eq(new BigDecimal("8999.50")), any());
    }

    @Test
    void buyMaintainsTheGlobalLockAndCompletionOrder() {
        stubNew(Mercado.B3, true, null);
        when(clock.instant()).thenReturn(operation);
        when(persistence.lockPosition(walletId, assetId)).thenReturn(Optional.empty());
        when(persistence.debit(walletId, new BigDecimal("10.00"))).thenReturn(new BigDecimal("9990.00"));

        service.execute(user, command(TipoTransacao.BUY, "1", "10", "0", null), "ordered-buy",
                UUID.randomUUID(), "/transactions");

        InOrder order = inOrder(persistence, audit);
        order.verify(persistence).reserve(eq(walletId), eq(user), eq("ordered-buy"), any());
        order.verify(persistence).lockAsset(assetId);
        order.verify(persistence).lockBroker(brokerId);
        order.verify(persistence).lockWallet(walletId);
        order.verify(persistence).lockPosition(walletId, assetId);
        order.verify(persistence).debit(walletId, new BigDecimal("10.00"));
        order.verify(persistence).insertPosition(any());
        order.verify(persistence).insertTransaction(any());
        order.verify(persistence).composeInvestmentSnapshot(eq(walletId), any(), eq(new BigDecimal("9990.00")));
        order.verify(audit).record(any());
        order.verify(persistence).complete(any(), any(), any(), eq(new BigDecimal("9990.00")), any());
    }

    @Test
    void sellMaintainsTheGlobalLockAndCompletionOrder() {
        stubNew(Mercado.B3, true, null);
        when(clock.instant()).thenReturn(operation);
        Posicao held = new Posicao(UUID.randomUUID(), walletId, assetId, new BigDecimal("2.00000000"),
                new BigDecimal("10.00000000"), new BigDecimal("20.00"), BigDecimal.ZERO.setScale(2), operation);
        when(persistence.lockPosition(walletId, assetId)).thenReturn(Optional.of(held));
        when(persistence.credit(walletId, new BigDecimal("10.00"))).thenReturn(new BigDecimal("10010.00"));

        service.execute(user, command(TipoTransacao.SELL, "1", "10", "0", null), "ordered-sell",
                UUID.randomUUID(), "/transactions");

        InOrder order = inOrder(persistence, audit);
        order.verify(persistence).reserve(eq(walletId), eq(user), eq("ordered-sell"), any());
        order.verify(persistence).lockAsset(assetId);
        order.verify(persistence).lockBroker(brokerId);
        order.verify(persistence).lockWallet(walletId);
        order.verify(persistence).lockPosition(walletId, assetId);
        order.verify(persistence).credit(walletId, new BigDecimal("10.00"));
        order.verify(persistence).updatePosition(any());
        order.verify(persistence).insertTransaction(any());
        order.verify(persistence).composeInvestmentSnapshot(eq(walletId), any(), eq(new BigDecimal("10010.00")));
        order.verify(audit).record(any());
        order.verify(persistence).complete(any(), any(), any(), eq(new BigDecimal("10010.00")), any());
    }

    @Test
    void cashWalletContractCannotAcquireInvestmentPositions() {
        assertThat(CashWalletPort.class.getDeclaredMethods())
                .allSatisfy(method -> assertThat(method.toGenericString()).doesNotContain("Posicao", "Position"));
        assertThat(CashMovementApplicationService.class.getDeclaredFields())
                .allSatisfy(field -> assertThat(field.getType()).isNotEqualTo(InvestmentPersistencePort.class));
    }

    @Test
    void financialPostHasNoExternalProviderDependency() {
        assertThat(InvestmentApplicationService.class.getDeclaredConstructors()).singleElement()
                .satisfies(constructor -> assertThat(Set.of(constructor.getParameterTypes())).containsExactlyInAnyOrder(
                        InvestmentPersistencePort.class, AuditoriaPort.class, Clock.class, ZoneId.class));
    }

    @Test
    void exactFxDeadlineIsInclusiveAndFirstMicrosecondAfterItConflicts() {
        UUID fxId = UUID.randomUUID();
        Instant registered = Instant.parse("2026-09-11T14:55:00.123456Z");
        stubNew(Mercado.US, true, fxId);
        var localRate = new InvestmentPersistencePort.LocalExchangeRate(
                fxId, MoedaCambio.USD, MoedaCambio.BRL, new BigDecimal("5.00000000"), registered);
        when(persistence.findExchangeRate(fxId)).thenReturn(Optional.of(localRate));
        when(clock.instant()).thenReturn(registered.plusSeconds(300));
        when(persistence.lockPosition(walletId, assetId)).thenReturn(Optional.empty());
        when(persistence.debit(walletId, new BigDecimal("500.00"))).thenReturn(new BigDecimal("9500.00"));
        assertThat(service.execute(user, command(TipoTransacao.BUY, "1", "100", "0", fxId), "fx-deadline",
                UUID.randomUUID(), "/transactions").transaction().transaction().taxaCambioBrl())
                .isEqualByComparingTo("5.00000000");

        InvestmentPersistencePort second = mock(InvestmentPersistencePort.class);
        AuditoriaPort secondAudit = mock(AuditoriaPort.class);
        when(second.findPrimaryWallet(user)).thenReturn(Optional.of(new Wallet(walletId, user, BigDecimal.TEN)));
        when(second.reserve(eq(walletId), eq(user), eq("expired"), any())).thenReturn(new NewReservation(UUID.randomUUID()));
        when(second.lockAsset(assetId)).thenReturn(new ProtectedAsset(assetId, "AAPL", Mercado.US, Moeda.USD, true));
        when(second.lockBroker(brokerId)).thenReturn(new ProtectedBroker(brokerId, true));
        when(second.findExchangeRate(fxId)).thenReturn(Optional.of(localRate));
        Clock late = Clock.fixed(registered.plusSeconds(300).plusNanos(1000), ZoneId.of("UTC"));
        var lateService = new InvestmentApplicationService(second, secondAudit, late, ZoneId.of("America/Sao_Paulo"));
        assertThatThrownBy(() -> lateService.execute(user, command(TipoTransacao.BUY, "1", "100", "0", fxId),
                "expired", UUID.randomUUID(), "/transactions")).isInstanceOf(InvestmentConflictException.class);
        verify(second, never()).lockWallet(any());
    }

    @Test
    void replayReturnsCapturedResultWithoutClockOrVolatileReads() {
        Instant saved = Instant.parse("2026-01-01T10:00:00.123456Z");
        Posicao position = new Posicao(UUID.randomUUID(), walletId, assetId, new BigDecimal("2.00000000"),
                new BigDecimal("10.00000000"), new BigDecimal("20.00"), BigDecimal.ZERO.setScale(2), saved);
        Transacao transaction = new Transacao(UUID.randomUUID(), walletId, user, assetId, brokerId, null,
                TipoTransacao.BUY, new BigDecimal("2.00000000"), Moeda.BRL, new BigDecimal("10.00000000"),
                BigDecimal.ZERO.setScale(8), InvestmentNumbers.ONE_8, new BigDecimal("20.00"), null, saved, saved);
        var request = command(TipoTransacao.BUY, "2", "10", "0", null);
        String fingerprint = com.carteira.carteiraInvestimento.domain.investment.InvestmentFingerprint.create(assetId,
                brokerId, TipoTransacao.BUY, new BigDecimal("2.00000000"), new BigDecimal("10.00000000"),
                BigDecimal.ZERO.setScale(8), request.tradeDate().toInstant(), null);
        when(persistence.reserve(walletId, user, "replay", fingerprint)).thenReturn(new ExistingReservation(fingerprint,
                new TransactionView(transaction, "PETR4"), new BigDecimal("20.0000000000000000"),
                new BigDecimal("80.00"), new PositionView(position, "PETR4")));

        var replay = service.execute(user, request, "replay", UUID.randomUUID(), "/transactions");
        assertThat(replay.transaction().transaction()).isEqualTo(transaction);
        assertThat(replay.position().position()).isEqualTo(position);
        verify(clock, never()).instant();
        verify(persistence, never()).lockAsset(any());
        verify(persistence, never()).lockBroker(any());
        verify(persistence, never()).findExchangeRate(any());
        verify(persistence, never()).lockWallet(any());
        verify(persistence, never()).lockPosition(any(), any());
        verify(audit, never()).record(any());
    }

    @Test
    void divergentReplayConflictsBeforeVolatileValidation() {
        when(persistence.reserve(eq(walletId), eq(user), eq("same"), any()))
                .thenReturn(new ExistingReservation("0".repeat(64), null, null, null, null));
        assertThatThrownBy(() -> service.execute(user, command(TipoTransacao.BUY, "1", "10", "0", null),
                "same", UUID.randomUUID(), "/transactions")).isInstanceOf(InvestmentConflictException.class);
        verify(clock, never()).instant(); verify(persistence, never()).lockAsset(any());
    }

    @Test
    void validatesInputBeforeWalletAndB3FxShapeAsBadRequest() {
        assertThatThrownBy(() -> service.execute(user, command(TipoTransacao.BUY, "1.123456789", "10", "0", null),
                "invalid", UUID.randomUUID(), "/transactions")).isInstanceOf(IllegalArgumentException.class);
        verify(persistence, never()).reserve(any(), any(), any(), any());

        stubNew(Mercado.B3, true, UUID.randomUUID()); when(clock.instant()).thenReturn(operation);
        assertThatThrownBy(() -> service.execute(user, command(TipoTransacao.BUY, "1", "10", "0", UUID.randomUUID()),
                "shape", UUID.randomUUID(), "/transactions")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sellAllowsInactiveAssetAndFeesEqualGrossWithZeroNetProceeds() {
        stubNew(Mercado.B3, false, null); when(clock.instant()).thenReturn(operation);
        Posicao held=new Posicao(UUID.randomUUID(),walletId,assetId,new BigDecimal("1.00000000"),
                new BigDecimal("100.00000000"),new BigDecimal("100.00"),BigDecimal.ZERO.setScale(2),operation.minusSeconds(1));
        when(persistence.lockPosition(walletId,assetId)).thenReturn(Optional.of(held));
        when(persistence.credit(walletId,BigDecimal.ZERO.setScale(2))).thenReturn(new BigDecimal("10000.00"));
        var result=service.execute(user,command(TipoTransacao.SELL,"1","10","10",null),"sell-zero",
                UUID.randomUUID(),"/transactions");
        assertThat(result.transaction().transaction().valorTotalBrl()).isZero();
        assertThat(result.transaction().transaction().resultadoRealizadoBrl()).isEqualByComparingTo("-100.00");
        assertThat(result.position().position().quantidade()).isZero();
        ArgumentCaptor<AuditoriaCommand> event=ArgumentCaptor.forClass(AuditoriaCommand.class);
        verify(audit).record(event.capture());assertThat(event.getValue().tipo()).isEqualTo(TipoEvento.VENDA);
    }

    @Test
    void buyRejectsInactiveLifecycleAndSellFeesAboveGross() {
        stubNew(Mercado.B3,false,null);when(clock.instant()).thenReturn(operation);
        assertThatThrownBy(()->service.execute(user,command(TipoTransacao.BUY,"1","10","0",null),"inactive",
                UUID.randomUUID(),"/transactions")).isInstanceOf(InvestmentConflictException.class);
        verify(persistence,never()).lockBroker(any());

        InvestmentPersistencePort other=mock(InvestmentPersistencePort.class);AuditoriaPort otherAudit=mock(AuditoriaPort.class);
        when(other.findPrimaryWallet(user)).thenReturn(Optional.of(new Wallet(walletId,user,BigDecimal.TEN)));
        when(other.reserve(eq(walletId),eq(user),any(),any())).thenReturn(new NewReservation(UUID.randomUUID()));
        when(other.lockAsset(assetId)).thenReturn(new ProtectedAsset(assetId,"PETR4",Mercado.B3,Moeda.BRL,true));
        when(other.lockBroker(brokerId)).thenReturn(new ProtectedBroker(brokerId,true));
        when(other.lockWallet(walletId)).thenReturn(new Wallet(walletId,user,BigDecimal.TEN));
        when(other.lockPosition(walletId,assetId)).thenReturn(Optional.of(new Posicao(UUID.randomUUID(),walletId,
                assetId,BigDecimal.ONE.setScale(8),BigDecimal.TEN.setScale(8),BigDecimal.TEN.setScale(2),
                BigDecimal.ZERO.setScale(2),operation)));
        var otherService=new InvestmentApplicationService(other,otherAudit,Clock.fixed(operation,ZoneId.of("UTC")),ZoneId.of("America/Sao_Paulo"));
        assertThatThrownBy(()->otherService.execute(user,command(TipoTransacao.SELL,"1","10","10.00000001",null),
                "fees",UUID.randomUUID(),"/transactions")).isInstanceOf(InvestmentConflictException.class);
        verify(other,never()).credit(any(),any());
    }

    @Test
    void idempotencyKeyContractIsStrictAndNeverTrimmed() {
        for(String key:new String[]{""," leading","trailing ","invalid/key","a".repeat(129)})
            assertThatThrownBy(()->service.execute(user,command(TipoTransacao.BUY,"1","10","0",null),key,
                    UUID.randomUUID(),"/transactions")).isInstanceOf(IllegalArgumentException.class);
        verify(persistence,never()).reserve(any(),any(),any(),any());
    }

    private void stubNew(Mercado market, boolean active, UUID fxId) {
        when(persistence.reserve(eq(walletId), eq(user), any(), any())).thenReturn(new NewReservation(UUID.randomUUID()));
        when(persistence.lockAsset(assetId)).thenReturn(new ProtectedAsset(assetId,
                market == Mercado.B3 ? "PETR4" : "AAPL", market, market == Mercado.B3 ? Moeda.BRL : Moeda.USD, active));
        when(persistence.lockBroker(brokerId)).thenReturn(new ProtectedBroker(brokerId, true));
        when(persistence.lockWallet(walletId)).thenReturn(new Wallet(walletId, user, new BigDecimal("10000.00")));
    }

    private InvestmentTransactionCommand command(TipoTransacao type, String quantity, String price,
            String fees, UUID fxId) {
        return new InvestmentTransactionCommand(assetId, brokerId, type, new BigDecimal(quantity),
                new BigDecimal(price), new BigDecimal(fees), OffsetDateTime.parse("2026-01-01T07:00:00-03:00"), fxId);
    }
}
