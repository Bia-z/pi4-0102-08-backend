package br.pucc.restmanager.mesa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CadastrarMesaRequest(
        @NotNull @Positive Integer numero,
        @NotNull Integer capacidade) {
}