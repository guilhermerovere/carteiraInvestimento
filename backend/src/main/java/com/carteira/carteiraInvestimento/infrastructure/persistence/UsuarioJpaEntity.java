package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.domain.identity.Role;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "usuarios")
public class UsuarioJpaEntity {
 @Id private UUID id; @Column(nullable=false) private String nome; @Column(nullable=false) private String email; @Column(name="senha_hash",nullable=false) private String senhaHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role; @Column(nullable=false) private boolean ativo; @Column(name="criado_em",nullable=false) private Instant criadoEm; @Column(name="atualizado_em",nullable=false) private Instant atualizadoEm;
 protected UsuarioJpaEntity(){} public UsuarioJpaEntity(UUID id,String nome,String email,String senhaHash,Role role,boolean ativo,Instant criadoEm,Instant atualizadoEm){this.id=id;this.nome=nome;this.email=email;this.senhaHash=senhaHash;this.role=role;this.ativo=ativo;this.criadoEm=criadoEm;this.atualizadoEm=atualizadoEm;}
 public UUID getId(){return id;} public String getNome(){return nome;} public String getEmail(){return email;} public String getSenhaHash(){return senhaHash;} public Role getRole(){return role;} public boolean isAtivo(){return ativo;} public Instant getCriadoEm(){return criadoEm;} public Instant getAtualizadoEm(){return atualizadoEm;}
}
