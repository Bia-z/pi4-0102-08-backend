package br.pucc.restmanager.mesa.dto;

import java.time.LocalDateTime;

public record PagamentoIniciadoResponse(
        Long mesaId,
        int numero,
        String status,
        Long iniciadoPor,
        LocalDateTime iniciadoEm) {
}