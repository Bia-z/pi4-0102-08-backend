package br.pucc.restmanager.pedido.dto;

import br.pucc.restmanager.pedido.domain.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

public record PedidoResponse(
        Long id,
        Long mesaId,
        String status,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao) {

    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getMesa().getId(),
                pedido.getStatus().name().toLowerCase(Locale.ROOT),
                pedido.getValorTotal(),
                pedido.getDataCriacao());
    }
}