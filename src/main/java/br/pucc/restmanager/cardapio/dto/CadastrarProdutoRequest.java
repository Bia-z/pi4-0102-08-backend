package br.pucc.restmanager.cardapio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CadastrarProdutoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 500) String descricao,
        BigDecimal preco,
        @NotNull Long categoriaId,
        @Size(max = 255) String imagemUrl,
        @NotNull Boolean ativo) {
}