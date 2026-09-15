package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class InvestmentTransactionApiIT extends PostgreSqlContainerSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtEncoder jwtEncoder;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM transacoes_idempotencia");
        jdbc.update("DELETE FROM transacoes");
        jdbc.update("DELETE FROM carteira_snapshots");
        jdbc.update("DELETE FROM posicoes");
        jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-api-%')");
        jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-api-%')");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE 'investment-api-%'");
        jdbc.update("DELETE FROM historico_cambio WHERE id IN (SELECT id FROM historico_cambio WHERE provider='TWELVE_DATA')");
        jdbc.update("DELETE FROM acoes WHERE ticker IN ('TSTX4','AAPL')");
        jdbc.update("DELETE FROM corretoras WHERE cnpj='12345678000199'");
    }
    @AfterEach void cleanAfter(){clean();}

    @Test
    void b3BuyReplaySellListsOwnershipSnapshotsAndAuditAreCoherent() throws Exception {
        Fixture f = fixture("ROLE_USER", new BigDecimal("5000.00"));
        UUID asset = asset("TSTX4", "B3", "BRL");
        UUID broker = broker();
        long initialQuotes=jdbc.queryForObject("SELECT count(*) FROM historico_cotacoes",Long.class);
        long initialFx=jdbc.queryForObject("SELECT count(*) FROM historico_cambio",Long.class);
        String buyBody = body(asset, broker, "BUY", "10", "100", "1", null);
        String original = mvc.perform(post("/api/v1/carteira/transacoes")
                .header(HttpHeaders.AUTHORIZATION, bearer(f)).header("Idempotency-Key", "buy-main")
                .contentType(MediaType.APPLICATION_JSON).content(buyBody))
                .andExpect(status().isCreated()).andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.transacao.tipo").value("BUY"))
                .andExpect(jsonPath("$.transacao.moeda").value("BRL"))
                .andExpect(jsonPath("$.transacao.taxaCambioBrl").value(1.0))
                .andExpect(jsonPath("$.transacao.valorTotalBrl").value(1001.0))
                .andExpect(jsonPath("$.transacao.resultadoRealizadoBrl").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.valorOrigem").value(1000.0))
                .andExpect(jsonPath("$.saldoCaixaBrl").value(3999.0))
                .andExpect(jsonPath("$.posicao.precoMedioBrl").value(100.1))
                .andExpect(jsonPath("$.transacao.carteiraId").doesNotExist())
                .andExpect(jsonPath("$.posicao.lucroNaoRealizadoBrl").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String replay = mvc.perform(post("/api/v1/carteira/transacoes")
                .header(HttpHeaders.AUTHORIZATION, bearer(f)).header("Idempotency-Key", "buy-main")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(asset, broker, "BUY", "10.0000000000", "100.0", "1.00000000", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertThat(replay).isEqualTo(original);
        assertThat(jdbc.queryForObject("SELECT estado_versao FROM carteiras WHERE id=?",Long.class,f.walletId()))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT estado_versao FROM carteiras WHERE id=?", Long.class, f.walletId()))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT valor_posicoes_brl FROM carteira_snapshots WHERE carteira_id=?",
                BigDecimal.class, f.walletId())).isNull();
        assertThat(jdbc.queryForObject("SELECT total_investido_brl FROM carteira_snapshots WHERE carteira_id=?",
                BigDecimal.class, f.walletId())).isEqualByComparingTo("1001.00");
        UUID transactionId = jdbc.queryForObject("SELECT id FROM transacoes", UUID.class);

        mvc.perform(get("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(f)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].id").value(jdbc.queryForObject("SELECT id FROM transacoes", UUID.class).toString()))
                .andExpect(jsonPath("$.items[0].ticker").value("TSTX4"))
                .andExpect(jsonPath("$.items[0].nome").value("Test Asset"))
                .andExpect(jsonPath("$.items[0].corretoraNome").value("Broker Test"))
                .andExpect(jsonPath("$.items[0].logoProvider").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.items[0].logoReference").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.items[0].exchangeRateId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.items[0].resultadoRealizadoBrl").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/api/v1/carteira/transacoes/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, bearer(f))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()));
        Fixture other = fixture("ROLE_USER", BigDecimal.ZERO);
        UUID deniedCorrelation = UUID.randomUUID();
        mvc.perform(get("/api/v1/carteira/transacoes/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, bearer(other)).header("X-Correlation-ID",deniedCorrelation))
                .andExpect(status().isForbidden()).andExpect(header().string("X-Correlation-ID",deniedCorrelation.toString()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(containsString("TSTX4"))))
                .andExpect(content().string(not(containsString("buy-main"))))
                .andExpect(content().string(not(containsString("fingerprint"))));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM logs_auditoria WHERE usuario_id=? AND correlation_id=? AND tipo_evento='ACESSO_NEGADO'",
                Long.class,other.userId(),deniedCorrelation)).isEqualTo(1);

        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(f))
                .header("Idempotency-Key", "sell-main").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset, broker, "SELL", "10", "120", "1", null)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.transacao.resultadoRealizadoBrl").value(198.0))
                .andExpect(jsonPath("$.saldoCaixaBrl").value(5198.0))
                .andExpect(jsonPath("$.posicao.quantidade").value(0));
        mvc.perform(get("/api/v1/carteira/posicoes").header(HttpHeaders.AUTHORIZATION, bearer(f)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get("/api/v1/carteira/posicoes/" + asset).header(HttpHeaders.AUTHORIZATION, bearer(f)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(0))
                .andExpect(jsonPath("$.lucroRealizadoAcumuladoBrl").value(198.0));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM transacoes", Long.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT estado_versao FROM carteiras WHERE id=?", Long.class, f.walletId()))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM logs_auditoria WHERE usuario_id=? AND tipo_evento IN ('COMPRA','VENDA')",
                Long.class, f.userId())).isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM transacoes t JOIN logs_auditoria l
                  ON l.usuario_id=t.usuario_id
                 AND l.tipo_evento=CASE WHEN t.tipo='BUY' THEN 'COMPRA' ELSE 'VENDA' END
                 AND l.data_hora=t.data_registro
                WHERE t.carteira_id=?
                """,Long.class,f.walletId())).isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM posicoes p JOIN transacoes t
                  ON t.carteira_id=p.carteira_id AND t.acao_id=p.acao_id
                 AND t.tipo='SELL' AND t.data_registro=p.ultima_atualizacao
                WHERE p.carteira_id=?
                """,Long.class,f.walletId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT valor_posicoes_brl FROM carteira_snapshots WHERE carteira_id=?",
                BigDecimal.class, f.walletId())).isEqualByComparingTo("0.00");
        assertThat(jdbc.queryForObject("SELECT patrimonio_total_brl FROM carteira_snapshots WHERE carteira_id=?",
                BigDecimal.class, f.walletId())).isEqualByComparingTo("5198.00");
        assertThat(jdbc.queryForObject("SELECT estado_versao FROM carteiras WHERE id=?",Long.class,f.walletId()))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM historico_cotacoes",Long.class)).isEqualTo(initialQuotes);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM historico_cambio",Long.class)).isEqualTo(initialFx);
    }

    @Test
    void persistedPositionKeepsItsUuidAndLifetimeProfitAcrossTotalSaleAndRepurchase() throws Exception {
        Fixture f=fixture("ROLE_USER",new BigDecimal("1000.00"));UUID asset=asset("TSTX4","B3","BRL"),broker=broker();
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(f))
                .header("Idempotency-Key","cycle-buy").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","2","10","0",null))).andExpect(status().isCreated());
        UUID positionId=jdbc.queryForObject("SELECT id FROM posicoes WHERE carteira_id=? AND acao_id=?",UUID.class,f.walletId(),asset);
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(f))
                .header("Idempotency-Key","cycle-sell").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"SELL","2","15","0",null))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.posicao.quantidade").value(0))
                .andExpect(jsonPath("$.posicao.lucroRealizadoAcumuladoBrl").value(10.0));
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(f))
                .header("Idempotency-Key","cycle-rebuy").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","3","20","0",null))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.posicao.id").value(positionId.toString()))
                .andExpect(jsonPath("$.posicao.quantidade").value(3.0))
                .andExpect(jsonPath("$.posicao.totalInvestidoBrl").value(60.0))
                .andExpect(jsonPath("$.posicao.precoMedioBrl").value(20.0))
                .andExpect(jsonPath("$.posicao.lucroRealizadoAcumuladoBrl").value(10.0));
        assertThat(jdbc.queryForObject("SELECT id FROM posicoes WHERE carteira_id=? AND acao_id=?",UUID.class,f.walletId(),asset))
                .isEqualTo(positionId);
    }

    @Test
    void usUsesOnlyLocalFxAndReplaySurvivesLaterExpiration() throws Exception {
        Fixture f = fixture("ROLE_USER", new BigDecimal("1000.00"));
        UUID asset = asset("AAPL", "US", "USD"); UUID broker = broker(); UUID fx = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO historico_cambio(id,moeda_origem,moeda_destino,taxa,provider,instante_cotacao,registrado_em)
                VALUES (?,'USD','BRL',5.00000000,'TWELVE_DATA',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, fx);
        long quoteRows=jdbc.queryForObject("SELECT count(*) FROM historico_cotacoes",Long.class);
        long fxRows=jdbc.queryForObject("SELECT count(*) FROM historico_cambio",Long.class);
        String body = body(asset, broker, "BUY", "1", "100", "0", fx);
        String first = mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(f))
                .header("Idempotency-Key", "us-local").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.transacao.moeda").value("USD"))
                .andExpect(jsonPath("$.transacao.valorTotalBrl").value(500.0)).andReturn().getResponse().getContentAsString();
        jdbc.update("UPDATE historico_cambio SET registrado_em=CURRENT_TIMESTAMP-INTERVAL '1 day' WHERE id=?", fx);
        String replay = mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(f))
                .header("Idempotency-Key", "us-local").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertThat(replay).isEqualTo(first);
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(f))
                .header("Idempotency-Key", "us-expired").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("FX_EXPIRED"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM transacoes_idempotencia WHERE idempotency_key='us-expired'",
                Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM historico_cotacoes",Long.class)).isEqualTo(quoteRows);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM historico_cambio",Long.class)).isEqualTo(fxRows);
    }

    @Test
    void endpointsEnforceSecurityStrictInputsPaginationAndFxShape() throws Exception {
        Fixture user = fixture("ROLE_USER", BigDecimal.TEN); Fixture admin = fixture("ROLE_ADMIN", BigDecimal.TEN);
        UUID asset = asset("TSTX4", "B3", "BRL"); UUID broker = broker();
        for(String path:List.of("/api/v1/carteira/transacoes","/api/v1/carteira/transacoes/"+UUID.randomUUID(),
                "/api/v1/carteira/posicoes","/api/v1/carteira/posicoes/"+UUID.randomUUID())) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized()).andExpect(header().exists("X-Correlation-ID"));
            mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION,bearer(admin))).andExpect(status().isForbidden())
                    .andExpect(header().exists("X-Correlation-ID"));
        }
        mvc.perform(post("/api/v1/carteira/transacoes").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key","anonymous").content(body(asset,broker,"BUY","1","1","0",null)))
                .andExpect(status().isUnauthorized()).andExpect(header().exists("X-Correlation-ID"));
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(admin))
                .header("Idempotency-Key","admin").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","1","1","0",null)))
                .andExpect(status().isForbidden()).andExpect(header().exists("X-Correlation-ID"));
        for (String query : List.of("?page=-1", "?size=0", "?size=101", "?tipo=BUY"))
            mvc.perform(get("/api/v1/carteira/transacoes" + query).header(HttpHeaders.AUTHORIZATION, bearer(user)))
                    .andExpect(status().isBadRequest());
        String base = body(asset, broker, "BUY", "1", "1", "0", null);
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(user))
                .header("Idempotency-Key", "unknown").contentType(MediaType.APPLICATION_JSON)
                .content(base.substring(0, base.length()-1) + ",\"usuarioId\":\"" + user.userId() + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(user))
                .header("Idempotency-Key", "scale").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset, broker, "BUY", "1.123456789", "1", "0", null)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION, bearer(user))
                .header("Idempotency-Key", "b3-fx").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset, broker, "BUY", "1", "1", "0", UUID.randomUUID())))
                .andExpect(status().isBadRequest());
        for(String path:List.of("/api/v1/carteira/transacoes/"+UUID.randomUUID(),
                "/api/v1/carteira/posicoes/"+UUID.randomUUID()))
            mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION,bearer(user))).andExpect(status().isNotFound())
                    .andExpect(header().exists("X-Correlation-ID"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(content().string(not(containsString("fingerprint"))));
    }

    @Test
    void validationPrecedenceAndRollbackCoverInputLifecycleBalancePositionOversellAndFx() throws Exception {
        Fixture user=fixture("ROLE_USER",new BigDecimal("100.00"));UUID asset=asset("TSTX4","B3","BRL"),broker=broker();
        for(String invalid:List.of(
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","0","1","0",null),
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","-1","1","0",null),
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","1","0","0",null),
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","1","-1","0",null),
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","1","1","-1",null),
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","1.123456789","1","0",null),
                body(UUID.randomUUID(),UUID.randomUUID(),"BUY","10000000000","1","0",null)))
            mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                    .header("Idempotency-Key","invalid-"+UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest());
        assertNoEffects(user,"100.00",0,0);

        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","missing-asset").contentType(MediaType.APPLICATION_JSON)
                .content(body(UUID.randomUUID(),broker,"BUY","1","1","0",null))).andExpect(status().isNotFound());
        jdbc.update("UPDATE acoes SET ativo=false WHERE id=?",asset);
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","inactive-asset").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","1","1","0",null))).andExpect(status().isConflict());
        jdbc.update("UPDATE acoes SET ativo=true WHERE id=?",asset);jdbc.update("UPDATE corretoras SET ativo=false WHERE id=?",broker);
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","inactive-broker").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","1","1","0",null))).andExpect(status().isConflict());
        jdbc.update("UPDATE corretoras SET ativo=true WHERE id=?",broker);
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","insufficient").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","1","101","0",null))).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","missing-position").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"SELL","1","1","0",null))).andExpect(status().isConflict());
        assertNoEffects(user,"100.00",0,0);

        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","seed").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"BUY","1","1","0",null))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","oversell").contentType(MediaType.APPLICATION_JSON)
                .content(body(asset,broker,"SELL","2","1","0",null))).andExpect(status().isConflict());
        assertNoEffects(user,"99.00",1,1);

        UUID us=asset("AAPL","US","USD");
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","missing-fx").contentType(MediaType.APPLICATION_JSON)
                .content(body(us,broker,"BUY","1","1","0",null))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/carteira/transacoes").header(HttpHeaders.AUTHORIZATION,bearer(user))
                .header("Idempotency-Key","unknown-fx").contentType(MediaType.APPLICATION_JSON)
                .content(body(us,broker,"BUY","1","1","0",UUID.randomUUID()))).andExpect(status().isConflict());
        assertNoEffects(user,"99.00",1,1);
    }

    private void assertNoEffects(Fixture user,String balance,long transactions,long positions){
        assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?",BigDecimal.class,user.walletId()))
                .isEqualByComparingTo(balance);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM transacoes WHERE carteira_id=?",Long.class,user.walletId()))
                .isEqualTo(transactions);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM posicoes WHERE carteira_id=?",Long.class,user.walletId()))
                .isEqualTo(positions);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM transacoes_idempotencia WHERE carteira_id=?",Long.class,user.walletId()))
                .isEqualTo(transactions);
    }

    private Fixture fixture(String role, BigDecimal balance) {
        UUID user = UUID.randomUUID(), wallet = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)", user,
                "Investment User", "investment-api-" + user + "@example.test", "hash", role);
        jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)", wallet, user,
                "Carteira Principal", balance);
        return new Fixture(user, wallet, role);
    }

    private UUID asset(String ticker, String market, String currency) {
        UUID id=UUID.randomUUID();
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,?,'ACAO',?,?,true)",
                id,ticker,"Test Asset",market,currency); return id;
    }
    private UUID broker() {
        UUID id=UUID.randomUUID();
        jdbc.update("""
                INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em)
                VALUES (?,'12345678000199','Broker Test','01001000','Rua A','Centro','Sao Paulo','SP',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """, id); return id;
    }
    private String body(UUID asset, UUID broker, String type, String quantity, String price, String fees, UUID fx) {
        return "{\"ativoId\":\""+asset+"\",\"corretoraId\":\""+broker+"\",\"tipo\":\""+type
                +"\",\"quantidade\":"+quantity+",\"precoUnitario\":"+price+",\"taxas\":"+fees
                +",\"dataNegociacao\":\"2026-09-11T12:00:00.123456789-03:00\""
                +(fx==null?"":",\"exchangeRateId\":\""+fx+"\"")+"}";
    }
    private String bearer(Fixture f) {
        Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().subject(f.userId().toString()).claim("role",f.role()).issuedAt(now)
                .expiresAt(now.plusSeconds(3600)).id(UUID.randomUUID().toString())
                .issuer("carteira-investimento-backend").audience(List.of("carteira-investimento-api")).build();
        return "Bearer "+jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
    }
    private record Fixture(UUID userId, UUID walletId, String role) { }
}
