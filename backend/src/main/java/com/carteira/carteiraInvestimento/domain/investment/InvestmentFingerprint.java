package com.carteira.carteiraInvestimento.domain.investment;

import com.carteira.carteiraInvestimento.domain.shared.CanonicalFingerprint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class InvestmentFingerprint {
    private InvestmentFingerprint() { }

    public static String create(UUID assetId, UUID brokerId, TipoTransacao type, BigDecimal quantity,
            BigDecimal unitPrice, BigDecimal fees, Instant tradeInstant, UUID exchangeRateId) {
        return CanonicalFingerprint.sha256(
                CanonicalFingerprint.field("version", "1"),
                CanonicalFingerprint.field("assetId", assetId),
                CanonicalFingerprint.field("brokerId", brokerId),
                CanonicalFingerprint.field("type", type.name()),
                CanonicalFingerprint.field("quantity", InvestmentNumbers.canonical(quantity)),
                CanonicalFingerprint.field("unitPrice", InvestmentNumbers.canonical(unitPrice)),
                CanonicalFingerprint.field("fees", InvestmentNumbers.canonical(fees)),
                CanonicalFingerprint.field("tradeInstant", tradeInstant),
                CanonicalFingerprint.field("fxPresent", exchangeRateId == null ? "0" : "1"),
                CanonicalFingerprint.field("exchangeRateId", exchangeRateId));
    }
}
