package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "historico_cambio")
class HistoricoCambioJpaEntity {
	@Id private UUID id;
	@Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "moeda_origem", nullable = false, length = 3, columnDefinition = "char(3)")
	private MoedaCambio moedaOrigem;
	@Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "moeda_destino", nullable = false, length = 3, columnDefinition = "char(3)")
	private MoedaCambio moedaDestino;
	@Column(nullable = false, precision = 18, scale = 8) private BigDecimal taxa;
	@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CambioProvider provider;
	@Column(name = "instante_cotacao", nullable = false) private Instant instanteCotacao;
	@Column(name = "registrado_em", nullable = false) private Instant registradoEm;

	protected HistoricoCambioJpaEntity() {}
	HistoricoCambioJpaEntity(UUID id, MoedaCambio moedaOrigem, MoedaCambio moedaDestino, BigDecimal taxa,
			CambioProvider provider, Instant instanteCotacao, Instant registradoEm) {
		this.id=id; this.moedaOrigem=moedaOrigem; this.moedaDestino=moedaDestino; this.taxa=taxa;
		this.provider=provider; this.instanteCotacao=instanteCotacao; this.registradoEm=registradoEm;
	}
	UUID getId(){return id;} MoedaCambio getMoedaOrigem(){return moedaOrigem;}
	MoedaCambio getMoedaDestino(){return moedaDestino;} BigDecimal getTaxa(){return taxa;}
	CambioProvider getProvider(){return provider;} Instant getInstanteCotacao(){return instanteCotacao;}
	Instant getRegistradoEm(){return registradoEm;}
}
