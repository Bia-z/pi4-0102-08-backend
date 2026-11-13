package br.pucc.restmanager.cardapio.dto;

import br.pucc.restmanager.cardapio.domain.Categoria;

public record CategoriaResponse(Long id, String nome, int ordem, boolean ativo, long itensAtivos) {

    public static CategoriaResponse from(Categoria categoria, long itensAtivos) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getOrdem(),
                categoria.isAtivo(),
                itensAtivos);
    }
}
