package com.carteira.carteiraInvestimento.presentation.investment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InvestmentTransactionContractTest {
    @Test
    void rejectsUnknownAndPrivateFieldsEvenWithPermissiveGlobalMapper() throws Exception {
        JsonMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .findAndAddModules().build();
        String base = "{\"ativoId\":\"" + UUID.randomUUID() + "\",\"corretoraId\":\"" + UUID.randomUUID()
                + "\",\"tipo\":\"BUY\",\"quantidade\":10,\"precoUnitario\":20,\"taxas\":0,"
                + "\"dataNegociacao\":\"2026-09-11T12:00:00-03:00\"";
        assertThat(mapper.readValue(base+"}",InvestmentTransactionRequest.class).command().tradeDate().toInstant())
                .hasToString("2026-09-11T15:00:00Z");
        assertThatThrownBy(() -> mapper.readValue(base + ",\"carteiraId\":\"" + UUID.randomUUID() + "\"}",
                InvestmentTransactionRequest.class)).hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.readValue(base + ",\"moeda\":\"BRL\"}",
                InvestmentTransactionRequest.class)).hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.readValue(base.replace("2026-09-11T12:00:00-03:00","2026-09-11T12:00:00")+"}",
                InvestmentTransactionRequest.class));
        assertThatThrownBy(() -> mapper.readValue(base.replace("\"BUY\"","\"PURCHASE\"")+"}",
                InvestmentTransactionRequest.class));
    }
}
