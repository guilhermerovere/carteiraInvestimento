package com.carteira.carteiraInvestimento.infrastructure.persistence;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity,UUID>{ Optional<UsuarioJpaEntity> findByEmail(String email); }
