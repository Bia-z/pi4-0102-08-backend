package br.pucc.restmanager.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import br.pucc.restmanager.realtime.StatusPedidoPublisher;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CancelamentoItemServiceTest {

    @Mock
    PedidoRepository pedidos;

    @Mock
    ItemPedidoRepository itens;

    @Mock
    MesaRepository mesas;

    @Mock
    StatusPedidoPublisher statusPedidoPublisher;

    @Mock
    StatusMesaPublisher statusMesaPublisher;

    private CancelamentoItemService service;
    private Mesa mesa;
    private Pedido pedido;
    private ItemPedido prato;
    private ItemPedido bebida;

    @BeforeEach
    void montar() {
        service = new CancelamentoItemService(pedidos, itens, mesas, statusPedidoPublisher, statusMesaPublisher);
        mesa = new Mesa(7, 4);
        ReflectionTestUtils.setField(mesa, "id", 3L);
        ReflectionTestUtils.setField(mesa, "status", StatusMesa.OCUPADA);
        Usuario garcom = new Usuario("g1", "Garcom", null, "hash", Perfil.GARCOM, true);
        Categoria categoria = new Categoria("Pratos", 1, true);
        prato = new ItemPedido(
                new Produto(categoria, "Prato feito", null, new BigDecimal("20.00"), true), 2, null);
        bebida = new ItemPedido(
                new Produto(categoria, "Suco", null, new BigDecimal("8.00"), true), 1, "Sem gelo");
        ReflectionTestUtils.setField(prato, "id", 11L);
        ReflectionTestUtils.setField(bebida, "id", 12L);
        pedido = new Pedido(mesa, garcom, new BigDecimal("52.80"));
        ReflectionTestUtils.setField(pedido, "id", 100L);
        pedido.adicionarItem(prato);
        pedido.adicionarItem(bebida);
    }

    @Test
    void cancelaItemAguardandoERecalculaTotalComTaxa() {
        prepararItem(bebida);
        when(mesas.buscarAtivaPorIdComBloqueio(3L)).thenReturn(Optional.of(mesa));
        when(pedidos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        when(itens.save(bebida)).thenReturn(bebida);

        var resposta = service.cancelarItem(100L, 12L);

        assertEquals("cancelado", resposta.status());
        assertEquals(StatusItem.CANCELADO, bebida.getStatus());
        assertEquals(new BigDecimal("44.00"), pedido.getValorTotal());
        assertEquals(StatusPedido.AGUARDANDO, pedido.getStatus());
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
        verify(statusPedidoPublisher).publicar(bebida);
        verify(statusMesaPublisher, never()).publicarDisponivel(anyLong(), any(Integer.class));
    }

    @Test
    void cancelarTodosOsItensCancelaOPedidoELiberaAMesa() {
        prato.cancelar();
        prepararItem(bebida);
        when(mesas.buscarAtivaPorIdComBloqueio(3L)).thenReturn(Optional.of(mesa));
        when(pedidos.taxaServicoPercentual()).thenReturn(BigDecimal.ZERO);
        when(pedidos.existsByMesaIdAndStatusNotIn(eq(3L), anyCollection())).thenReturn(false);
        when(itens.save(bebida)).thenReturn(bebida);

        service.cancelarItem(100L, 12L);

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(new BigDecimal("0.00"), pedido.getValorTotal());
        assertEquals(StatusMesa.DISPONIVEL, mesa.getStatus());
        verify(statusMesaPublisher).publicarDisponivel(3L, 7);
    }

    @Test
    void mesaContinuaOcupadaQuandoTemOutroPedidoEmAberto() {
        prato.cancelar();
        prepararItem(bebida);
        when(mesas.buscarAtivaPorIdComBloqueio(3L)).thenReturn(Optional.of(mesa));
        when(pedidos.taxaServicoPercentual()).thenReturn(BigDecimal.ZERO);
        when(pedidos.existsByMesaIdAndStatusNotIn(eq(3L), anyCollection())).thenReturn(true);
        when(itens.save(bebida)).thenReturn(bebida);

        service.cancelarItem(100L, 12L);

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
        verify(statusMesaPublisher, never()).publicarDisponivel(anyLong(), any(Integer.class));
    }

    @Test
    void recusaCancelarItemQueJaEstaEmPreparo() {
        prato.atualizarStatus(StatusItem.EM_PREPARO);
        prepararItem(prato);
        when(mesas.buscarAtivaPorIdComBloqueio(3L)).thenReturn(Optional.of(mesa));

        BusinessException erro = assertThrows(BusinessException.class, () -> service.cancelarItem(100L, 11L));

        assertEquals(ErrorCode.CANCELAMENTO_NAO_PERMITIDO, erro.getErro());
        verify(statusPedidoPublisher, never()).publicar(any());
    }

    @Test
    void recusaCancelarComMesaEmPagamento() {
        ReflectionTestUtils.setField(mesa, "status", StatusMesa.EM_PAGAMENTO);
        prepararItem(bebida);
        when(mesas.buscarAtivaPorIdComBloqueio(3L)).thenReturn(Optional.of(mesa));

        BusinessException erro = assertThrows(BusinessException.class, () -> service.cancelarItem(100L, 12L));

        assertEquals(ErrorCode.MESA_EM_PAGAMENTO, erro.getErro());
        assertEquals(StatusItem.AGUARDANDO, bebida.getStatus());
    }

    @Test
    void recusaCancelarItemDePedidoJaPago() {
        ReflectionTestUtils.setField(pedido, "status", StatusPedido.PAGO);
        prepararItem(bebida);

        BusinessException erro = assertThrows(BusinessException.class, () -> service.cancelarItem(100L, 12L));

        assertEquals(ErrorCode.PEDIDO_NAO_EM_ABERTO, erro.getErro());
    }

    @Test
    void informaPedidoOuItemInexistente() {
        when(itens.findByIdAndPedidoId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(pedidos.existsById(100L)).thenReturn(true);
        when(pedidos.existsById(999L)).thenReturn(false);

        BusinessException itemInexistente = assertThrows(
                BusinessException.class, () -> service.cancelarItem(100L, 50L));
        BusinessException pedidoInexistente = assertThrows(
                BusinessException.class, () -> service.cancelarItem(999L, 50L));

        assertEquals(ErrorCode.ITEM_PEDIDO_NAO_ENCONTRADO, itemInexistente.getErro());
        assertEquals(ErrorCode.PEDIDO_NAO_ENCONTRADO, pedidoInexistente.getErro());
    }

    private void prepararItem(ItemPedido item) {
        when(itens.findByIdAndPedidoId(item.getId(), 100L)).thenReturn(Optional.of(item));
    }
}
