package br.pucc.restmanager.cardapio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Dados para cadastrar ou editar uma categoria do cardápio.
 * A ordem define a posição da categoria no cardápio do garçom (menor aparece primeiro).
 * {@code ativo} é opcional: quando vem {@code true} na edição, reativa a categoria.
 */
public record CategoriaRequest(
        @NotBlank @Size(max = 80) String nome,
        @NotNull @PositiveOrZero Integer ordem,
        Boolean ativo) {
}
