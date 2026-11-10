package br.pucc.restmanager.cardapio.dto;

import br.pucc.restmanager.cardapio.domain.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Long categoriaId,
        String imagemUrl,
        boolean ativo,
        LocalDateTime criadoEm) {

    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria().getId(),
                produto.getImagemUrl(),
                produto.isAtivo(),
                produto.getCriadoEm());
    }
}