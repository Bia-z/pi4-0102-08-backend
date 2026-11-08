package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioDiaResponse(
        LocalDate data,
        BigDecimal totalRecebido,
        BigDecimal totalDinheiro,
        BigDecimal totalCredito,
        BigDecimal totalDebito,
        BigDecimal totalPix,
        BigDecimal totalVale,
        long quantidadePagamentos,
        List<MovimentacaoResponse> movimentacoes) {
    public RelatorioDiaResponse {
        movimentacoes = List.copyOf(movimentacoes);
    }
}