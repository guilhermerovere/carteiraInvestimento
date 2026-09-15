package com.carteira.carteiraInvestimento.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface HistoricoCotacaoJpaRepository extends JpaRepository<HistoricoCotacaoJpaEntity, UUID> {
	Page<HistoricoCotacaoJpaEntity> findByAtivoId(UUID ativoId, Pageable pageable);
}
