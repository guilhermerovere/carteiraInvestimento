package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.port.PortfolioEvolutionPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PortfolioEvolutionApplicationServiceTest {
    @Test
    void exposesOnlyPersistedMaterializedSnapshotsInPortOrderForPrincipal() {
        UUID user = UUID.randomUUID();
        PortfolioEvolutionPort persistence = mock(PortfolioEvolutionPort.class);
        when(persistence.findMaterializedSnapshots(user)).thenReturn(List.of(
                new PortfolioEvolutionPort.MaterializedSnapshot(LocalDate.of(2026, 9, 10), new BigDecimal("100.00"), new BigDecimal("2.00"), new BigDecimal("102.00"), new BigDecimal("112.00")),
                new PortfolioEvolutionPort.MaterializedSnapshot(LocalDate.of(2026, 9, 12), new BigDecimal("125.00"), new BigDecimal("-3.00"), new BigDecimal("122.00"), new BigDecimal("132.00"))));

        var points = new PortfolioEvolutionApplicationService(persistence).read(user);

        assertThat(points).extracting(PortfolioEvolutionPoint::dataReferencia)
                .containsExactly(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12));
        assertThat(points.get(0).totalInvestidoBrl()).isEqualByComparingTo("100.00");
        assertThat(points.get(1).resultadoNaoRealizadoBrl()).isEqualByComparingTo("-3.00");
        assertThat(points.get(0).valorPosicoesBrl()).isEqualByComparingTo("102.00");
        assertThat(points.get(1).patrimonioTotalBrl()).isEqualByComparingTo("132.00");
        verify(persistence).findMaterializedSnapshots(user);
    }

    @Test
    void keepsEmptyHistoryEmpty() {
        PortfolioEvolutionPort persistence = mock(PortfolioEvolutionPort.class);
        UUID user = UUID.randomUUID();
        when(persistence.findMaterializedSnapshots(user)).thenReturn(List.of());
        assertThat(new PortfolioEvolutionApplicationService(persistence).read(user)).isEmpty();
    }
}
