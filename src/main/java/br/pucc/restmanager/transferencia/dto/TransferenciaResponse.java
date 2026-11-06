package br.pucc.restmanager.transferencia.dto;

import java.time.LocalDateTime;

public record TransferenciaResponse(
        Long pedidoId,
        Long mesaOrigemId,
        Long mesaDestinoId,
        LocalDateTime transferidoEm) {
}