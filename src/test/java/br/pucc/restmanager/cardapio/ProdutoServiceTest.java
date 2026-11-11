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
import br.pucc.restmanager.cardapio.domain.Ingrediente;
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
import java.util.Set;
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

    @Mock
    IngredienteRepository ingredientes;

    private ProdutoService service;

    @BeforeEach
    void montar() {
        service = new ProdutoService(produtos, categorias, ingredientes);
    }

    @Test
    void cadastraProdutoNaCategoriaEDevolveRespostaDoContrato() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Ingrediente ingrediente = new Ingrediente("Tomate", true);
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(ingredientes.findByIdInAndAtivoTrue(List.of(1L))).thenReturn(List.of(ingrediente));
        when(produtos.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoResponse response = service.cadastrar(request(
                "Bruschetta", new BigDecimal("28.00"), 1L, null, List.of(1L)));

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
    void recusaIngredienteInexistente() {
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria(1L, "Entradas", 1, true)));
        when(ingredientes.findByIdInAndAtivoTrue(List.of(5L))).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarProdutoRequest(
                        "Bruschetta", null, new BigDecimal("28.00"), 1L, null, List.of(5L), true)));

        assertEquals(ErrorCode.INGREDIENTE_NAO_ENCONTRADO, exception.getErro());
        verify(produtos, never()).save(any(Produto.class));
    }

    @Test
    void cadastraProdutoSemIngredientes() {
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria(1L, "Entradas", 1, true)));
        when(produtos.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoResponse response = service.cadastrar(new CadastrarProdutoRequest(
                "Bruschetta", null, new BigDecimal("28.00"), 1L, null, null, true));

        assertEquals("Bruschetta", response.nome());
        verify(ingredientes, never()).findByIdInAndAtivoTrue(any());
    }

    @Test
    void recusaIngredienteRepetido() {
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria(1L, "Entradas", 1, true)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request(
                        "Bruschetta", new BigDecimal("28.00"), 1L, null, List.of(1L, 1L))));

        assertEquals(ErrorCode.INGREDIENTE_DUPLICADO, exception.getErro());
        verify(ingredientes, never()).findByIdInAndAtivoTrue(any());
        verify(produtos, never()).save(any(Produto.class));
    }

    @Test
    void listaCardapioPorCategoriaSomenteComCategoriasEProdutosAtivos() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Produto ativo = new Produto(
                categoria, "Bruschetta", null, new BigDecimal("28.00"), true, java.util.Set.of());
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
        Ingrediente ingrediente = new Ingrediente("Tomate", true);
        List<Produto> salvos = new ArrayList<>();
        when(produtos.existsByNomeIgnoreCase("Bruschetta")).thenReturn(false);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(ingredientes.findByIdInAndAtivoTrue(List.of(1L))).thenReturn(List.of(ingrediente));
        when(produtos.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            salvos.add(produto);
            return produto;
        });
        when(categorias.findByAtivoTrueOrderByOrdemAscNomeAsc()).thenReturn(List.of(categoria));
        when(produtos.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(1L)).thenAnswer(
                invocation -> salvos.stream().filter(Produto::isAtivo).toList());

        service.cadastrar(request(
                "Bruschetta", new BigDecimal("28.00"), 1L, null, List.of(1L)));
        List<CardapioCategoriaResponse> cardapio = service.listarCardapio();

        assertEquals(List.of("Bruschetta"),
                cardapio.get(0).produtos().stream().map(ProdutoResponse::nome).toList());
        assertTrue(cardapio.get(0).produtos().get(0).ativo());
    }

    @Test
    void atualizaProdutoIncluindoIngredientesEAtivacao() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Ingrediente tomate = new Ingrediente("Tomate", true);
        ReflectionTestUtils.setField(tomate, "id", 1L);
        Produto produto = new Produto(
                categoria, "Bruschetta", null, new BigDecimal("20.00"), true,
                java.util.Set.of());
        ReflectionTestUtils.setField(produto, "id", 10L);
        when(produtos.findById(10L)).thenReturn(java.util.Optional.of(produto));
        when(categorias.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(ingredientes.findByIdInAndAtivoTrue(List.of(1L))).thenReturn(List.of(tomate));

        ProdutoResponse response = service.atualizar(10L, new CadastrarProdutoRequest(
                "Bruschetta atualizada", "Nova descrição", new BigDecimal("28.00"),
                1L, "imagem.jpg", List.of(1L), false));

        assertEquals("Bruschetta atualizada", response.nome());
        assertEquals(new BigDecimal("28.00"), response.preco());
        assertEquals(List.of(1L), response.ingredientes());
        assertFalse(response.ativo());
    }

    @Test
    void mantemIngredientesAtuaisQuandoAtualizacaoNaoInformaIngredientes() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Ingrediente tomate = new Ingrediente("Tomate", true);
        ReflectionTestUtils.setField(tomate, "id", 1L);
        Produto produto = new Produto(
                categoria, "Bruschetta", null, new BigDecimal("20.00"), true, Set.of(tomate));
        ReflectionTestUtils.setField(produto, "id", 10L);
        when(produtos.findById(10L)).thenReturn(Optional.of(produto));
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));

        ProdutoResponse response = service.atualizar(10L, new CadastrarProdutoRequest(
                "Bruschetta", null, new BigDecimal("22.00"), 1L, null, null, true));

        assertEquals(List.of(1L), response.ingredientes());
        verify(ingredientes, never()).findByIdInAndAtivoTrue(any());
    }

    @Test
    void mantemIngredienteJaVinculadoMesmoDesativadoAoAtualizarProduto() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Ingrediente tomateDesativado = new Ingrediente("Tomate", false);
        ReflectionTestUtils.setField(tomateDesativado, "id", 1L);
        Ingrediente manjericao = new Ingrediente("Manjericão", true);
        ReflectionTestUtils.setField(manjericao, "id", 2L);
        Produto produto = new Produto(
                categoria, "Bruschetta", null, new BigDecimal("20.00"), true,
                Set.of(tomateDesativado));
        ReflectionTestUtils.setField(produto, "id", 10L);
        when(produtos.findById(10L)).thenReturn(Optional.of(produto));
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(ingredientes.findByIdInAndAtivoTrue(List.of(2L))).thenReturn(List.of(manjericao));

        ProdutoResponse response = service.atualizar(10L, new CadastrarProdutoRequest(
                "Bruschetta", null, new BigDecimal("22.00"),
                1L, null, List.of(1L, 2L), true));

        assertEquals(Set.of(1L, 2L), Set.copyOf(response.ingredientes()));
    }

    @Test
    void recusaIngredienteNovoDesativadoAoAtualizarProduto() {
        Categoria categoria = categoria(1L, "Entradas", 1, true);
        Produto produto = new Produto(
                categoria, "Bruschetta", null, new BigDecimal("20.00"), true, Set.of());
        ReflectionTestUtils.setField(produto, "id", 10L);
        when(produtos.findById(10L)).thenReturn(Optional.of(produto));
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(ingredientes.findByIdInAndAtivoTrue(List.of(3L))).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.atualizar(10L, new CadastrarProdutoRequest(
                        "Bruschetta", null, new BigDecimal("22.00"),
                        1L, null, List.of(3L), true)));

        assertEquals(ErrorCode.INGREDIENTE_NAO_ENCONTRADO, exception.getErro());
    }

    private Categoria categoria(Long id, String nome, int ordem, boolean ativo) {
        Categoria categoria = new Categoria(nome, ordem, ativo);
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    private CadastrarProdutoRequest request(String nome, BigDecimal preco, Long categoriaId,
                                            String imagemUrl) {
        return new CadastrarProdutoRequest(
                nome, null, preco, categoriaId, imagemUrl, List.of(1L), true);
    }

    private CadastrarProdutoRequest request(String nome, BigDecimal preco, Long categoriaId,
                                            String imagemUrl, List<Long> idsIngredientes) {
        return new CadastrarProdutoRequest(
                nome, null, preco, categoriaId, imagemUrl, idsIngredientes, true);
    }
}