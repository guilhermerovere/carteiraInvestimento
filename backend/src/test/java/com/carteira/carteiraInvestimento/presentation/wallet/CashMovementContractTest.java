package com.carteira.carteiraInvestimento.presentation.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CashMovementContractTest {
	@Test
	void rejectsUnknownCashFieldsEvenWhenTheGlobalMapperIgnoresUnknownProperties() throws Exception {
		JsonMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
		CashMovementRequest valid = mapper.readValue("{\"valor\":10,\"descricao\":\"origem\"}", CashMovementRequest.class);
		assertThat(valid.valor()).isEqualByComparingTo(BigDecimal.TEN);
		assertThatThrownBy(() -> mapper.readValue("{\"valor\":10,\"carteiraId\":\"private\"}",
				CashMovementRequest.class)).hasRootCauseInstanceOf(IllegalArgumentException.class);
	}
}
