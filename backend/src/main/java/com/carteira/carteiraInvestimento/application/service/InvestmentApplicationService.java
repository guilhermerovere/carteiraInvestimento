package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.ExistingReservation;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.NewReservation;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.Page;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.PositionView;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort.TransactionView;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.investment.FinancialStateException;
import com.carteira.carteiraInvestimento.domain.investment.InvestmentFingerprint;
import com.carteira.carteiraInvestimento.domain.investment.InvestmentNumbers;
import com.carteira.carteiraInvestimento.domain.investment.Posicao;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.carteira.carteiraInvestimento.domain.investment.Transacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

public class InvestmentApplicationService implements InvestmentUseCase {
    private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$");
    private final InvestmentPersistencePort persistence;
    private final AuditoriaPort audit;
    private final Clock clock;
    private final ZoneId snapshotZone;

    public InvestmentApplicationService(InvestmentPersistencePort persistence, AuditoriaPort audit,
            Clock clock, ZoneId snapshotZone) {
        this.persistence = persistence; this.audit = audit; this.clock = clock; this.snapshotZone = snapshotZone;
    }

    @Override
    @Transactional
    public InvestmentOperationResult execute(UUID userId, InvestmentTransactionCommand raw, String key,
            UUID correlationId, String endpoint) {
        Objects.requireNonNull(userId, "userId is required");
        persistence.lockActiveUser(userId);
        if (raw == null || raw.assetId() == null || raw.brokerId() == null || raw.type() == null
                || raw.tradeDate() == null) throw new IllegalArgumentException("missing transaction field");
        if (key == null || !KEY.matcher(key).matches()) throw new IllegalArgumentException("invalid idempotency key");
        BigDecimal quantity = InvestmentNumbers.positiveIntegerInput(raw.quantity(), "quantity");
        BigDecimal unitPrice = InvestmentNumbers.positiveInput(raw.unitPrice(), "unit price");
        BigDecimal fees = InvestmentNumbers.nonNegativeInput(raw.fees(), "fees");
        Instant tradeInstant = micros(raw.tradeDate().toInstant());
        String fingerprint = InvestmentFingerprint.create(raw.assetId(), raw.brokerId(), raw.type(), quantity,
                unitPrice, fees, tradeInstant, raw.exchangeRateId());

        var wallet = persistence.findPrimaryWallet(userId).orElseThrow(PrimaryWalletMissingException::new);
        var reservation = persistence.reserve(wallet.id(), userId, key, fingerprint);
        if (reservation instanceof ExistingReservation existing) {
            if (!fingerprint.equals(existing.fingerprint())) throw new InvestmentConflictException("idempotency conflict");
            if (existing.transaction() == null || existing.sourceValue() == null
                    || existing.resultingBalance() == null || existing.resultingPosition() == null)
                throw new IllegalStateException("completed idempotency result is incomplete");
            return new InvestmentOperationResult(existing.transaction(), existing.sourceValue(),
                    existing.resultingBalance(), existing.resultingPosition());
        }

        Instant operationInstant = micros(clock.instant());
        var asset = persistence.lockAsset(raw.assetId());
        validateAssetAndFxShape(asset.market(), asset.active(), raw.type(), raw.exchangeRateId());
        var broker = persistence.lockBroker(raw.brokerId());
        if (!broker.active()) throw new InvestmentConflictException("inactive broker");
        BigDecimal rate = resolveRate(asset.market(), raw.exchangeRateId(), operationInstant);
        wallet = persistence.lockWallet(wallet.id());
        Posicao previous = persistence.lockPosition(wallet.id(), asset.id()).orElse(null);

        BigDecimal sourceValue = InvestmentNumbers.origin(quantity, unitPrice);
        BigDecimal grossBrl = sourceValue.multiply(rate);
        Posicao resultingPosition;
        BigDecimal totalValue;
        BigDecimal realized = null;
        BigDecimal resultingBalance;
        if (raw.type() == TipoTransacao.BUY) {
            totalValue = InvestmentNumbers.derivedMoney(grossBrl.add(fees), "purchase cost");
            resultingBalance = persistence.debit(wallet.id(), totalValue);
            Posicao base = previous == null ? Posicao.vazia(wallet.id(), asset.id(), operationInstant) : previous;
            resultingPosition = base.comprar(quantity, totalValue, operationInstant);
        } else {
            if (fees.compareTo(grossBrl) > 0) throw new InvestmentConflictException("fees exceed proceeds");
            totalValue = InvestmentNumbers.derivedMoney(grossBrl.subtract(fees), "net proceeds");
            if (previous == null) throw new InvestmentConflictException("position does not exist");
            Posicao.Sale sale;
            try { sale = previous.vender(quantity, totalValue, operationInstant); }
            catch (FinancialStateException exception) { throw new InvestmentConflictException(exception.getMessage()); }
            resultingPosition = sale.position(); realized = sale.realizedResult();
            resultingBalance = persistence.credit(wallet.id(), totalValue);
        }

        if (previous == null) persistence.insertPosition(resultingPosition);
        else persistence.updatePosition(resultingPosition);
        Transacao transaction = new Transacao(UUID.randomUUID(), wallet.id(), userId, asset.id(), broker.id(),
                raw.exchangeRateId(), raw.type(), quantity, asset.currency(), unitPrice, fees, rate,
                totalValue, realized, tradeInstant, operationInstant);
        persistence.insertTransaction(transaction);
        persistence.composeInvestmentSnapshot(wallet.id(), operationInstant.atZone(snapshotZone).toLocalDate(),
                resultingBalance);
        audit.record(new AuditoriaCommand(userId,
                raw.type() == TipoTransacao.BUY ? TipoEvento.COMPRA : TipoEvento.VENDA,
                ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, endpoint, correlationId, operationInstant));
        var transactionView = new TransactionView(transaction, asset.ticker());
        persistence.complete(((NewReservation) reservation).id(), transactionView, sourceValue,
                resultingBalance, resultingPosition);
        return new InvestmentOperationResult(transactionView, sourceValue, resultingBalance,
                PositionView.from(resultingPosition, asset));
    }

