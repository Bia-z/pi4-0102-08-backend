package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.domain.Ingrediente;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.cardapio.dto.CadastrarProdutoRequest;
import br.pucc.restmanager.cardapio.dto.CardapioCategoriaResponse;
import br.pucc.restmanager.cardapio.dto.ProdutoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;
    private final IngredienteRepository ingredientes;

    public ProdutoService(
            ProdutoRepository produtos,
            CategoriaRepository categorias,
            IngredienteRepository ingredientes) {
        this.produtos = produtos;
        this.categorias = categorias;
        this.ingredientes = ingredientes;
    }

    @Transactional
    public ProdutoResponse cadastrar(CadastrarProdutoRequest request) {
        BigDecimal preco = validarPreco(request.preco());
        String nome = request.nome().trim();
        if (produtos.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(ErrorCode.PRODUTO_DUPLICADO);
        }

        Categoria categoria = buscarCategoria(request.categoriaId());
        List<Ingrediente> encontrados = buscarIngredientes(request.ingredientes());

        Produto produto = new Produto(
                categoria,
                nome,
                request.descricao(),
                preco,
                request.ativo(),
                Set.copyOf(encontrados));
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
        List<Ingrediente> encontrados = buscarIngredientes(request.ingredientes());
        produto.atualizar(
                categoria,
                nome,
                request.descricao(),
                validarPreco(request.preco()),
                request.ativo(),
                encontrados,
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
        return categorias.findById(categoriaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORIA_NAO_ENCONTRADA));
    }

    private List<Ingrediente> buscarIngredientes(List<Long> idsIngredientes) {
        if (idsIngredientes == null || idsIngredientes.isEmpty()) {
            throw new BusinessException(ErrorCode.INGREDIENTES_OBRIGATORIOS);
        }
        if (idsIngredientes.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.INGREDIENTE_NAO_ENCONTRADO);
        }
        if (idsIngredientes.stream().distinct().count() != idsIngredientes.size()) {
            throw new BusinessException(ErrorCode.INGREDIENTE_DUPLICADO);
        }
        List<Ingrediente> encontrados = ingredientes.findByIdInAndAtivoTrue(idsIngredientes);
        if (encontrados.size() != idsIngredientes.size()) {
            throw new BusinessException(ErrorCode.INGREDIENTE_NAO_ENCONTRADO);
        }
        return encontrados;
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