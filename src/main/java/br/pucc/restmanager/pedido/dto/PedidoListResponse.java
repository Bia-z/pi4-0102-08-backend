package br.pucc.restmanager.pedido.dto;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public record PedidoListResponse(
        Long id,
        Long mesaId,
        int numeroMesa,
        Long garcomId,
        String status,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao,
        List<Item> itens) {

    public static PedidoListResponse from(Pedido pedido, boolean cozinha) {
        List<Item> itens = pedido.getItens().stream()
                .filter(item -> !cozinha || item.getStatus() == StatusItem.AGUARDANDO
                        || item.getStatus() == StatusItem.EM_PREPARO)
                .map(Item::from)
                .toList();
        return new PedidoListResponse(
                pedido.getId(),
                pedido.getMesa().getId(),
                pedido.getMesa().getNumero(),
                pedido.getGarcom().getId(),
                pedido.getStatus().name().toLowerCase(Locale.ROOT),
                pedido.getValorTotal(),
                pedido.getDataCriacao(),
                itens);
    }

    public record Item(
            Long id,
            Long produtoId,
            String produtoNome,
            int quantidade,
            String observacao,
            String status) {

        private static Item from(ItemPedido item) {
            return new Item(
                    item.getId(),
                    item.getProduto().getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    item.getObservacao(),
                    item.getStatus().name().toLowerCase(Locale.ROOT));
        }
    }
}
