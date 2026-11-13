package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.cardapio.dto.CadastrarProdutoRequest;
import br.pucc.restmanager.cardapio.dto.CardapioCategoriaResponse;
import br.pucc.restmanager.cardapio.dto.ProdutoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;

    public ProdutoService(ProdutoRepository produtos, CategoriaRepository categorias) {
        this.produtos = produtos;
        this.categorias = categorias;
    }

    @Transactional
    public ProdutoResponse cadastrar(CadastrarProdutoRequest request) {
        BigDecimal preco = validarPreco(request.preco());
        String nome = request.nome().trim();
        if (produtos.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(ErrorCode.PRODUTO_DUPLICADO);
        }

        Categoria categoria = buscarCategoria(request.categoriaId());

        Produto produto = new Produto(
                categoria,
                nome,
                request.descricao(),
                preco,
                request.ativo());
        produto.adicionarImagemPrincipal(request.imagemUrl());
        return ProdutoResponse.from(produtos.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtos.findAllByOrderByNomeAsc().stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, CadastrarProdutoRequest request) {
        Produto produto = produtos.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUTO_NAO_ENCONTRADO));
        String nome = request.nome().trim();
        if (produtos.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessException(ErrorCode.PRODUTO_DUPLICADO);
        }
        Categoria categoria = buscarCategoria(request.categoriaId());
        produto.atualizar(
                categoria,
                nome,
                request.descricao(),
                validarPreco(request.preco()),
                request.ativo(),
                request.imagemUrl());
        return ProdutoResponse.from(produto);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = produtos.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUTO_NAO_ENCONTRADO));
        produto.desativar();
    }

    private BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PRECO_INVALIDO);
        }
        return preco;
    }

    private Categoria buscarCategoria(Long categoriaId) {
        // Categoria desativada não recebe item novo nem item reativado
        return categorias.findById(categoriaId)
                .filter(Categoria::isAtivo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORIA_NAO_ENCONTRADA));
    }

    @Transactional(readOnly = true)
    public List<CardapioCategoriaResponse> listarCardapio() {
        return categorias.findByAtivoTrueOrderByOrdemAscNomeAsc().stream()
                .map(categoria -> new CardapioCategoriaResponse(
                        categoria.getId(),
                        categoria.getNome(),
                        produtos.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(categoria.getId())
                                .stream()
                                .map(ProdutoResponse::from)
                                .collect(Collectors.toList())))
                .toList();
    }
}