package br.pucc.restmanager.realtime.dto;

import br.pucc.restmanager.pedido.domain.Pedido;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoCriadoMessage(
        String tipo,
        Long pedidoId,
        int numeroMesa,
        LocalDateTime horario,
        List<Item> itens) {

    public static PedidoCriadoMessage from(Pedido pedido) {
        return new PedidoCriadoMessage(
                "pedidoCriado",
                pedido.getId(),
                pedido.getMesa().getNumero(),
                pedido.getDataCriacao(),
                pedido.getItens().stream()
                        .map(item -> new Item(
                                item.getProduto().getNome(),
                                item.getQuantidade(),
                                item.getObservacao()))
                        .toList());
    }

    public record Item(String nome, int quantidade, String observacao) {
    }
}