    @Override @Transactional(readOnly = true)
    public Page<TransactionView> transactions(UUID userId, int page, int size) {
        validatePage(page, size); return persistence.transactions(wallet(userId).id(), page, size);
    }

    @Override @Transactional(readOnly = true)
    public TransactionView transaction(UUID userId, UUID id) {
        Objects.requireNonNull(id, "transaction id is required");
        TransactionView found = persistence.transaction(id).orElseThrow(InvestmentNotFoundException::new);
        if (!found.transaction().usuarioId().equals(userId)) throw new AccessDeniedException("cross-owner access");
        return found;
    }

    @Override @Transactional(readOnly = true)
    public Page<PositionView> positions(UUID userId, int page, int size) {
        validatePage(page, size); return persistence.openPositions(wallet(userId).id(), page, size);
    }

    @Override @Transactional(readOnly = true)
    public PositionView position(UUID userId, UUID assetId) {
        return persistence.position(wallet(userId).id(), Objects.requireNonNull(assetId))
                .orElseThrow(InvestmentNotFoundException::new);
    }

    private InvestmentPersistencePort.Wallet wallet(UUID userId) {
        return persistence.findPrimaryWallet(userId).orElseThrow(PrimaryWalletMissingException::new);
    }

    private BigDecimal resolveRate(Mercado market, UUID exchangeRateId, Instant operationInstant) {
        if (market == Mercado.B3) return InvestmentNumbers.ONE_8;
        var fx = persistence.findExchangeRate(exchangeRateId)
                .orElseThrow(() -> new InvestmentConflictException("exchange rate not found"));
        if (fx.source() != MoedaCambio.USD || fx.target() != MoedaCambio.BRL)
            throw new InvestmentConflictException("invalid exchange-rate pair");
        if (operationInstant.isAfter(fx.registeredAt().plus(5, ChronoUnit.MINUTES)))
            throw new FxExpiredException();
        return fx.rate().setScale(8);
    }

    private void validateAssetAndFxShape(Mercado market, boolean active, TipoTransacao type, UUID fxId) {
        if (market == Mercado.B3 && fxId != null) throw new IllegalArgumentException("B3 must not have exchange rate");
        if (market == Mercado.US && fxId == null) throw new IllegalArgumentException("US requires exchange rate");
        if (type == TipoTransacao.BUY && !active) throw new InvestmentConflictException("inactive asset");
    }

    private static Instant micros(Instant value) { return value.truncatedTo(ChronoUnit.MICROS); }
    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("invalid pagination");
    }
}
