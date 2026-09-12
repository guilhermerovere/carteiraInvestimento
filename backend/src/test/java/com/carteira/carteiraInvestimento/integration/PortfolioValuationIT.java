package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort;
import com.carteira.carteiraInvestimento.application.service.CambioUseCase;
import com.carteira.carteiraInvestimento.application.service.CambioUnavailableException;
import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.application.service.InvestmentTransactionCommand;
import com.carteira.carteiraInvestimento.application.service.InvestmentUseCase;
import com.carteira.carteiraInvestimento.application.service.MarketQuoteUseCase;
import com.carteira.carteiraInvestimento.application.service.PortfolioValuationConflictException;
import com.carteira.carteiraInvestimento.application.service.PortfolioValuationUseCase;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PortfolioValuationIT.Fakes.class)
class PortfolioValuationIT extends PostgreSqlContainerSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired PortfolioValuationUseCase valuation;
    @Autowired CashMovementUseCase cash;
    @Autowired InvestmentUseCase investments;
    @Autowired PortfolioValuationPort valuationPersistence;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired FakeQuotes quotes;
    @Autowired FakeExchange exchange;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach void cleanAndReset() { clean(); quotes.reset(); exchange.reset(); }
    @AfterEach void cleanAfter() { clean(); }

    @Test
    void getB3UsesRepeatableReadBoundaryReturnsExactContractAndDoesNotPersistOrVersion() throws Exception {
        Fixture fixture = user("ROLE_USER", "100.00");
        UUID open = asset("PVAL4", "B3", "BRL", false);
        UUID closed = asset("CLOS4", "B3", "BRL", true);
        position(fixture.wallet(), open, "2.00000000", "40.00", "20.00000000", "3.00");
        position(fixture.wallet(), closed, "0.00000000", "0.00", "0.00000000", "7.00");
        quotes.values.put(open, quote(open, Moeda.BRL, "25.0000", QuoteProvider.BRAPI));

        String body = mvc.perform(get("/api/v1/carteira/resumo").header(HttpHeaders.AUTHORIZATION, bearer(fixture)))
                .andExpect(status().isOk()).andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.saldoCaixaBrl").value(100.0))
                .andExpect(jsonPath("$.valorPosicoesBrl").value(50.0))
                .andExpect(jsonPath("$.lucroNaoRealizadoBrl").value(10.0))
                .andExpect(jsonPath("$.lucroRealizadoAcumuladoBrl").value(10.0))
                .andExpect(jsonPath("$.patrimonioTotalBrl").value(150.0))
                .andExpect(jsonPath("$.rentabilidadeNaoRealizadaPercentual").value(25.0))
                .andExpect(jsonPath("$.cambioAtual").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.posicoes.length()").value(1))
                .andExpect(jsonPath("$.posicoes[0].ativoId").value(open.toString()))
                .andReturn().getResponse().getContentAsString();
        JsonNode root = json.readTree(body);
        assertThat(fieldNames(root)).containsExactlyInAnyOrder("valuationInstant", "saldoCaixaBrl",
                "totalInvestidoBrl", "valorPosicoesBrl", "lucroNaoRealizadoBrl",
                "lucroRealizadoAcumuladoBrl", "patrimonioTotalBrl", "rentabilidadeNaoRealizadaPercentual",
                "cambioAtual", "posicoes");
        assertThat(fieldNames(root.get("posicoes").get(0))).hasSize(14).containsExactlyInAnyOrder("ativoId",
                "ticker", "mercado", "moeda", "quantidade", "precoMedioBrl", "totalInvestidoBrl",
                "cotacaoAtual", "providerCotacao", "instanteCotacao", "valorAtualOrigem", "valorAtualBrl",
                "lucroNaoRealizadoBrl", "rentabilidadePercentual");
        assertThat(root.get("valuationInstant").asText()).matches(".*\\.\\d{6}Z");
        assertThat(count("carteira_snapshots", fixture.wallet())).isZero();
        assertThat(version(fixture.wallet())).isZero();
        assertThat(quotes.transactionObserved).isFalse();
        assertThat(exchange.calls.get()).isZero();
    }

    @Test
    void postEmptyUsesNoProvidersMaterializesZerosAndDoesNotAuditOrIncrementVersion() throws Exception {
        Fixture fixture = user("ROLE_USER", "42.00");
        long auditBefore = jdbc.queryForObject("SELECT count(*) FROM logs_auditoria", Long.class);
        mvc.perform(post("/api/v1/carteira/resumo/atualizar").header(HttpHeaders.AUTHORIZATION, bearer(fixture)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalInvestidoBrl").value(0.0))
                .andExpect(jsonPath("$.valorPosicoesBrl").value(0.0))
                .andExpect(jsonPath("$.lucroNaoRealizadoBrl").value(0.0))
                .andExpect(jsonPath("$.patrimonioTotalBrl").value(42.0))
                .andExpect(jsonPath("$.rentabilidadeNaoRealizadaPercentual").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.posicoes").isEmpty());
        Map<String,Object> snapshot = jdbc.queryForMap("SELECT * FROM carteira_snapshots WHERE carteira_id=?", fixture.wallet());
        assertThat(snapshot.get("valuation_instant")).isNotNull();
        assertThat((BigDecimal)snapshot.get("valor_posicoes_brl")).isEqualByComparingTo("0.00");
        assertThat(version(fixture.wallet())).isZero();
        assertThat(quotes.calls.get()).isZero(); assertThat(exchange.calls.get()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM logs_auditoria", Long.class)).isEqualTo(auditBefore);
    }

    @Test
    void severalUsPositionsShareOneFxAndProviderFailuresReturnComplete502WithoutSnapshot() throws Exception {
        Fixture fixture = user("ROLE_USER", "10.00");
        UUID first = asset("AAPL", "US", "USD", true), second = asset("MSFT", "US", "USD", true);
        position(fixture.wallet(), first, "1.00000000", "10.00", "10.00000000", "0.00");
        position(fixture.wallet(), second, "2.00000000", "20.00", "10.00000000", "0.00");
        quotes.values.put(first, quote(first, Moeda.USD, "3.0000", QuoteProvider.ALPHA_VANTAGE));
        quotes.values.put(second, quote(second, Moeda.USD, "4.0000", QuoteProvider.TWELVE_DATA));
        mvc.perform(get("/api/v1/carteira/resumo").header(HttpHeaders.AUTHORIZATION, bearer(fixture)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cambioAtual.taxaCambioBrl").value(5.0))
                .andExpect(jsonPath("$.posicoes.length()").value(2));
        assertThat(exchange.calls.get()).isEqualTo(1);
        quotes.failure = new QuoteIntegrationException("unavailable");
        mvc.perform(post("/api/v1/carteira/resumo/atualizar").header(HttpHeaders.AUTHORIZATION, bearer(fixture)))
                .andExpect(status().isBadGateway()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().exists("X-Correlation-ID"));
        assertThat(count("carteira_snapshots", fixture.wallet())).isZero();
        quotes.failure = null;
        exchange.failure = new CambioUnavailableException("unavailable", new IllegalStateException());
        mvc.perform(post("/api/v1/carteira/resumo/atualizar").header(HttpHeaders.AUTHORIZATION, bearer(fixture)))
                .andExpect(status().isBadGateway()).andExpect(header().exists("X-Correlation-ID"));
        assertThat(count("carteira_snapshots", fixture.wallet())).isZero();
    }

    @Test
    void strictSecurityAndOpenApiExposeOnlyTheTwoValuationOperations() throws Exception {
        Fixture user = user("ROLE_USER", "0.00"); Fixture admin = user("ROLE_ADMIN", "0.00");
        for (String path : List.of("/api/v1/carteira/resumo", "/api/v1/carteira/resumo/atualizar")) {
            mvc.perform(path.endsWith("atualizar") ? post(path) : get(path)).andExpect(status().isUnauthorized());
            mvc.perform((path.endsWith("atualizar") ? post(path) : get(path)).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                    .andExpect(status().isForbidden()).andExpect(header().exists("X-Correlation-ID"));
        }
        mvc.perform(get("/api/v1/carteira/resumo?usuarioId=" + user.user()).header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/carteira/resumo/atualizar").header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isBadRequest());
        String docs = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode paths = json.readTree(docs).get("paths");
        assertThat(fieldNames(paths).stream().filter(name -> name.contains("/carteira/resumo")).toList())
                .containsExactlyInAnyOrder("/api/v1/carteira/resumo", "/api/v1/carteira/resumo/atualizar");
        assertThat(paths.get("/api/v1/carteira/resumo").has("get")).isTrue();
        assertThat(paths.get("/api/v1/carteira/resumo/atualizar").has("post")).isTrue();
    }

    @Test
    void slowProviderAllowsMutationThenRetriesCompletelyWithFreshLocalState() throws Exception {
        Fixture fixture = user("ROLE_USER", "100.00"); UUID asset = asset("RETR4", "B3", "BRL", true);
        position(fixture.wallet(), asset, "1.00000000", "10.00", "10.00000000", "0.00");
        quotes.values.put(asset, quote(asset, Moeda.BRL, "12.0000", QuoteProvider.BRAPI));
        quotes.blockFirst = true;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var future = executor.submit(() -> valuation.summarize(fixture.user()));
            assertThat(quotes.entered.await(5, TimeUnit.SECONDS)).isTrue();
            jdbc.update("UPDATE carteiras SET saldo_caixa_brl=saldo_caixa_brl+5,estado_versao=estado_versao+1 WHERE id=?", fixture.wallet());
            quotes.release.countDown();
            var result = future.get(5, TimeUnit.SECONDS);
            assertThat(result.cashBalanceBrl()).isEqualByComparingTo("105.00");
            assertThat(quotes.calls.get()).isEqualTo(2);
            assertThat(result.valuationInstant()).isAfter(quotes.firstValuationFloor);
        }
        assertThat(quotes.transactionObserved).isFalse();
    }

    @Test
    void twoStateLossesReturn409AndTwoConcurrentPostsKeepTheNewerInstant() throws Exception {
        Fixture conflict = user("ROLE_USER", "1.00"); UUID conflictAsset = asset("CNFL4", "B3", "BRL", true);
        position(conflict.wallet(), conflictAsset, "1.00000000", "1.00", "1.00000000", "0.00");
        quotes.values.put(conflictAsset, quote(conflictAsset, Moeda.BRL, "1.0000", QuoteProvider.BRAPI));
        quotes.afterQuote = () -> jdbc.update("UPDATE carteiras SET estado_versao=estado_versao+1 WHERE id=?", conflict.wallet());
        assertThatThrownBy(() -> valuation.refresh(conflict.user())).isInstanceOf(PortfolioValuationConflictException.class);
        assertThat(quotes.calls.get()).isEqualTo(2); assertThat(count("carteira_snapshots", conflict.wallet())).isZero();
        quotes.calls.set(0);
        mvc.perform(get("/api/v1/carteira/resumo").header(HttpHeaders.AUTHORIZATION, bearer(conflict)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().exists("X-Correlation-ID"));

        quotes.reset(); Fixture ordered = user("ROLE_USER", "5.00"); UUID orderedAsset = asset("ORDR4", "B3", "BRL", true);
        position(ordered.wallet(), orderedAsset, "1.00000000", "2.00", "2.00000000", "0.00");
        quotes.values.put(orderedAsset, quote(orderedAsset, Moeda.BRL, "3.0000", QuoteProvider.BRAPI));
        quotes.blockFirst = true;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var older = executor.submit(() -> valuation.refresh(ordered.user()));
            assertThat(quotes.entered.await(5, TimeUnit.SECONDS)).isTrue();
            var newer = executor.submit(() -> valuation.refresh(ordered.user()));
            var newerResult = newer.get(5, TimeUnit.SECONDS);
            quotes.release.countDown();
            var olderResult = older.get(5, TimeUnit.SECONDS);
            Instant persisted = jdbc.queryForObject("SELECT valuation_instant FROM carteira_snapshots WHERE carteira_id=?",
                    Instant.class, ordered.wallet());
            assertThat(persisted).isEqualTo(newerResult.valuationInstant());
            assertThat(persisted).isAfter(olderResult.valuationInstant());
        }
    }

    @Test
    void snapshotUpsertAcceptsNullOrStrictlyGreaterInstantAndRejectsLowerOrMicrosecondTie() {
        Fixture fixture = user("ROLE_USER", "10.00");
        var totals = new PortfolioValuationPort.MaterializedTotals(new BigDecimal("1.00"),
                new BigDecimal("2.00"), new BigDecimal("1.00"), new BigDecimal("12.00"));
        Instant base = Instant.parse("2026-09-12T12:00:00.123456Z");
        var transaction = new TransactionTemplate(transactionManager);
        java.time.LocalDate date = java.time.LocalDate.of(2026, 9, 12);
        java.util.function.BiConsumer<Instant,BigDecimal> write = (instant,value) -> transaction.executeWithoutResult(status ->
                valuationPersistence.validateAndOptionallyMaterialize(fixture.wallet(), 0, date, instant,
                        new BigDecimal("10.00"), new PortfolioValuationPort.MaterializedTotals(
                                new BigDecimal("1.00"), value, value.subtract(BigDecimal.ONE),
                                value.add(new BigDecimal("10.00"))), true));
        write.accept(base, new BigDecimal("2.00"));
        write.accept(base.minusNanos(1_000), new BigDecimal("3.00"));
        write.accept(base, new BigDecimal("4.00"));
        assertThat(jdbc.queryForObject("SELECT valor_posicoes_brl FROM carteira_snapshots WHERE carteira_id=?",
                BigDecimal.class, fixture.wallet())).isEqualByComparingTo("2.00");
        write.accept(base.plusNanos(1_000), new BigDecimal("5.00"));
        assertThat(jdbc.queryForObject("SELECT valor_posicoes_brl FROM carteira_snapshots WHERE carteira_id=?",
                BigDecimal.class, fixture.wallet())).isEqualByComparingTo("5.00");
    }

    @Test
    void slowValuationRacesWithDepositWithdrawalBuyAndSellWithoutHoldingFinancialLock() throws Exception {
        Fixture fixture = user("ROLE_USER", "100.00");
        UUID held = asset("MUTR4", "B3", "BRL", true), traded = asset("ZBUY4", "B3", "BRL", true);
        position(fixture.wallet(), held, "1.00000000", "10.00", "10.00000000", "0.00");
        quotes.values.put(held, quote(held, Moeda.BRL, "11.0000", QuoteProvider.BRAPI));
        quotes.values.put(traded, quote(traded, Moeda.BRL, "10.0000", QuoteProvider.BRAPI));
        UUID broker = broker();

        race(fixture, () -> cash.execute(fixture.user(), TipoMovimentacaoCaixa.DEPOSITO,
                new BigDecimal("5.00"), null, "valuation-deposit", UUID.randomUUID(), "/deposito"));
        assertThat(version(fixture.wallet())).isEqualTo(1);
        race(fixture, () -> cash.execute(fixture.user(), TipoMovimentacaoCaixa.SAQUE,
                new BigDecimal("3.00"), null, "valuation-withdraw", UUID.randomUUID(), "/saque"));
        assertThat(version(fixture.wallet())).isEqualTo(2);
        race(fixture, () -> investments.execute(fixture.user(), command(traded, broker, TipoTransacao.BUY),
                "valuation-buy", UUID.randomUUID(), "/transacoes"));
        assertThat(version(fixture.wallet())).isEqualTo(3);
        race(fixture, () -> investments.execute(fixture.user(), command(traded, broker, TipoTransacao.SELL),
                "valuation-sell", UUID.randomUUID(), "/transacoes"));
        assertThat(version(fixture.wallet())).isEqualTo(4);
        assertThat(quotes.transactionObserved).isFalse();
    }

    @Test
    void twoGetsTwoPostsAndMixedGetPostRemainCoherentAndVersionNeutral() throws Exception {
        Fixture fixture = user("ROLE_USER", "9.00");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var getA = executor.submit(() -> valuation.summarize(fixture.user()));
            var getB = executor.submit(() -> valuation.summarize(fixture.user()));
            assertThat(getA.get().totalEquityBrl()).isEqualByComparingTo("9.00");
            assertThat(getB.get().totalEquityBrl()).isEqualByComparingTo("9.00");
            assertThat(count("carteira_snapshots", fixture.wallet())).isZero();
            var postA = executor.submit(() -> valuation.refresh(fixture.user()));
            var postB = executor.submit(() -> valuation.refresh(fixture.user()));
            postA.get(); postB.get();
            var mixedGet = executor.submit(() -> valuation.summarize(fixture.user()));
            var mixedPost = executor.submit(() -> valuation.refresh(fixture.user()));
            mixedGet.get(); mixedPost.get();
        }
        assertThat(count("carteira_snapshots", fixture.wallet())).isEqualTo(1);
        assertThat(version(fixture.wallet())).isZero();
        assertThat(quotes.calls.get()).isZero(); assertThat(exchange.calls.get()).isZero();
    }

    private Fixture user(String role, String balance) {
        UUID user = UUID.randomUUID(), wallet = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)", user,
                "Valuation", "valuation-it-" + user + "@test", "hash", role);
        jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)", wallet, user,
                "Principal", new BigDecimal(balance));
        return new Fixture(user, wallet, role);
    }
    private UUID asset(String ticker, String market, String currency, boolean active) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,?,'ACAO',?,?,?)",
                id, ticker, "Valuation Asset", market, currency, active); return id;
    }
    private UUID broker() {
        UUID id = UUID.randomUUID(); String cnpj = String.format("%014d", Math.abs(id.getLeastSignificantBits()) % 100000000000000L);
        jdbc.update("""
                INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em)
                VALUES (?,?,'Valuation Broker','01001000','Rua','Centro','Sao Paulo','SP',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, id, cnpj); return id;
    }
    private InvestmentTransactionCommand command(UUID asset, UUID broker, TipoTransacao type) {
        return new InvestmentTransactionCommand(asset, broker, type, BigDecimal.ONE, BigDecimal.TEN,
                BigDecimal.ZERO, OffsetDateTime.parse("2026-09-12T12:00:00-03:00"), null);
    }
    private void race(Fixture fixture, Runnable mutation) throws Exception {
        quotes.calls.set(0); quotes.blockFirst = true; quotes.entered = new CountDownLatch(1);
        quotes.release = new CountDownLatch(1); quotes.afterQuote = null;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var running = executor.submit(() -> valuation.summarize(fixture.user()));
            assertThat(quotes.entered.await(5, TimeUnit.SECONDS)).isTrue();
            mutation.run();
            quotes.release.countDown();
            running.get(5, TimeUnit.SECONDS);
        }
        quotes.blockFirst = false;
    }
    private void position(UUID wallet, UUID asset, String quantity, String invested, String average, String realized) {
        jdbc.update("INSERT INTO posicoes VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)", UUID.randomUUID(), wallet, asset,
                new BigDecimal(quantity), new BigDecimal(average), new BigDecimal(invested), new BigDecimal(realized));
    }
    private Cotacao quote(UUID asset, Moeda currency, String price, QuoteProvider provider) {
        Instant instant = Instant.parse("2026-09-12T10:00:00Z");
        return new Cotacao(UUID.randomUUID(), asset, new BigDecimal(price), currency, instant, provider, instant);
    }
    private String bearer(Fixture fixture) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().subject(fixture.user().toString()).claim("role", fixture.role())
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).id(UUID.randomUUID().toString())
                .issuer("carteira-investimento-backend").audience(List.of("carteira-investimento-api")).build();
        return "Bearer " + jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    private List<String> fieldNames(JsonNode node) { var names = new ArrayList<String>(); node.fieldNames().forEachRemaining(names::add); return names; }
    private long count(String table, UUID wallet) { return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE carteira_id=?", Long.class, wallet); }
    private long version(UUID wallet) { return jdbc.queryForObject("SELECT estado_versao FROM carteiras WHERE id=?", Long.class, wallet); }
    private void clean() {
        jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%'))");
        jdbc.update("DELETE FROM transacoes_idempotencia WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%'))");
        jdbc.update("DELETE FROM transacoes WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%'))");
        jdbc.update("DELETE FROM movimentacoes_caixa WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%'))");
        jdbc.update("DELETE FROM carteira_snapshots WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%'))");
        jdbc.update("DELETE FROM posicoes WHERE carteira_id IN (SELECT id FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%'))");
        jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%')");
        jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'valuation-it-%')");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE 'valuation-it-%'");
        jdbc.update("DELETE FROM acoes WHERE ticker IN ('PVAL4','CLOS4','AAPL','MSFT','RETR4','CNFL4','ORDR4','MUTR4','ZBUY4')");
        jdbc.update("DELETE FROM corretoras WHERE razao_social='Valuation Broker'");
    }
    private record Fixture(UUID user, UUID wallet, String role) { }

    @TestConfiguration(proxyBeanMethods = false)
    static class Fakes {
        @Bean @Primary FakeQuotes fakeQuotes() { return new FakeQuotes(); }
        @Bean @Primary FakeExchange fakeExchange() { return new FakeExchange(); }
    }

    static class FakeQuotes implements MarketQuoteUseCase {
        final Map<UUID,Cotacao> values = new java.util.concurrent.ConcurrentHashMap<>();
        final AtomicInteger calls = new AtomicInteger();
        volatile RuntimeException failure; volatile boolean transactionObserved; volatile boolean blockFirst;
        volatile CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        volatile Runnable afterQuote; volatile Instant firstValuationFloor = Instant.EPOCH;
        void reset() { values.clear(); calls.set(0); failure=null; transactionObserved=false; blockFirst=false;
            entered=new CountDownLatch(1); release=new CountDownLatch(1); afterQuote=null; firstValuationFloor=Instant.now(); }
        @Override public Cotacao cotacaoAtualParaCustodia(UUID id, boolean open) {
            transactionObserved |= TransactionSynchronizationManager.isActualTransactionActive();
            int invocation = calls.incrementAndGet();
            if (blockFirst && invocation == 1) { entered.countDown(); try { release.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException exception) { Thread.currentThread().interrupt(); } }
            if (afterQuote != null) afterQuote.run();
            if (failure != null) throw failure;
            return values.get(id);
        }
        @Override public Cotacao cotacaoAtual(UUID id) { throw new UnsupportedOperationException(); }
        @Override public Cotacao atualizarCotacao(UUID id) { throw new UnsupportedOperationException(); }
        @Override public HistoricoCotacaoPage historico(UUID id, int page, int size, boolean admin) { throw new UnsupportedOperationException(); }
    }
    static class FakeExchange implements CambioUseCase {
        final AtomicInteger calls = new AtomicInteger();
        volatile RuntimeException failure;
        void reset() { calls.set(0); failure = null; }
        @Override public ObservacaoCambio obterUsdBrl() { calls.incrementAndGet(); Instant now=Instant.parse("2026-09-12T10:00:00Z");
            if (failure != null) throw failure;
            return new ObservacaoCambio(UUID.randomUUID(), MoedaCambio.USD, MoedaCambio.BRL,
                    new BigDecimal("5.00000000"), CambioProvider.ALPHA_VANTAGE, now, now); }
    }
}
