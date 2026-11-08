package br.pucc.restmanager.cardapio.dto;

import br.pucc.restmanager.cardapio.domain.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Long categoriaId,
        String imagemUrl,
        boolean ativo,
        LocalDateTime criadoEm,
        List<Long> ingredientes) {

    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria().getId(),
                produto.getImagemUrl(),
                produto.isAtivo(),
                produto.getCriadoEm(),
                produto.getIngredientes().stream().map(ingrediente -> ingrediente.getId()).toList());
    }
}