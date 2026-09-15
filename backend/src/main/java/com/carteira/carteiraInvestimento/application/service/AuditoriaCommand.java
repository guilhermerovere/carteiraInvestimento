package com.carteira.carteiraInvestimento.application.service;
import java.time.Instant;
import java.util.UUID;
public record AuditoriaCommand(UUID usuarioId, TipoEvento tipo, ResultadoAuditoria resultado,
        SeveridadeAuditoria severidade, String endpoint, UUID correlationId, Instant instante) {
    public AuditoriaCommand(UUID usuarioId, TipoEvento tipo, ResultadoAuditoria resultado,
            SeveridadeAuditoria severidade, String endpoint, UUID correlationId) {
        this(usuarioId, tipo, resultado, severidade, endpoint, correlationId, null);
    }
}
