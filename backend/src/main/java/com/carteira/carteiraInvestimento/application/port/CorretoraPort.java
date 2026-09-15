package com.carteira.carteiraInvestimento.application.port;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.util.Optional;
import java.util.UUID;
public interface CorretoraPort {
    boolean existsByCnpj(String cnpj);
    Corretora saveAndFlush(Corretora corretora);
    Optional<Corretora> findById(UUID id, CorretoraReadScope scope);
    Optional<Corretora> findByCnpj(String cnpj, CorretoraReadScope scope);
    CorretoraPage list(int page, int size, CorretoraReadScope scope);
}
