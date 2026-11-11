package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record ResumoTurnoResponse(
        CaixaResponse caixa,
        BigDecimal totalRecebido,
        BigDecimal totalDinheiro,
        BigDecimal totalCredito,
        BigDecimal totalDebito,
        BigDecimal totalPix,
        BigDecimal totalVale,
        long quantidadePagamentos,
        BigDecimal totalSangrias,
        BigDecimal totalReforcos) {
}
