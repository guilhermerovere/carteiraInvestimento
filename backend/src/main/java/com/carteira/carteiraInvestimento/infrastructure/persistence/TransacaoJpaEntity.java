package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

@Entity @Immutable @Table(name="transacoes")
class TransacaoJpaEntity {
    protected TransacaoJpaEntity() { }
    @Id UUID id;
    @Column(name="carteira_id",nullable=false) UUID carteiraId;
    @Column(name="usuario_id",nullable=false) UUID usuarioId;
    @Column(name="acao_id",nullable=false) UUID acaoId;
    @Column(name="corretora_id",nullable=false) UUID corretoraId;
    @Column(name="exchange_rate_id") UUID exchangeRateId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=4) TipoTransacao tipo;
    @Column(nullable=false,precision=18,scale=8) BigDecimal quantidade;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=3) Moeda moeda;
    @Column(name="preco_unitario",nullable=false,precision=18,scale=8) BigDecimal precoUnitario;
    @Column(nullable=false,precision=18,scale=8) BigDecimal taxas;
    @Column(name="taxa_cambio_brl",nullable=false,precision=18,scale=8) BigDecimal taxaCambioBrl;
    @Column(name="valor_total_brl",nullable=false,precision=18,scale=2) BigDecimal valorTotalBrl;
    @Column(name="resultado_realizado_brl",precision=18,scale=2) BigDecimal resultadoRealizadoBrl;
    @Column(name="data_negociacao",nullable=false) Instant dataNegociacao;
    @Column(name="data_registro",nullable=false) Instant dataRegistro;
}
