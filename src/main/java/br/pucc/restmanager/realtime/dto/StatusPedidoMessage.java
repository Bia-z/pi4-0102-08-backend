package br.pucc.restmanager.realtime.dto;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import java.time.LocalDateTime;
import java.util.Locale;

public record StatusPedidoMessage(
        String tipo,
        Long pedidoId,
        Long itemId,
        int numeroMesa,
        String status,
        LocalDateTime atualizadoEm) {

    public static StatusPedidoMessage from(ItemPedido item) {
        return new StatusPedidoMessage(
                "statusPedidoAtualizado",
                item.getPedido().getId(),
                item.getId(),
                item.getPedido().getMesa().getNumero(),
                item.getStatus().name().toLowerCase(Locale.ROOT),
                item.getAtualizadoEm());
    }
}