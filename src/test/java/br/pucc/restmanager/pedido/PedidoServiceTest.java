package br.pucc.restmanager.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.cardapio.ProdutoRepository;
import br.pucc.restmanager.comanda.ImpressaoService;
import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.MesaRepository;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.CriarPedidoRequest;
import br.pucc.restmanager.pedido.dto.ItemPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoListResponse;
import br.pucc.restmanager.pedido.dto.PedidoResponse;
import br.pucc.restmanager.realtime.PedidoCriadoPublisher;
import br.pucc.restmanager.realtime.StatusPedidoPublisher;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    MesaRepository mesas;

    @Mock
    ProdutoRepository produtos;

    @Mock
    UsuarioRepository usuarios;

    @Mock
    PedidoRepository pedidos;

    @Mock
    ItemPedidoRepository itens;

    @Mock
    PedidoCriadoPublisher pedidoCriadoPublisher;

    @Mock
    StatusPedidoPublisher statusPedidoPublisher;

    @Mock
    ImpressaoService impressao;

    private PedidoService service;
    private Mesa mesa;
    private Produto produto;
    private Usuario garcom;

    @BeforeEach
    void montar() {
        service = new PedidoService(
                mesas, produtos, usuarios, pedidos, itens, pedidoCriadoPublisher, statusPedidoPublisher,
                impressao);
        mesa = new Mesa(10, 4);
        ReflectionTestUtils.setField(mesa, "id", 5L);
        produto = new Produto(
                new Categoria("Pratos", 1, true),
                "Prato feito",
                null,
                new BigDecimal("20.00"),
                true);
        ReflectionTestUtils.setField(produto, "id", 8L);
        garcom = new Usuario("g1", "Garcom", null, "hash", Perfil.GARCOM, true);
        ReflectionTestUtils.setField(garcom, "id", 2L);
    }

    @Test
    void criaPedidoComItensObservacaoTotalComTaxaEOcupaMesaDisponivel() {
        prepararMesa(StatusMesa.DISPONIVEL);
        when(produtos.findById(8L)).thenReturn(Optional.of(produto));
        when(usuarios.findById(2L)).thenReturn(Optional.of(garcom));
        when(pedidos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            ReflectionTestUtils.setField(pedido, "id", 101L);
            return pedido;
        });

        PedidoResponse response = service.criarPedido(request(2, "Sem molho"));

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidos).save(pedidoCaptor.capture());
        Pedido salvo = pedidoCaptor.getValue();
        ItemPedido item = salvo.getItens().get(0);
        assertEquals(101L, response.id());
        assertEquals(5L, response.mesaId());
        assertEquals("aguardando", response.status());
        assertEquals(new BigDecimal("44.00"), response.valorTotal());
        assertEquals(StatusPedido.AGUARDANDO, salvo.getStatus());
        assertEquals(StatusItem.AGUARDANDO, item.getStatus());
        assertEquals(2, item.getQuantidade());
        assertEquals("Sem molho", item.getObservacao());
        assertEquals(new BigDecimal("20.00"), item.getPrecoUnitario());
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
        verify(pedidoCriadoPublisher).publicar(salvo);
    }

    @Test
    void ocupaMesaReservadaAoCriarPedido() {
        prepararMesa(StatusMesa.RESERVADA);
        prepararPedidoValido();

        service.criarPedido(request(1, "Sem sal"));

        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
    }

    @Test
    void pedidoAdicionalPermaneceNaMesaOcupada() {
        prepararMesa(StatusMesa.OCUPADA);
        prepararPedidoValido();

        PedidoResponse response = service.criarPedido(request(1, null));

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidos).save(captor.capture());
        assertEquals(5L, captor.getValue().getMesa().getId());
        assertEquals(5L, response.mesaId());
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
    }

    @Test
    void recusaMesaInexistenteComErroDaFicha() {
        when(mesas.buscarAtivaPorIdComBloqueio(5L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.criarPedido(request(1, null)));

        assertEquals(ErrorCode.MESA_NAO_ENCONTRADA, exception.getErro());
        verify(pedidos, never()).save(any(Pedido.class));
        verify(pedidoCriadoPublisher, never()).publicar(any(Pedido.class));
    }

    @Test
    void recusaMesaEmPagamentoComErroDaFicha() {
        prepararMesa(StatusMesa.EM_PAGAMENTO);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.criarPedido(request(1, null)));

        assertEquals(ErrorCode.MESA_EM_PAGAMENTO, exception.getErro());
        verify(pedidos, never()).save(any(Pedido.class));
        verify(pedidoCriadoPublisher, never()).publicar(any(Pedido.class));
    }

    @Test
    void recusaProdutoInexistenteComErroDaFicha() {
        prepararMesa(StatusMesa.DISPONIVEL);
        when(produtos.findById(8L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.criarPedido(request(1, null)));

        assertEquals(ErrorCode.PRODUTO_NAO_ENCONTRADO, exception.getErro());
        verify(pedidos, never()).save(any(Pedido.class));
        verify(pedidoCriadoPublisher, never()).publicar(any(Pedido.class));
    }

    @Test
    void recusaProdutoInativoComErroDaFicha() {
        prepararMesa(StatusMesa.DISPONIVEL);
        Produto inativo = new Produto(
                new Categoria("Pratos", 1, true),
                "Prato inativo",
                null,
                new BigDecimal("20.00"),
                false);
        when(produtos.findById(8L)).thenReturn(Optional.of(inativo));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.criarPedido(request(1, null)));

        assertEquals(ErrorCode.PRODUTO_NAO_ENCONTRADO, exception.getErro());
        verify(pedidos, never()).save(any(Pedido.class));
        verify(pedidoCriadoPublisher, never()).publicar(any(Pedido.class));
    }

    @Test
    void recusaQuantidadeMenorQueUmComErroDaFicha() {
        prepararMesa(StatusMesa.DISPONIVEL);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.criarPedido(new CriarPedidoRequest(
                        5L, 2L, List.of(new ItemPedidoRequest(8L, 0, null)))));

        assertEquals(ErrorCode.QUANTIDADE_INVALIDA, exception.getErro());
        verify(produtos, never()).findById(any());
        verify(pedidos, never()).save(any(Pedido.class));
        verify(pedidoCriadoPublisher, never()).publicar(any(Pedido.class));
    }

    @Test
    void listaPedidosComFiltrosNaOrdemDoRepositorio() {
        Pedido pedido = pedidoParaListagem(StatusItem.AGUARDANDO, StatusPedido.AGUARDANDO);
        when(pedidos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        when(pedidos.listarAbertos(
                StatusPedido.AGUARDANDO, StatusItem.AGUARDANDO, 2L, 5L, false))
                .thenReturn(List.of(pedido));

        List<PedidoListResponse> resposta = service.listarPedidos(
                StatusPedido.AGUARDANDO, StatusItem.AGUARDANDO, 2L, 5L, false);

        assertEquals(1, resposta.size());
        assertEquals(5L, resposta.get(0).mesaId());
        assertEquals(2L, resposta.get(0).garcomId());
        assertEquals(new BigDecimal("10.00"), resposta.get(0).taxaServicoPercentual());
        assertEquals(new BigDecimal("20.00"), resposta.get(0).itens().get(0).precoUnitario());
        assertEquals("aguardando", resposta.get(0).itens().get(0).status());
        verify(pedidos).listarAbertos(
                StatusPedido.AGUARDANDO, StatusItem.AGUARDANDO, 2L, 5L, false);
    }

    @Test
    void cozinhaRecebeSomentePedidosEItensAguardandoOuEmPreparo() {
        Pedido pedidoComItensEmVariosStatus = pedidoParaListagem(
                StatusItem.AGUARDANDO, StatusPedido.EM_PREPARO);
        ItemPedido pronto = new ItemPedido(produto, 1, null);
        ReflectionTestUtils.setField(pronto, "status", StatusItem.PRONTO);
        pedidoComItensEmVariosStatus.adicionarItem(pronto);
        Pedido pedidoSemItensPendentes = pedidoParaListagem(
                StatusItem.PRONTO, StatusPedido.PRONTO);
        when(pedidos.listarAbertos(null, null, null, null, true))
                .thenReturn(List.of(pedidoComItensEmVariosStatus, pedidoSemItensPendentes));

        List<PedidoListResponse> resposta = service.listarPedidos(null, null, null, null, true);

        assertEquals(1, resposta.size());
        assertEquals(1, resposta.get(0).itens().size());
        assertEquals("aguardando", resposta.get(0).itens().get(0).status());
    }

    @Test
    void listaVaziaQuandoNaoHaPedidosAbertosCorrespondentes() {
        when(pedidos.listarAbertos(null, null, null, null, false)).thenReturn(List.of());

        assertEquals(List.of(), service.listarPedidos(null, null, null, null, false));
    }

    @Test
    void atualizaStatusDoItemNaSequenciaPermitidaEPublicaEvento() {
        Pedido pedido = pedidoParaListagem(StatusItem.AGUARDANDO, StatusPedido.AGUARDANDO);
        ItemPedido item = pedido.getItens().get(0);
        when(pedidos.existsById(100L)).thenReturn(true);
        when(itens.findByIdAndPedidoId(200L, 100L)).thenReturn(Optional.of(item));
        when(itens.save(item)).thenReturn(item);

        var resposta = service.atualizarStatusPedido(100L, 200L, StatusItem.EM_PREPARO);

        assertEquals("em_preparo", resposta.status());
        assertEquals(StatusItem.EM_PREPARO, item.getStatus());
        assertEquals(100L, resposta.pedidoId());
        assertEquals(200L, resposta.itemId());
        verify(statusPedidoPublisher).publicar(item);
    }

    @Test
    void recusaAtualizacaoQuandoPedidoNaoExisteComErroDaFicha() {
        when(pedidos.existsById(100L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.atualizarStatusPedido(100L, 200L, StatusItem.EM_PREPARO));

        assertEquals(ErrorCode.PEDIDO_NAO_ENCONTRADO, exception.getErro());
        verify(itens, never()).save(any(ItemPedido.class));
        verify(statusPedidoPublisher, never()).publicar(any(ItemPedido.class));
    }

    @Test
    void recusaAtualizacaoQuandoItemNaoPertenceAoPedido() {
        when(pedidos.existsById(100L)).thenReturn(true);
        when(itens.findByIdAndPedidoId(200L, 100L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.atualizarStatusPedido(100L, 200L, StatusItem.EM_PREPARO));

        assertEquals(ErrorCode.ITEM_PEDIDO_NAO_ENCONTRADO, exception.getErro());
        verify(itens, never()).save(any(ItemPedido.class));
        verify(statusPedidoPublisher, never()).publicar(any(ItemPedido.class));
    }

    @Test
    void recusaTransicaoForaDaSequenciaComErroDaFicha() {
        ItemPedido item = pedidoParaListagem(StatusItem.AGUARDANDO, StatusPedido.AGUARDANDO)
                .getItens().get(0);
        when(pedidos.existsById(100L)).thenReturn(true);
        when(itens.findByIdAndPedidoId(200L, 100L)).thenReturn(Optional.of(item));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.atualizarStatusPedido(100L, 200L, StatusItem.PRONTO));

        assertEquals(ErrorCode.TRANSICAO_STATUS_INVALIDA, exception.getErro());
        verify(itens, never()).save(any(ItemPedido.class));
        verify(statusPedidoPublisher, never()).publicar(any(ItemPedido.class));
    }

    private Pedido pedidoParaListagem(StatusItem statusItem, StatusPedido statusPedido) {
        Pedido pedido = new Pedido(mesa, garcom, new BigDecimal("22.00"));
        ReflectionTestUtils.setField(pedido, "id", 100L);
        ReflectionTestUtils.setField(pedido, "status", statusPedido);
        ItemPedido item = new ItemPedido(produto, 1, null);
        ReflectionTestUtils.setField(item, "id", 200L);
        ReflectionTestUtils.setField(item, "status", statusItem);
        pedido.adicionarItem(item);
        return pedido;
    }

    private void prepararPedidoValido() {
        when(produtos.findById(8L)).thenReturn(Optional.of(produto));
        when(usuarios.findById(2L)).thenReturn(Optional.of(garcom));
        when(pedidos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepararMesa(StatusMesa status) {
        ReflectionTestUtils.setField(mesa, "status", status);
        when(mesas.buscarAtivaPorIdComBloqueio(5L)).thenReturn(Optional.of(mesa));
    }

    private CriarPedidoRequest request(int quantidade, String observacao) {
        return new CriarPedidoRequest(
                5L,
                2L,
                List.of(new ItemPedidoRequest(8L, quantidade, observacao)));
    }
}