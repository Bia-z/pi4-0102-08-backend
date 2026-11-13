package br.pucc.restmanager.cardapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.dto.CategoriaRequest;
import br.pucc.restmanager.cardapio.dto.CategoriaResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    CategoriaRepository categorias;

    @Mock
    ProdutoRepository produtos;

    private CategoriaService service;

    @BeforeEach
    void montar() {
        service = new CategoriaService(categorias, produtos);
    }

    private Categoria categoria(long id, String nome, int ordem, boolean ativo) {
        Categoria categoria = new Categoria(nome, ordem, ativo);
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    private void salvarDevolvendoComId() {
        when(categorias.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria salva = invocation.getArgument(0);
            if (salva.getId() == null) ReflectionTestUtils.setField(salva, "id", 50L);
            return salva;
        });
    }

    @Test
    void listaTodasEmOrdemComQuantidadeDeItensAtivos() {
        when(categorias.findAllByOrderByOrdemAscNomeAsc()).thenReturn(List.of(
                categoria(1L, "Entradas", 1, true), categoria(2L, "Antiga", 9, false)));
        when(produtos.countByCategoriaIdAndAtivoTrue(1L)).thenReturn(3L);
        when(produtos.countByCategoriaIdAndAtivoTrue(2L)).thenReturn(0L);

        List<CategoriaResponse> lista = service.listar();

        assertEquals(List.of("Entradas", "Antiga"), lista.stream().map(CategoriaResponse::nome).toList());
        assertEquals(3L, lista.get(0).itensAtivos());
        assertFalse(lista.get(1).ativo());
    }

    @Test
    void cadastraCategoriaAtivaSemEspacosNasPontas() {
        when(categorias.existsByNomeIgnoreCase("Porções")).thenReturn(false);
        salvarDevolvendoComId();
        when(produtos.countByCategoriaIdAndAtivoTrue(50L)).thenReturn(0L);

        CategoriaResponse resposta = service.cadastrar(new CategoriaRequest("  Porções ", 5, null));

        assertEquals(50L, resposta.id());
        assertEquals("Porções", resposta.nome());
        assertEquals(5, resposta.ordem());
        assertTrue(resposta.ativo());
    }

    @Test
    void recusaNomeRepetidoIgnorandoMaiusculas() {
        when(categorias.existsByNomeIgnoreCase("bebidas")).thenReturn(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CategoriaRequest("bebidas", 1, true)));

        assertEquals(ErrorCode.CATEGORIA_DUPLICADA, erro.getErro());
        verify(categorias, never()).save(any(Categoria.class));
    }

    @Test
    void editaNomeEOrdem() {
        Categoria atual = categoria(1L, "Entradas", 1, true);
        when(categorias.findById(1L)).thenReturn(Optional.of(atual));
        when(categorias.existsByNomeIgnoreCaseAndIdNot("Petiscos", 1L)).thenReturn(false);
        salvarDevolvendoComId();
        when(produtos.countByCategoriaIdAndAtivoTrue(1L)).thenReturn(2L);

        CategoriaResponse resposta = service.atualizar(1L, new CategoriaRequest("Petiscos", 3, null));

        assertEquals("Petiscos", resposta.nome());
        assertEquals(3, resposta.ordem());
        assertTrue(resposta.ativo(), "sem o campo ativo, não muda a situação");
    }

    @Test
    void recusaEditarParaNomeDeOutraCategoria() {
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria(1L, "Entradas", 1, true)));
        when(categorias.existsByNomeIgnoreCaseAndIdNot("Bebidas", 1L)).thenReturn(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.atualizar(1L, new CategoriaRequest("Bebidas", 1, null)));

        assertEquals(ErrorCode.CATEGORIA_DUPLICADA, erro.getErro());
    }

    @Test
    void edicaoComAtivoTrueReativaACategoria() {
        Categoria desativada = categoria(2L, "Antiga", 9, false);
        when(categorias.findById(2L)).thenReturn(Optional.of(desativada));
        when(categorias.existsByNomeIgnoreCaseAndIdNot("Antiga", 2L)).thenReturn(false);
        salvarDevolvendoComId();
        when(produtos.countByCategoriaIdAndAtivoTrue(2L)).thenReturn(0L);

        assertTrue(service.atualizar(2L, new CategoriaRequest("Antiga", 9, true)).ativo());
    }

    @Test
    void desativaCategoriaSemItensAtivos() {
        Categoria vazia = categoria(2L, "Antiga", 9, true);
        when(categorias.findById(2L)).thenReturn(Optional.of(vazia));
        when(produtos.countByCategoriaIdAndAtivoTrue(2L)).thenReturn(0L);

        service.desativar(2L);

        assertFalse(vazia.isAtivo());
        verify(categorias).save(vazia);
    }

    @Test
    void recusaDesativarCategoriaComItensAtivos() {
        Categoria comItens = categoria(1L, "Entradas", 1, true);
        when(categorias.findById(1L)).thenReturn(Optional.of(comItens));
        when(produtos.countByCategoriaIdAndAtivoTrue(1L)).thenReturn(3L);

        BusinessException erro = assertThrows(BusinessException.class, () -> service.desativar(1L));

        assertEquals(ErrorCode.CATEGORIA_COM_ITENS, erro.getErro());
        assertTrue(comItens.isAtivo());
        verify(categorias, never()).save(any(Categoria.class));
    }

    @Test
    void edicaoComAtivoFalseTambemRespeitaOsItensAtivos() {
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria(1L, "Entradas", 1, true)));
        when(categorias.existsByNomeIgnoreCaseAndIdNot("Entradas", 1L)).thenReturn(false);
        when(produtos.countByCategoriaIdAndAtivoTrue(1L)).thenReturn(1L);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.atualizar(1L, new CategoriaRequest("Entradas", 1, false)));

        assertEquals(ErrorCode.CATEGORIA_COM_ITENS, erro.getErro());
    }

    @Test
    void categoriaInexistenteDaErro() {
        when(categorias.findById(99L)).thenReturn(Optional.empty());

        BusinessException erro = assertThrows(BusinessException.class, () -> service.desativar(99L));

        assertEquals(ErrorCode.CATEGORIA_NAO_ENCONTRADA, erro.getErro());
    }
}
