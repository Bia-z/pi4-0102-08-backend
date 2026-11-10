package br.pucc.restmanager.cardapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.cardapio.dto.CadastrarProdutoRequest;
import br.pucc.restmanager.cardapio.dto.CardapioCategoriaResponse;
import br.pucc.restmanager.cardapio.dto.ProdutoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    ProdutoRepository produtos;

    @Mock
    CategoriaRepository categorias;

    private ProdutoService service;

    @BeforeEach
    void montar() {
        service = new ProdutoService(produtos, categorias);
    }

    @Test
    void cadastraProdutoNaCategoriaEDevolveRespostaDoContrato() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtos.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoResponse response = service.cadastrar(request(
                "Bruschetta", new BigDecimal("28.00"), 1L, null));

        assertEquals("Bruschetta", response.nome());
        assertEquals(1L, response.categoriaId());
        assertEquals(new BigDecimal("28.00"), response.preco());
        assertTrue(response.ativo());
    }

    @Test
    void recusaPrecoZeroComErroDaFicha() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("Bruschetta", BigDecimal.ZERO, 1L, null)));

        assertEquals(ErrorCode.PRECO_INVALIDO, exception.getErro());
        verify(produtos, never()).save(any(Produto.class));
    }

    @Test
    void recusaPrecoNegativoComErroDaFicha() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("Bruschetta", new BigDecimal("-0.01"), 1L, null)));

        assertEquals(ErrorCode.PRECO_INVALIDO, exception.getErro());
        verify(produtos, never()).save(any(Produto.class));
    }

    @Test
    void recusaCategoriaInexistenteComErroDaFicha() {
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("Bruschetta", new BigDecimal("28.00"), 99L, null)));

        assertEquals(ErrorCode.CATEGORIA_NAO_ENCONTRADA, exception.getErro());
        verify(produtos, never()).save(any(Produto.class));
    }

    @Test
    void recusaProdutoComNomeDuplicadoComErroDaFicha() {
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("Bruschetta", new BigDecimal("28.00"), 1L, null)));

        assertEquals(ErrorCode.PRODUTO_DUPLICADO, exception.getErro());
        verify(produtos, never()).save(any(Produto.class));
    }

    @Test
    void listaCardapioPorCategoriaSomenteComCategoriasEProdutosAtivos() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Produto ativo = new Produto(
                categoria, "Bruschetta", null, new BigDecimal("28.00"), true);
        when(categorias.findByAtivoTrueOrderByOrdemAscNomeAsc()).thenReturn(List.of(categoria));
        when(produtos.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(1L)).thenReturn(List.of(ativo));

        List<CardapioCategoriaResponse> response = service.listarCardapio();

        assertEquals(1, response.size());
        assertEquals("Entradas", response.get(0).nome());
        assertEquals(List.of("Bruschetta"),
                response.get(0).produtos().stream().map(ProdutoResponse::nome).toList());
        verify(produtos).findByCategoriaIdAndAtivoTrueOrderByNomeAsc(1L);
    }

    @Test
    void produtoAtivoCadastradoFicaDisponivelNoCardapio() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        List<Produto> salvos = new ArrayList<>();
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtos.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            salvos.add(produto);
            return produto;
        });
        when(categorias.findByAtivoTrueOrderByOrdemAscNomeAsc()).thenReturn(List.of(categoria));
        when(produtos.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(1L)).thenAnswer(
                invocation -> salvos.stream().filter(Produto::isAtivo).toList());

        service.cadastrar(request(
                "Bruschetta", new BigDecimal("28.00"), 1L, null));
        List<CardapioCategoriaResponse> cardapio = service.listarCardapio();

        assertEquals(List.of("Bruschetta"),
                cardapio.get(0).produtos().stream().map(ProdutoResponse::nome).toList());
        assertTrue(cardapio.get(0).produtos().get(0).ativo());
    }

    @Test
    void atualizaProdutoIncluindoCategoriaEAtivacao() {
        Categoria entradas = categoria(1L, "Entradas", 1, true);
        Categoria adicionais = categoria(2L, "Adicionais", 4, true);
        Produto produto = new Produto(
                entradas, "Bruschetta", null, new BigDecimal("20.00"), true);
        ReflectionTestUtils.setField(produto, "id", 10L);
        when(produtos.findById(10L)).thenReturn(Optional.of(produto));
        when(categorias.findById(2L)).thenReturn(Optional.of(adicionais));

        ProdutoResponse response = service.atualizar(10L, new CadastrarProdutoRequest(
                "Bruschetta atualizada", "Nova descrição", new BigDecimal("28.00"),
                2L, "imagem.jpg", false));

        assertEquals("Bruschetta atualizada", response.nome());
        assertEquals(new BigDecimal("28.00"), response.preco());
        assertEquals(2L, response.categoriaId());
        assertFalse(response.ativo());
    }

    @Test
    void produtoDaCategoriaAdicionaisAparecePertoDasDemaisCategoriasNoCardapio() {
        Categoria pratos = categoria(1L, "Pratos principais", 2, true);
        Categoria adicionais = categoria(2L, "Adicionais", 4, true);
        Produto queijo = new Produto(
                adicionais, "Queijo extra", null, new BigDecimal("6.00"), true);
        when(categorias.findByAtivoTrueOrderByOrdemAscNomeAsc())
                .thenReturn(List.of(pratos, adicionais));
        when(produtos.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(1L)).thenReturn(List.of());
        when(produtos.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(2L)).thenReturn(List.of(queijo));

        List<CardapioCategoriaResponse> cardapio = service.listarCardapio();

        assertEquals(List.of("Pratos principais", "Adicionais"),
                cardapio.stream().map(CardapioCategoriaResponse::nome).toList());
        assertEquals(List.of("Queijo extra"),
                cardapio.get(1).produtos().stream().map(ProdutoResponse::nome).toList());
    }

    private Categoria categoria(Long id, String nome, int ordem, boolean ativo) {
        Categoria categoria = new Categoria(nome, ordem, ativo);
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    private CadastrarProdutoRequest request(String nome, BigDecimal preco, Long categoriaId,
                                            String imagemUrl) {
        return new CadastrarProdutoRequest(
                nome, null, preco, categoriaId, imagemUrl, true);
    }
}
