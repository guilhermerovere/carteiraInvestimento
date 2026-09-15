package com.carteira.carteiraInvestimento.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="posicoes")
class PosicaoJpaEntity {
    protected PosicaoJpaEntity() { }
    @Id UUID id;
    @Column(name="carteira_id",nullable=false) UUID carteiraId;
    @Column(name="acao_id",nullable=false) UUID acaoId;
    @Column(nullable=false,precision=18,scale=8) BigDecimal quantidade;
    @Column(name="preco_medio_brl",nullable=false,precision=18,scale=8) BigDecimal precoMedioBrl;
    @Column(name="total_investido_brl",nullable=false,precision=18,scale=2) BigDecimal totalInvestidoBrl;
    @Column(name="lucro_realizado_acumulado_brl",nullable=false,precision=18,scale=2) BigDecimal lucroRealizadoAcumuladoBrl;
    @Column(name="ultima_atualizacao",nullable=false) Instant ultimaAtualizacao;
}
