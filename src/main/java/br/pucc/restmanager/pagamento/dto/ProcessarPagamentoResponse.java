package br.pucc.restmanager.pagamento.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProcessarPagamentoResponse(
        Long mesaId,
        BigDecimal subtotal,
        BigDecimal taxaServico,
        BigDecimal desconto,
        BigDecimal total,
        BigDecimal totalPago,
        BigDecimal troco,
        List<Long> pedidosPagos,
        String statusMesa) {
}
