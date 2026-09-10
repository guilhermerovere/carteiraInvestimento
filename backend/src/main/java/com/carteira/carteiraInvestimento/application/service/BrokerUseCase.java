package com.carteira.carteiraInvestimento.application.service;
import com.carteira.carteiraInvestimento.application.port.CorretoraPage;
import com.carteira.carteiraInvestimento.application.port.CorretoraReadScope;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.util.UUID;
public interface BrokerUseCase {
    Corretora criar(String cnpj, String numero, String complemento, UUID actorId, UUID correlationId);
    CorretoraPage listar(int page, int size, CorretoraReadScope scope);
    Corretora consultar(UUID id, CorretoraReadScope scope);
    Corretora consultarPorCnpj(String cnpj, CorretoraReadScope scope);
    Corretora editar(UUID id, BrokerPatch patch, UUID actorId, UUID correlationId);
    Corretora definirLifecycle(UUID id, boolean ativo, UUID actorId, UUID correlationId);
}
