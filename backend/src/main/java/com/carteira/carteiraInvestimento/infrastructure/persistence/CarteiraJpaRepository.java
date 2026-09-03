package com.carteira.carteiraInvestimento.infrastructure.persistence;
import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
interface CarteiraJpaRepository extends JpaRepository<CarteiraJpaEntity,UUID>{}
