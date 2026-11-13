package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.dto.CategoriaRequest;
import br.pucc.restmanager.cardapio.dto.CategoriaResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestão das categorias do cardápio pelo administrador.
 * Desativar é lógico: a categoria some do cardápio, mas os itens antigos continuam ligados a ela.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;

    public CategoriaService(CategoriaRepository categorias, ProdutoRepository produtos) {
        this.categorias = categorias;
        this.produtos = produtos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categorias.findAllByOrderByOrdemAscNomeAsc().stream()
                .map(this::responder)
                .toList();
    }

    @Transactional
    public CategoriaResponse cadastrar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categorias.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(ErrorCode.CATEGORIA_DUPLICADA);
        }
        boolean ativo = request.ativo() == null || request.ativo();
        return responder(categorias.save(new Categoria(nome, request.ordem(), ativo)));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        String nome = request.nome().trim();
        if (categorias.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessException(ErrorCode.CATEGORIA_DUPLICADA);
        }
        categoria.atualizar(nome, request.ordem());
        if (Boolean.TRUE.equals(request.ativo())) {
            categoria.ativar();
        } else if (Boolean.FALSE.equals(request.ativo()) && categoria.isAtivo()) {
            garantirSemItensAtivos(categoria);
            categoria.desativar();
        }
        return responder(categorias.save(categoria));
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscar(id);
        garantirSemItensAtivos(categoria);
        categoria.desativar();
        categorias.save(categoria);
    }

    private void garantirSemItensAtivos(Categoria categoria) {
        if (produtos.countByCategoriaIdAndAtivoTrue(categoria.getId()) > 0) {
            throw new BusinessException(ErrorCode.CATEGORIA_COM_ITENS);
        }
    }

    private Categoria buscar(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORIA_NAO_ENCONTRADA));
    }

    private CategoriaResponse responder(Categoria categoria) {
        long itensAtivos = categoria.getId() == null ? 0 : produtos.countByCategoriaIdAndAtivoTrue(categoria.getId());
        return CategoriaResponse.from(categoria, itensAtivos);
    }
}
