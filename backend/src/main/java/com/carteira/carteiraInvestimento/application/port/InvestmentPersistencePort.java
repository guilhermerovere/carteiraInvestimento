package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.investment.Posicao;
import com.carteira.carteiraInvestimento.domain.investment.Transacao;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentPersistencePort {
    record Wallet(UUID id, UUID userId, BigDecimal balance) { }
    record ProtectedAsset(UUID id, String ticker, String name, Mercado market, Moeda currency, boolean active,
            LogoProvider logoProvider, String logoReference) {
        public ProtectedAsset(UUID id, String ticker, Mercado market, Moeda currency, boolean active) {
            this(id, ticker, ticker, market, currency, active, null, null);
        }
    }
    record ProtectedBroker(UUID id, boolean active) { }
    record LocalExchangeRate(UUID id, MoedaCambio source, MoedaCambio target, BigDecimal rate,
            Instant registeredAt) { }
    record TransactionView(Transacao transaction, String ticker, String assetName, LogoProvider assetLogoProvider,
            String assetLogoReference, String brokerName) {
        public TransactionView(Transacao transaction, String ticker) {
            this(transaction, ticker, ticker, null, null, null);
        }
    }
    record PositionView(Posicao position, String ticker, String name, Mercado market, Moeda currency, boolean active,
            LogoProvider logoProvider, String logoReference) {
        public PositionView(Posicao position, String ticker) {
            this(position, ticker, ticker, null, null, true, null, null);
        }
        public static PositionView from(Posicao position, ProtectedAsset asset) {
            return new PositionView(position, asset.ticker(), asset.name(), asset.market(), asset.currency(),
                    asset.active(), asset.logoProvider(), asset.logoReference());
        }
    }
    record Page<T>(List<T> items, int page, int size, long totalElements, int totalPages) { }
    sealed interface Reservation permits NewReservation, ExistingReservation { }
    record NewReservation(UUID id) implements Reservation { }
    record ExistingReservation(String fingerprint, TransactionView transaction, BigDecimal sourceValue,
            BigDecimal resultingBalance, PositionView resultingPosition) implements Reservation { }

    Optional<Wallet> findPrimaryWallet(UUID userId);
    void lockActiveUser(UUID userId);
    Reservation reserve(UUID walletId, UUID userId, String key, String fingerprint);
    ProtectedAsset lockAsset(UUID assetId);
    ProtectedBroker lockBroker(UUID brokerId);
    Optional<LocalExchangeRate> findExchangeRate(UUID exchangeRateId);
    Wallet lockWallet(UUID walletId);
    Optional<Posicao> lockPosition(UUID walletId, UUID assetId);
    BigDecimal debit(UUID walletId, BigDecimal amount);
    BigDecimal credit(UUID walletId, BigDecimal amount);
    void insertPosition(Posicao position);
    void updatePosition(Posicao position);
    void insertTransaction(Transacao transaction);
    void composeInvestmentSnapshot(UUID walletId, LocalDate referenceDate, BigDecimal balance);
    void complete(UUID reservationId, TransactionView transaction, BigDecimal sourceValue,
            BigDecimal resultingBalance, Posicao resultingPosition);
    Page<TransactionView> transactions(UUID walletId, int page, int size);
    Optional<TransactionView> transaction(UUID transactionId);
    Page<PositionView> openPositions(UUID walletId, int page, int size);
    Optional<PositionView> position(UUID walletId, UUID assetId);
}
