package com.carteira.carteiraInvestimento.domain.broker;

import static org.assertj.core.api.Assertions.*;
import java.time.*;
import org.junit.jupiter.api.Test;

class BrokerDomainTest {
    private final Clock first=Clock.fixed(Instant.parse("2026-09-10T10:00:00Z"),ZoneOffset.UTC);
    @Test void canonicalizesCnpjManualFieldsAndUsesOneCreationInstant(){
        Corretora b=Corretora.nova("19.131.243/0001-97"," Empresa ","  ","01310-100"," Paulista "," Bela Vista "," São Paulo ","sp"," 10 "," ",first);
        assertThat(b.cnpj()).isEqualTo("19131243000197"); assertThat(b.nomeFantasia()).isNull(); assertThat(b.numero()).isEqualTo("10");
        assertThat(b.complemento()).isNull(); assertThat(b.uf()).isEqualTo("SP"); assertThat(b.criadoEm()).isEqualTo(b.atualizadoEm());
    }
    @Test void validatesChecksumAndLimits(){
        assertThatThrownBy(()->CnpjCanonicalizer.canonicalize("11.111.111/1111-11")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->Corretora.normalizeNumero("x".repeat(21))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->Corretora.normalizeComplemento("x".repeat(161))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void noOpsPreserveIdentityAndTimestampWhileRealChangesUseClock(){
        Corretora b=broker(); Clock later=Clock.fixed(Instant.parse("2026-09-10T11:00:00Z"),ZoneOffset.UTC);
        assertThat(b.editar(" 10 ",null,later)).isSameAs(b); assertThat(b.definirAtivo(true,later)).isSameAs(b);
        assertThat(b.editar("", " Sala 2 ",later).atualizadoEm()).isEqualTo(later.instant());
        assertThat(b.definirAtivo(false,later).atualizadoEm()).isEqualTo(later.instant());
    }
    private Corretora broker(){return Corretora.nova("19131243000197","Empresa",null,"01310100","Paulista","Bela Vista","São Paulo","SP","10",null,first);}
}
