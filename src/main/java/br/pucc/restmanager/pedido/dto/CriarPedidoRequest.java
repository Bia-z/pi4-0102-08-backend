package br.pucc.restmanager.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CriarPedidoRequest(
        @NotNull Long mesaId,
        @NotNull Long garcomId,
        @NotEmpty List<@Valid ItemPedidoRequest> itens) {
}