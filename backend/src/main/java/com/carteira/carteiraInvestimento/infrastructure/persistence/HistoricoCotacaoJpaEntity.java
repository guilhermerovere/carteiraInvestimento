package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "historico_cotacoes")
public class HistoricoCotacaoJpaEntity {
	@Id private UUID id;
	@Column(name = "ativo_id", nullable = false) private UUID ativoId;
	@Column(nullable = false, precision = 15, scale = 4) private BigDecimal preco;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private Moeda moeda;
	@Column(name = "instante_cotacao", nullable = false) private Instant instanteCotacao;
	@Enumerated(EnumType.STRING) @Column(name = "origem_provider", nullable = false, length = 20)
	private QuoteProvider provider;
	@Column(name = "recebido_em", nullable = false) private Instant recebidoEm;

	protected HistoricoCotacaoJpaEntity() {}

	public HistoricoCotacaoJpaEntity(UUID id, UUID ativoId, BigDecimal preco, Moeda moeda,
			Instant instanteCotacao, QuoteProvider provider, Instant recebidoEm) {
		this.id = id; this.ativoId = ativoId; this.preco = preco; this.moeda = moeda;
		this.instanteCotacao = instanteCotacao; this.provider = provider; this.recebidoEm = recebidoEm;
	}

	public UUID getId() { return id; }
	public UUID getAtivoId() { return ativoId; }
	public BigDecimal getPreco() { return preco; }
	public Moeda getMoeda() { return moeda; }
	public Instant getInstanteCotacao() { return instanteCotacao; }
	public QuoteProvider getProvider() { return provider; }
	public Instant getRecebidoEm() { return recebidoEm; }
}
