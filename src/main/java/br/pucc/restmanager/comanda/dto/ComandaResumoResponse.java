package br.pucc.restmanager.comanda.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ComandaResumoResponse(
        Long pedidoId,
        int numeroMesa,
        String nomeGarcom,
        LocalDateTime criadaEm,
        List<Item> itens) {

    public ComandaResumoResponse {
        itens = List.copyOf(itens);
    }

    public record Item(
            String nome,
            int quantidade,
            String observacao,
            String status) {
    }
}