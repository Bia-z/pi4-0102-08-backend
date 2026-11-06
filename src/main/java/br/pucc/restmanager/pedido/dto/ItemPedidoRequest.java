package br.pucc.restmanager.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemPedidoRequest(
        @NotNull Long produtoId,
        @NotNull Integer quantidade,
        @Size(max = 255) String observacao) {
}