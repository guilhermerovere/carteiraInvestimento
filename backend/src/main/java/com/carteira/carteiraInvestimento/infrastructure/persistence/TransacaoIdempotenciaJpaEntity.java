package com.carteira.carteiraInvestimento.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="transacoes_idempotencia")
class TransacaoIdempotenciaJpaEntity {
    protected TransacaoIdempotenciaJpaEntity() { }
    @Id UUID id;
    @Column(name="carteira_id",nullable=false) UUID carteiraId;
    @Column(name="usuario_id",nullable=false) UUID usuarioId;
    @Column(name="idempotency_key",nullable=false,length=128) String idempotencyKey;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(nullable=false,length=64,columnDefinition="char(64)") String fingerprint;
    @Column(nullable=false,length=12) String estado;
    @Column(name="transacao_id") UUID transacaoId;
    @Column(name="ticker_resultante",length=6) String tickerResultante;
    @Column(name="valor_origem",precision=36,scale=16) BigDecimal valorOrigem;
    @Column(name="saldo_caixa_brl_resultante",precision=18,scale=2) BigDecimal saldoCaixaBrlResultante;
    @Column(name="posicao_id") UUID posicaoId;
    @Column(name="posicao_quantidade_resultante",precision=18,scale=8) BigDecimal posicaoQuantidadeResultante;
    @Column(name="posicao_preco_medio_brl_resultante",precision=18,scale=8) BigDecimal posicaoPrecoMedioBrlResultante;
    @Column(name="posicao_total_investido_brl_resultante",precision=18,scale=2) BigDecimal posicaoTotalInvestidoBrlResultante;
    @Column(name="posicao_lucro_realizado_acumulado_brl_resultante",precision=18,scale=2) BigDecimal posicaoLucroRealizadoAcumuladoBrlResultante;
    @Column(name="posicao_ultima_atualizacao_resultante") Instant posicaoUltimaAtualizacaoResultante;
    @Column(name="reservada_em",nullable=false) Instant reservadaEm;
    @Column(name="concluida_em") Instant concluidaEm;
}
