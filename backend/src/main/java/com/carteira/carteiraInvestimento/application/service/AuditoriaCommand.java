package com.carteira.carteiraInvestimento.application.service;
import java.util.UUID;
public record AuditoriaCommand(UUID usuarioId, TipoEvento tipo, ResultadoAuditoria resultado, SeveridadeAuditoria severidade, String endpoint, UUID correlationId) { }
