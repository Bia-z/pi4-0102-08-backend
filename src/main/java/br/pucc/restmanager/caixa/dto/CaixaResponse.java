package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CaixaResponse(
        Long id,
        Long operadorId,
        String turno,
        String status,
        BigDecimal valorAbertura,
        LocalDateTime abertoEm,
        BigDecimal valorFechamento,
        BigDecimal valorEsperado,
        LocalDateTime fechadoEm) {
}