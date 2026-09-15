package com.carteira.carteiraInvestimento.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;

@Entity
@Table(name = "corretoras")
public class CorretoraJpaEntity {
    @Id private UUID id;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(nullable = false, length = 14, columnDefinition = "char(14)") private String cnpj;
    @Column(name = "razao_social", nullable = false, length = 255) private String razaoSocial;
    @Column(name = "nome_fantasia", length = 255) private String nomeFantasia;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(nullable = false, length = 8, columnDefinition = "char(8)") private String cep;
    @Column(nullable = false, length = 255) private String logradouro;
    @Column(nullable = false, length = 160) private String bairro;
    @Column(nullable = false, length = 160) private String cidade;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(nullable = false, length = 2, columnDefinition = "char(2)") private String uf;
    @Column(length = 20) private String numero;
    @Column(length = 160) private String complemento;
    @Column(nullable = false) private boolean ativo;
    @Enumerated(EnumType.STRING) @Column(name = "logo_provider", length = 16) private LogoProvider logoProvider;
    @Column(name = "logo_reference", length = 253) private String logoReference;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false) private Instant atualizadoEm;

    protected CorretoraJpaEntity() { }
    public CorretoraJpaEntity(UUID id, String cnpj, String razaoSocial, String nomeFantasia, String cep,
            String logradouro, String bairro, String cidade, String uf, String numero, String complemento,
            boolean ativo, LogoProvider logoProvider, String logoReference, Instant criadoEm, Instant atualizadoEm) {
        this.id=id; this.cnpj=cnpj; this.razaoSocial=razaoSocial; this.nomeFantasia=nomeFantasia; this.cep=cep;
        this.logradouro=logradouro; this.bairro=bairro; this.cidade=cidade; this.uf=uf; this.numero=numero;
        this.complemento=complemento; this.ativo=ativo; this.logoProvider=logoProvider;
        this.logoReference=logoReference; this.criadoEm=criadoEm; this.atualizadoEm=atualizadoEm;
    }
    public UUID getId(){return id;} public String getCnpj(){return cnpj;} public String getRazaoSocial(){return razaoSocial;}
    public String getNomeFantasia(){return nomeFantasia;} public String getCep(){return cep;} public String getLogradouro(){return logradouro;}
    public String getBairro(){return bairro;} public String getCidade(){return cidade;} public String getUf(){return uf;}
    public String getNumero(){return numero;} public String getComplemento(){return complemento;} public boolean isAtivo(){return ativo;}
    public LogoProvider getLogoProvider(){return logoProvider;} public String getLogoReference(){return logoReference;}
    public Instant getCriadoEm(){return criadoEm;} public Instant getAtualizadoEm(){return atualizadoEm;}
}
