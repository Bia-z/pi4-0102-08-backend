package br.pucc.restmanager.realtime.dto;

import java.time.LocalDateTime;

public record StatusMesaMessage(
        String tipo,
        Long mesaId,
        int numero,
        String status,
        LocalDateTime atualizadoEm) {
}