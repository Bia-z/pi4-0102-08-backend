package br.pucc.restmanager.pagamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record ProcessarPagamentoRequest(
        @NotEmpty List<@Valid FormaPagamentoRequest> pagamentos,
        @PositiveOrZero BigDecimal desconto) {

    public ProcessarPagamentoRequest {
        desconto = desconto == null ? BigDecimal.ZERO : desconto;
    }
}