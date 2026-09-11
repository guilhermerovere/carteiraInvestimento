package com.carteira.carteiraInvestimento.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="carteira_snapshots")
class CarteiraSnapshotJpaEntity {
    protected CarteiraSnapshotJpaEntity() { }
    @Id UUID id;
    @Column(name="carteira_id",nullable=false) UUID carteiraId;
    @Column(name="data_referencia",nullable=false) LocalDate dataReferencia;
    @Column(name="saldo_caixa_brl",nullable=false,precision=18,scale=2) BigDecimal saldoCaixaBrl;
    @Column(name="valor_posicoes_brl",precision=18,scale=2) BigDecimal valorPosicoesBrl;
    @Column(name="total_investido_brl",nullable=false,precision=18,scale=2) BigDecimal totalInvestidoBrl;
    @Column(name="patrimonio_total_brl",precision=18,scale=2) BigDecimal patrimonioTotalBrl;
    @Column(name="lucro_nao_realizado_brl",precision=18,scale=2) BigDecimal lucroNaoRealizadoBrl;
}
