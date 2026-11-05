package br.pucc.restmanager.cardapio.dto;

import java.util.List;

public record CardapioCategoriaResponse(
        Long categoriaId,
        String nome,
        List<ProdutoResponse> produtos) {
}