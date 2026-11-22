package br.pucc.restmanager.comanda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.pedido.PedidoRepository;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComandaServiceTest {

    @Mock
    private PedidoRepository pedidos;

    @Test
    void comandaInformaQueOPedidoJaFoiPago() {
        Pedido pago = pedido(7L, StatusPedido.PAGO);
        when(pedidos.findById(7L)).thenReturn(Optional.of(pago));

        var comanda = new ComandaService(pedidos).obter(7L);

        assertEquals("pago", comanda.statusPedido());
        assertEquals(7L, comanda.pedidoId());
        assertEquals(12, comanda.numeroMesa());
    }

    @Test
    void comandaInformaQueOPedidoAindaNaoFoiPago() {
        Pedido emPreparo = pedido(8L, StatusPedido.EM_PREPARO);
        when(pedidos.findById(8L)).thenReturn(Optional.of(emPreparo));

        assertEquals("em_preparo", new ComandaService(pedidos).obter(8L).statusPedido());
    }

    @Test
    void listaDoDiaTrazOStatusDeCadaPedido() {
        LocalDate dia = LocalDate.of(2026, 11, 8);
        List<Pedido> doDia = List.of(
                pedido(1L, StatusPedido.PAGO),
                pedido(2L, StatusPedido.PRONTO),
                pedido(3L, StatusPedido.CANCELADO));
        when(pedidos.buscarCriadosEntre(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay()))
                .thenReturn(doDia);

        var comandas = new ComandaService(pedidos).listar(dia);

        assertEquals(List.of("pago", "pronto", "cancelado"),
                comandas.stream().map(c -> c.statusPedido()).toList());
    }

    private Pedido pedido(Long id, StatusPedido status) {
        Mesa mesa = mock(Mesa.class);
        Usuario garcom = mock(Usuario.class);
        Pedido pedido = mock(Pedido.class);
        when(mesa.getNumero()).thenReturn(12);
        when(garcom.getNome()).thenReturn("Garcom");
        when(pedido.getId()).thenReturn(id);
        when(pedido.getMesa()).thenReturn(mesa);
        when(pedido.getGarcom()).thenReturn(garcom);
        when(pedido.getDataCriacao()).thenReturn(LocalDateTime.of(2026, 11, 8, 12, 30));
        when(pedido.getStatus()).thenReturn(status);
        when(pedido.getItens()).thenReturn(List.of());
        return pedido;
    }
}