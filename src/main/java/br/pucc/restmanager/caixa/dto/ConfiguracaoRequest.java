package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record ConfiguracaoRequest(
        String nomeRestaurante,
        BigDecimal taxaServicoPercentual,
        Boolean taxaServicoAtiva,
        Boolean imprimirComandaAoAceitar,
        Boolean comandaUnica,
        Boolean imprimirComprovanteAoFechar) {
}
