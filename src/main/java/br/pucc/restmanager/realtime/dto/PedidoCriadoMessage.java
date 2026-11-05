package br.pucc.restmanager.realtime.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoCriadoMessage(
        Long pedidoId,
        int numeroMesa,
        LocalDateTime horario,
        List<Item> itens) {

    public record Item(
            Long itemId,
            String nome,
            int quantidade,
            String observacao,
            String status) {
    }
}
