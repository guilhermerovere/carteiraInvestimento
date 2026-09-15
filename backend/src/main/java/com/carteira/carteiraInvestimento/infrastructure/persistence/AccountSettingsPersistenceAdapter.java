package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.AccountSettingsPersistencePort;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountSettingsPersistenceAdapter implements AccountSettingsPersistencePort {
    private final JdbcTemplate jdbc;

    public AccountSettingsPersistenceAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Usuario> lockUser(UUID userId) {
        return jdbc.query("""
                SELECT id,nome,email,senha_hash,role,ativo,criado_em,atualizado_em
                FROM usuarios WHERE id=? FOR UPDATE
                """, (rs, row) -> new Usuario(rs.getObject("id", UUID.class), rs.getString("nome"),
                        rs.getString("email"), rs.getString("senha_hash"), Role.valueOf(rs.getString("role")),
                        rs.getBoolean("ativo"), rs.getTimestamp("criado_em").toInstant(),
                        rs.getTimestamp("atualizado_em").toInstant()), userId).stream().findFirst();
    }

    @Override
    public Optional<WalletState> lockWallet(UUID userId) {
        return jdbc.query("SELECT id,saldo_caixa_brl FROM carteiras WHERE usuario_id=? FOR UPDATE",
                (rs, row) -> new WalletState(rs.getObject("id", UUID.class),
                        rs.getBigDecimal("saldo_caixa_brl")), userId).stream().findFirst();
    }

    @Override
    public boolean hasOpenPositions(UUID walletId) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM posicoes WHERE carteira_id=? AND quantidade>0)",
                Boolean.class, walletId);
        return Boolean.TRUE.equals(exists);
    }
}
