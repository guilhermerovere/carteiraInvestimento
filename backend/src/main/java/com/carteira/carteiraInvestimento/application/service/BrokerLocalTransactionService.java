package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CorretoraPort;
import com.carteira.carteiraInvestimento.application.port.CorretoraReadScope;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrokerLocalTransactionService {
    private static final String ENDPOINT = "/api/v1/corretoras";
    private final CorretoraPort brokers;
    private final AuditoriaPort audit;
    private final Clock clock;
    public BrokerLocalTransactionService(CorretoraPort brokers, AuditoriaPort audit, Clock clock) { this.brokers=brokers; this.audit=audit; this.clock=clock; }

    @Transactional
    public Corretora create(Corretora broker, UUID actorId, UUID correlationId) {
        Corretora saved = brokers.saveAndFlush(broker);
        audit.record(command(actorId, TipoEvento.CORRETORA_CRIADA, correlationId));
        return saved;
    }
    @Transactional
    public Corretora edit(UUID id, BrokerPatch patch, UUID actorId, UUID correlationId) {
        Corretora current = brokers.findById(id, CorretoraReadScope.ALL).orElseThrow(BrokerNotFoundException::new);
        String number = patch.numeroPresent() ? patch.numero() : current.numero();
        String complement = patch.complementoPresent() ? patch.complemento() : current.complemento();
        Corretora changed = current.editar(number, complement, clock);
        if (changed == current) return current;
        Corretora saved = brokers.saveAndFlush(changed);
        audit.record(command(actorId, TipoEvento.CORRETORA_EDITADA, correlationId));
        return saved;
    }
    @Transactional
    public Corretora lifecycle(UUID id, boolean active, UUID actorId, UUID correlationId) {
        Corretora current = brokers.findById(id, CorretoraReadScope.ALL).orElseThrow(BrokerNotFoundException::new);
        Corretora changed = current.definirAtivo(active, clock);
        if (changed == current) return current;
        Corretora saved = brokers.saveAndFlush(changed);
        audit.record(command(actorId, active ? TipoEvento.CORRETORA_ATIVADA : TipoEvento.CORRETORA_DESATIVADA, correlationId));
        return saved;
    }
    private AuditoriaCommand command(UUID actor, TipoEvento type, UUID correlation) {
        return new AuditoriaCommand(actor, type, ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, ENDPOINT, correlation);
    }
}
