package br.pucc.restmanager.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarStatusItemRequest(@NotBlank String novoStatus) {
}