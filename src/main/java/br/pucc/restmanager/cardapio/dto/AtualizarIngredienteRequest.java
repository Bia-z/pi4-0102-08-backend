package br.pucc.restmanager.cardapio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarIngredienteRequest(
        @NotBlank @Size(max = 80) String nome,
        @NotNull Boolean ativo) {
}
