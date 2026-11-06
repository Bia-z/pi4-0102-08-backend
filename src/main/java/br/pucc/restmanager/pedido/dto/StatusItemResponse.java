package br.pucc.restmanager.pedido.dto;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import java.time.LocalDateTime;
import java.util.Locale;

public record StatusItemResponse(
        Long pedidoId,
        Long itemId,
        String status,
        LocalDateTime atualizadoEm) {

    public static StatusItemResponse from(ItemPedido item) {
        return new StatusItemResponse(
                item.getPedido().getId(),
                item.getId(),
                item.getStatus().name().toLowerCase(Locale.ROOT),
                item.getAtualizadoEm());
    }
}
