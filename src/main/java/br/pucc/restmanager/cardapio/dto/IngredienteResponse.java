package br.pucc.restmanager.cardapio.dto;

import br.pucc.restmanager.cardapio.domain.Ingrediente;

public record IngredienteResponse(Long id, String nome) {

    public static IngredienteResponse from(Ingrediente ingrediente) {
        return new IngredienteResponse(ingrediente.getId(), ingrediente.getNome());
    }
}