package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "acoes")
public class AtivoJpaEntity {
	@Id
	private UUID id;
	@Column(nullable = false, length = 6)
	private String ticker;
	@Column(nullable = false, length = 160)
	private String nome;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private TipoAtivo tipo;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private Mercado mercado;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 3)
	private Moeda moeda;
	@Column(nullable = false)
	private boolean ativo;
	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;
	@Column(name = "atualizado_em", nullable = false)
	private Instant atualizadoEm;

	protected AtivoJpaEntity() {
	}

	public AtivoJpaEntity(UUID id, String ticker, String nome, TipoAtivo tipo, Mercado mercado, Moeda moeda,
			boolean ativo, Instant criadoEm, Instant atualizadoEm) {
		this.id = id;
		this.ticker = ticker;
		this.nome = nome;
		this.tipo = tipo;
		this.mercado = mercado;
		this.moeda = moeda;
		this.ativo = ativo;
		this.criadoEm = criadoEm;
		this.atualizadoEm = atualizadoEm;
	}

	public UUID getId() { return id; }
	public String getTicker() { return ticker; }
	public String getNome() { return nome; }
	public TipoAtivo getTipo() { return tipo; }
	public Mercado getMercado() { return mercado; }
	public Moeda getMoeda() { return moeda; }
	public boolean isAtivo() { return ativo; }
	public Instant getCriadoEm() { return criadoEm; }
	public Instant getAtualizadoEm() { return atualizadoEm; }
}
