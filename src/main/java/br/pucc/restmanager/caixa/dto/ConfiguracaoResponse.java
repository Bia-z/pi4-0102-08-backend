package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record ConfiguracaoResponse(
        String nomeRestaurante,
        BigDecimal taxaServicoPercentual,
        boolean taxaServicoAtiva,
        boolean imprimirComandaAoAceitar,
        boolean comandaUnica,
        boolean imprimirComprovanteAoFechar) {
}
