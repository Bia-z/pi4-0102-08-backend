package br.pucc.restmanager.cardapio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarIngredienteRequest(@NotBlank @Size(max = 80) String nome) {
}