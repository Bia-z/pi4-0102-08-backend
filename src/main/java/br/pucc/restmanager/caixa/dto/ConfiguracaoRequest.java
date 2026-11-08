package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record ConfiguracaoRequest(
        String nomeRestaurante,
        BigDecimal taxaServicoPercentual,
        Boolean imprimirComandaAoAceitar) {
}