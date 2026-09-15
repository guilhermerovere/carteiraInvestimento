package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionHistoryPlaywrightIT extends PostgreSqlContainerSupport {
    private static final String QA_EMAIL_PREFIX = "transaction-qa-";

    @Autowired JdbcTemplate jdbc;
    @LocalServerPort int backendPort;

    private final UUID assetId = UUID.randomUUID();
    private final UUID brokerId = UUID.randomUUID();
    private String brokerCnpj;

    @BeforeEach
    void seedCatalog() {
        brokerCnpj = validCnpj();
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,'E2E ledger asset','ACAO','B3','BRL',true)",
                assetId, "QAEX1");
        jdbc.update("""
                INSERT INTO corretoras(id,cnpj,razao_social,nome_fantasia,cep,logradouro,bairro,cidade,uf,
                    numero,complemento,ativo,criado_em,atualizado_em)
                VALUES (?,?, 'QA Ledger Broker S.A.','QA Ledger Broker','01001000','Rua A','Centro',
                    'Sao Paulo','SP','100',NULL,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, brokerId, brokerCnpj);
    }

    @AfterEach
    void removeIsolatedFixtures() {
        List<UUID> users = jdbc.query("SELECT id FROM usuarios WHERE email LIKE ?",
                (rs, row) -> rs.getObject(1, UUID.class), QA_EMAIL_PREFIX + "%");
        for (UUID userId : users) {
            List<UUID> wallets = jdbc.query("SELECT id FROM carteiras WHERE usuario_id=?",
                    (rs, row) -> rs.getObject(1, UUID.class), userId);
            for (UUID walletId : wallets) {
                jdbc.update("DELETE FROM transacoes_idempotencia WHERE carteira_id=?", walletId);
                jdbc.update("DELETE FROM transacoes WHERE carteira_id=?", walletId);
                jdbc.update("DELETE FROM posicoes WHERE carteira_id=?", walletId);
                jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia WHERE carteira_id=?", walletId);
                jdbc.update("DELETE FROM movimentacoes_caixa WHERE carteira_id=?", walletId);
                jdbc.update("DELETE FROM carteira_snapshots WHERE carteira_id=?", walletId);
                jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id=?", userId);
                jdbc.update("DELETE FROM carteiras WHERE id=?", walletId);
            }
            jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id=?", userId);
            jdbc.update("DELETE FROM usuarios WHERE id=?", userId);
        }
        jdbc.update("DELETE FROM acoes WHERE id=?", assetId);
        jdbc.update("DELETE FROM corretoras WHERE id=?", brokerId);
    }

    @Test
    void browserBuyIsCommittedAndReturnedByTheRealPaginatedLedgerEvenWhenQuoteFails() throws Exception {
        Path frontend = Path.of("..", "frontend").toAbsolutePath().normalize();
        assertThat(Files.isDirectory(frontend.resolve("node_modules")))
                .as("frontend dependencies must be installed before the real Playwright regression")
                .isTrue();
        int frontendPort = freePort();
        String appOrigin = "http://127.0.0.1:" + frontendPort;
        Path log = Path.of("target", "transaction-history-playwright.log").toAbsolutePath();
        Files.createDirectories(log.getParent());
        ProcessBuilder command = new ProcessBuilder("cmd.exe", "/c", "npm.cmd", "run", "test:e2e", "--",
                "--config", "playwright.real.config.ts", "e2e/transaction-history.real.spec.ts")
                .directory(frontend.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile());
        command.environment().put("PLAYWRIGHT_APP_ORIGIN", appOrigin);
        command.environment().put("BACKEND_API_URL", "http://127.0.0.1:" + backendPort);
        command.environment().put("APP_ORIGIN", appOrigin);

        Process playwright = command.start();
        boolean finished;
        try {
            finished = playwright.waitFor(4, TimeUnit.MINUTES);
            if (!finished) playwright.destroyForcibly();
            String output = Files.exists(log) ? Files.readString(log) : "Playwright produced no output.";
            assertThat(finished).as(output).isTrue();
            assertThat(playwright.exitValue()).as(output).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM transacoes WHERE acao_id=? AND tipo='BUY'",
                    Long.class, assetId)).isEqualTo(1L);
        } finally {
            if (playwright.isAlive()) {
                playwright.destroy();
                if (!playwright.waitFor(5, TimeUnit.SECONDS)) playwright.destroyForcibly();
            }
        }
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static String validCnpj() {
        String base = String.format("%012d", Math.floorMod(System.nanoTime(), 1_000_000_000_000L));
        int first = checkDigit(base, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        String withFirst = base + first;
        int second = checkDigit(withFirst, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        return withFirst + second;
    }

    private static int checkDigit(String value, int[] weights) {
        int sum = 0;
        for (int index = 0; index < weights.length; index++) {
            sum += (value.charAt(index) - '0') * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
