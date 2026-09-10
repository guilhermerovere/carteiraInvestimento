package com.carteira.carteiraInvestimento.infrastructure.persistence;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
interface CorretoraJpaRepository extends JpaRepository<CorretoraJpaEntity, UUID>, JpaSpecificationExecutor<CorretoraJpaEntity> {
    boolean existsByCnpj(String cnpj);
    Optional<CorretoraJpaEntity> findByCnpj(String cnpj);
    Optional<CorretoraJpaEntity> findByCnpjAndAtivoTrue(String cnpj);
    Optional<CorretoraJpaEntity> findByIdAndAtivoTrue(UUID id);
}
