package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InvestmentSchemaIT extends PostgreSqlContainerSupport {
    private static final String SCHEMA="investment_schema_test";
    private static final AtomicLong BROKER_CNPJ = new AtomicLong(99887766000155L);
    @BeforeAll static void migrate() {
        Flyway f=Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())
                .schemas(SCHEMA).defaultSchema(SCHEMA).cleanDisabled(false).load();
        f.clean(); f.migrate();
    }

    @Test
    void v9CreatesOnlyTypedFinancialStructuresAndNullableValuationTrio() throws Exception {
        try(Connection c=connection()) {
            assertThat(columnNullable(c,"carteira_snapshots","valor_posicoes_brl")).isEqualTo("YES");
            assertThat(columnNullable(c,"carteira_snapshots","lucro_nao_realizado_brl")).isEqualTo("YES");
            assertThat(columnNullable(c,"carteira_snapshots","patrimonio_total_brl")).isEqualTo("YES");
            assertThat(columnNullable(c,"carteira_snapshots","saldo_caixa_brl")).isEqualTo("NO");
            assertThat(columnNullable(c,"carteira_snapshots","total_investido_brl")).isEqualTo("NO");
            assertThat(count(c,"SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_name IN ('transacoes','posicoes','transacoes_idempotencia')",SCHEMA)).isEqualTo(3);
            assertThat(count(c,"SELECT count(*) FROM information_schema.columns WHERE table_schema=? AND data_type IN ('json','jsonb')",SCHEMA)).isZero();
            assertThat(count(c,"SELECT count(*) FROM information_schema.columns WHERE table_schema=? AND table_name='transacoes' AND column_name='valor_origem'",SCHEMA)).isZero();
            assertThat(count(c,"SELECT count(*) FROM pg_indexes WHERE schemaname=? AND indexdef LIKE '%carteira_id, data_registro DESC, id DESC%'",SCHEMA)).isEqualTo(1);
        }
    }

    @Test
    void constraintsRejectCrossOwnershipCurrencyMismatchInvalidStatesPartialCompletionAndDeletes() throws Exception {
        try(Connection c=connection()) {
            Fixture a=fixture(c,"SCHM4","B3","BRL"); Fixture b=fixture(c,"ABCD5","B3","BRL");
            UUID broker=broker(c), position=position(c,a.wallet(),a.asset()), fx=exchange(c);
            assertThatThrownBy(() -> position(c,a.wallet(),a.asset())).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> transaction(c,a.wallet(),b.user(),a.asset(),broker,"BRL","BUY",null,null))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> transaction(c,a.wallet(),a.user(),a.asset(),broker,"USD","BUY",fx,null))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> transaction(c,a.wallet(),a.user(),a.asset(),broker,"BRL","SELL",null,null))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> update(c,"UPDATE posicoes SET quantidade=0,total_investido_brl=1 WHERE id=?",position))
                    .isInstanceOf(SQLException.class);
            update(c,"UPDATE posicoes SET lucro_realizado_acumulado_brl=-5 WHERE id=?",position);
            update(c,"UPDATE posicoes SET lucro_realizado_acumulado_brl=5 WHERE id=?",position);
            reserve(c,a,"reserved");
            assertThatThrownBy(() -> reserve(c,a,"reserved")).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> partialIdempotency(c,a,position)).isInstanceOf(SQLException.class);
            UUID transaction=transaction(c,a.wallet(),a.user(),a.asset(),broker,"BRL","BUY",null,null);
            assertThatThrownBy(() -> update(c,"DELETE FROM acoes WHERE id=?",a.asset())).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> update(c,"DELETE FROM corretoras WHERE id=?",broker)).isInstanceOf(SQLException.class);
            assertThat(transaction).isNotNull();
        }
    }

    @Test
    void snapshotRejectsPartialUnknownValuationAndAcceptsLegacyKnownAndFullyUnknown() throws Exception {
        try(Connection c=connection()) {
            Fixture f=fixture(c,"SNAP4","B3","BRL");
            snapshot(c,f.wallet(),LocalDate.of(2026,1,1),BigDecimal.ZERO,BigDecimal.ZERO,new BigDecimal("1000.00"));
            snapshot(c,f.wallet(),LocalDate.of(2026,1,2),null,null,null);
            assertThatThrownBy(() -> snapshot(c,f.wallet(),LocalDate.of(2026,1,3),null,null,BigDecimal.ZERO))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void postgresShareLocksProtectAssetAndBrokerLifecycleUntilCommit() throws Exception {
        UUID asset,broker;
        try(Connection setup=connection()){Fixture f=fixture(setup,"LIFE4","B3","BRL");asset=f.asset();broker=broker(setup);}
        assertShareLockBlocks("acoes",asset);
        assertShareLockBlocks("corretoras",broker);
    }

    private void assertShareLockBlocks(String table,UUID id)throws Exception{
        try(Connection holder=connection();Connection contender=connection()){
            holder.setAutoCommit(false);contender.setAutoCommit(false);
            try(var read=holder.prepareStatement("SELECT id FROM "+table+" WHERE id=? FOR SHARE")){read.setObject(1,id);read.executeQuery();}
            try(var timeout=contender.createStatement()){timeout.execute("SET LOCAL lock_timeout='100ms'");}
            assertThatThrownBy(()->update(contender,"UPDATE "+table+" SET ativo=false WHERE id=?",id)).isInstanceOf(SQLException.class);
            contender.rollback();holder.commit();
            update(contender,"UPDATE "+table+" SET ativo=false WHERE id=?",id);contender.commit();
        }
    }

    private Connection connection() throws SQLException { Connection c=POSTGRES.createConnection(""); c.setSchema(SCHEMA); return c; }
    private Fixture fixture(Connection c,String ticker,String market,String currency) throws SQLException {
        UUID user=UUID.randomUUID(),wallet=UUID.randomUUID(),asset=UUID.randomUUID();
        exec(c,"INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,'ROLE_USER',true)",user,"User","schema-"+user+"@test","hash");
        exec(c,"INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,1000)",wallet,user,"Principal");
        exec(c,"INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,?,'ACAO',?,?,true)",asset,ticker,"Asset",market,currency);
        return new Fixture(user,wallet,asset);
    }
    private UUID broker(Connection c) throws SQLException { UUID id=UUID.randomUUID(); exec(c,"INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em) VALUES (?,?, 'Broker','01001000','Rua','Centro','Sao Paulo','SP',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id,String.format("%014d",BROKER_CNPJ.getAndIncrement())); return id; }
    private UUID exchange(Connection c) throws SQLException {UUID id=UUID.randomUUID();exec(c,"INSERT INTO historico_cambio VALUES (?,'USD','BRL',5,'TWELVE_DATA',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id);return id;}
    private UUID position(Connection c,UUID wallet,UUID asset) throws SQLException { UUID id=UUID.randomUUID(); exec(c,"INSERT INTO posicoes VALUES (?,?,?,1,10,10,0,CURRENT_TIMESTAMP)",id,wallet,asset); return id; }
    private UUID transaction(Connection c,UUID wallet,UUID user,UUID asset,UUID broker,String currency,String type,UUID fx,BigDecimal result) throws SQLException {
        UUID id=UUID.randomUUID(); exec(c,"INSERT INTO transacoes VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",id,wallet,user,asset,broker,fx,type,new BigDecimal("1.00000000"),currency,new BigDecimal("10.00000000"),BigDecimal.ZERO.setScale(8),BigDecimal.ONE.setScale(8),new BigDecimal("10.00"),result,Timestamp.from(Instant.now()),Timestamp.from(Instant.now())); return id;
    }
    private void partialIdempotency(Connection c,Fixture f,UUID position) throws SQLException {
        exec(c,"INSERT INTO transacoes_idempotencia(id,carteira_id,usuario_id,idempotency_key,fingerprint,estado,posicao_id) VALUES (?,?,?,?,?,'CONCLUIDA',?)",UUID.randomUUID(),f.wallet(),f.user(),"partial","a".repeat(64),position);
    }
    private void reserve(Connection c,Fixture f,String key)throws SQLException{exec(c,"INSERT INTO transacoes_idempotencia(id,carteira_id,usuario_id,idempotency_key,fingerprint,estado) VALUES (?,?,?,?,?,'RESERVADA')",UUID.randomUUID(),f.wallet(),f.user(),key,"a".repeat(64));}
    private void snapshot(Connection c,UUID wallet,LocalDate date,BigDecimal value,BigDecimal profit,BigDecimal equity) throws SQLException {
        exec(c,"INSERT INTO carteira_snapshots VALUES (?,?,?,?,?,?,?,?)",UUID.randomUUID(),wallet,date,new BigDecimal("1000.00"),value,BigDecimal.ZERO.setScale(2),equity,profit);
    }
    private String columnNullable(Connection c,String table,String column) throws SQLException { try(var p=c.prepareStatement("SELECT is_nullable FROM information_schema.columns WHERE table_schema=? AND table_name=? AND column_name=?")){p.setString(1,SCHEMA);p.setString(2,table);p.setString(3,column);try(var r=p.executeQuery()){r.next();return r.getString(1);}} }
    private long count(Connection c,String sql,String value) throws SQLException { try(var p=c.prepareStatement(sql)){p.setString(1,value);try(var r=p.executeQuery()){r.next();return r.getLong(1);}} }
    private void update(Connection c,String sql,UUID id) throws SQLException { exec(c,sql,id); }
    private void exec(Connection c,String sql,Object... values) throws SQLException { try(var p=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();} }
    private record Fixture(UUID user,UUID wallet,UUID asset) { }
}
