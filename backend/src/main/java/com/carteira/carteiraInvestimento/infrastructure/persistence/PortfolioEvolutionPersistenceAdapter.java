package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.PortfolioEvolutionPort;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PortfolioEvolutionPersistenceAdapter implements PortfolioEvolutionPort {
    private final JdbcTemplate jdbc;

    public PortfolioEvolutionPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<MaterializedSnapshot> findMaterializedSnapshots(UUID userId) {
        return jdbc.query("""
                SELECT snapshot.data_referencia,snapshot.total_investido_brl,snapshot.lucro_nao_realizado_brl,
                       snapshot.valor_posicoes_brl,snapshot.patrimonio_total_brl
                FROM carteira_snapshots snapshot
                JOIN carteiras carteira ON carteira.id=snapshot.carteira_id
                WHERE carteira.usuario_id=?
                  AND snapshot.valuation_instant IS NOT NULL
                  AND snapshot.valor_posicoes_brl IS NOT NULL
                  AND snapshot.lucro_nao_realizado_brl IS NOT NULL
                  AND snapshot.patrimonio_total_brl IS NOT NULL
                ORDER BY snapshot.data_referencia ASC
                """, (rs, row) -> new MaterializedSnapshot(rs.getObject("data_referencia", java.time.LocalDate.class),
                        rs.getBigDecimal("total_investido_brl"), rs.getBigDecimal("lucro_nao_realizado_brl"),
                        rs.getBigDecimal("valor_posicoes_brl"), rs.getBigDecimal("patrimonio_total_brl")), userId);
    }
}
