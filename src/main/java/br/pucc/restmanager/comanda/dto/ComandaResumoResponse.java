package br.pucc.restmanager.comanda.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ComandaResumoResponse(
        Long pedidoId,
        int numeroMesa,
        String nomeGarcom,
        LocalDateTime criadaEm,
        String statusPedido,
        List<Item> itens) {

    public ComandaResumoResponse {
        itens = List.copyOf(itens);
    }

    public ComandaResumoResponse somenteItem(Long itemId) {
        return new ComandaResumoResponse(pedidoId, numeroMesa, nomeGarcom, criadaEm,
                statusPedido, itens.stream().filter(item -> item.itemId().equals(itemId)).toList());
    }

    public record Item(
            Long itemId,
            String nome,
            int quantidade,
            String observacao,
            String status) {
    }
}
