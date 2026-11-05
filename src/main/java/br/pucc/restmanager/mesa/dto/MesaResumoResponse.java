package br.pucc.restmanager.mesa.dto;

import java.math.BigDecimal;

public record MesaResumoResponse(
        Long mesaId,
        int numero,
        String status,
        BigDecimal valorConsumido) {
}