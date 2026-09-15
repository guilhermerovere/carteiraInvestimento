package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.service.AccountClosureConflictException;
import com.carteira.carteiraInvestimento.application.service.AccountSettingsApplicationService;
import com.carteira.carteiraInvestimento.application.service.AccountSettingsUseCase;
import com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException;
import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.application.service.LoginService;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class AccountSettingsIT extends PostgreSqlContainerSupport {
    @Autowired AccountSettingsUseCase settings;
    @Autowired CashMovementUseCase cash;
    @Autowired LoginService login;
    @Autowired PasswordHasher passwords;
    @Autowired JdbcTemplate jdbc;
    private final List<UUID> users = new ArrayList<>();
    private final List<UUID> assets = new ArrayList<>();

    @AfterEach
    void clean() {
        for (UUID user : users) {
            jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id=?)", user);
            jdbc.update("DELETE FROM movimentacoes_caixa WHERE usuario_id=?", user);
            jdbc.update("DELETE FROM carteira_snapshots WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id=?)", user);
            jdbc.update("DELETE FROM posicoes WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id=?)", user);
            jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id=?", user);
            jdbc.update("DELETE FROM carteiras WHERE usuario_id=?", user);
            jdbc.update("DELETE FROM usuarios WHERE id=?", user);
        }
        for (UUID asset : assets) jdbc.update("DELETE FROM acoes WHERE id=?", asset);
    }

    @Test
    void closureKeepsFinancialHistoryAuditAndHistoricalZeroPositionThenPreventsLogin() {
        Fixture fixture = fixture("0.00");
        UUID asset = asset("CLSE3");
        jdbc.update("INSERT INTO movimentacoes_caixa(id,carteira_id,usuario_id,tipo,valor_brl,descricao,data_hora) VALUES (?,?,?,'DEPOSITO',100.00,'histórico',CURRENT_TIMESTAMP)", UUID.randomUUID(), fixture.wallet(), fixture.user());
        jdbc.update("INSERT INTO movimentacoes_caixa(id,carteira_id,usuario_id,tipo,valor_brl,descricao,data_hora) VALUES (?,?,?,'SAQUE',100.00,'histórico',CURRENT_TIMESTAMP)", UUID.randomUUID(), fixture.wallet(), fixture.user());
        jdbc.update("INSERT INTO posicoes(id,carteira_id,acao_id,quantidade,preco_medio_brl,total_investido_brl,lucro_realizado_acumulado_brl,ultima_atualizacao) VALUES (?,?,?,0,0,0,25.00,CURRENT_TIMESTAMP)", UUID.randomUUID(), fixture.wallet(), asset);

        settings.closeAccount(fixture.user(), "Atual123!", AccountSettingsApplicationService.CLOSURE_CONFIRMATION,
                UUID.randomUUID(), "/api/v1/account/closure");

        assertThat(jdbc.queryForObject("SELECT ativo FROM usuarios WHERE id=?", Boolean.class, fixture.user())).isFalse();
        assertThat(jdbc.queryForObject("SELECT nome FROM usuarios WHERE id=?", String.class, fixture.user())).isEqualTo("Conta encerrada");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM movimentacoes_caixa WHERE usuario_id=?", Long.class, fixture.user())).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM posicoes WHERE carteira_id=?", Long.class, fixture.wallet())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM logs_auditoria WHERE usuario_id=? AND tipo_evento='CONTA_ENCERRADA'", Long.class, fixture.user())).isEqualTo(1);
        assertThatThrownBy(() -> login.login(fixture.email(), "Atual123!", UUID.randomUUID()))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void concurrentDepositAndClosureSerializeOnUserAndNeverOperateAfterClosure() throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            Fixture fixture = fixture("0.00");
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var closing = executor.submit(() -> outcome(() -> settings.closeAccount(fixture.user(), "Atual123!",
                        AccountSettingsApplicationService.CLOSURE_CONFIRMATION, UUID.randomUUID(), "/closure"), ready, start));
                var depositing = executor.submit(() -> outcome(() -> cash.execute(fixture.user(), TipoMovimentacaoCaixa.DEPOSITO,
                        new BigDecimal("10.00"), null, "race-" + UUID.randomUUID(), UUID.randomUUID(), "/deposit"), ready, start));
                ready.await(); start.countDown();
                Object closeResult = closing.get(); Object depositResult = depositing.get();
                boolean active = jdbc.queryForObject("SELECT ativo FROM usuarios WHERE id=?", Boolean.class, fixture.user());
                BigDecimal balance = jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?", BigDecimal.class, fixture.wallet());
                if (active) {
                    assertThat(closeResult).isInstanceOf(AccountClosureConflictException.class);
                    assertThat(depositResult).isNotInstanceOf(Throwable.class);
                    assertThat(balance).isEqualByComparingTo("10.00");
                } else {
                    assertThat(closeResult).isEqualTo("ok");
                    assertThat(depositResult).isInstanceOf(RuntimeException.class);
                    assertThat(((RuntimeException) depositResult).getMessage()).contains("active principal unavailable");
                    assertThat(balance).isEqualByComparingTo("0.00");
                }
            }
        }
    }

    private Object outcome(ThrowingAction action, CountDownLatch ready, CountDownLatch start) {
        try { ready.countDown(); start.await(); action.run(); return "ok"; }
        catch (Throwable failure) { return failure; }
    }

    private Fixture fixture(String balance) {
        UUID user = UUID.randomUUID(); UUID wallet = UUID.randomUUID(); String email = "settings-it-" + user + "@example.test";
        users.add(user);
        jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?, 'ROLE_USER',true)", user, "Settings IT", email, passwords.hash("Atual123!"));
        jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)", wallet, user, "Principal", new BigDecimal(balance));
        return new Fixture(user, wallet, email);
    }

    private UUID asset(String ticker) {
        UUID id = UUID.randomUUID(); assets.add(id);
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,?,'ACAO','B3','BRL',true)", id, ticker, "Closure asset");
        return id;
    }

    private record Fixture(UUID user, UUID wallet, String email) { }
    @FunctionalInterface private interface ThrowingAction { void run() throws Exception; }
}
