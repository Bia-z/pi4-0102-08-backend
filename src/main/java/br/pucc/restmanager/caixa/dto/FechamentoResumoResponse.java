package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record FechamentoResumoResponse(
        CaixaResponse caixa,
        BigDecimal valorEsperado,
        BigDecimal diferenca) {
}