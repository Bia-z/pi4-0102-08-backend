package br.pucc.restmanager.pagamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pagamento.domain.FormaPagamento;
import br.pucc.restmanager.pagamento.dto.FormaPagamentoRequest;
import br.pucc.restmanager.pagamento.dto.ProcessarPagamentoRequest;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoMesaRepository pagamentos;

    @Mock
    private StatusMesaPublisher statusMesaPublisher;

    private PagamentoService service;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(pagamentos, statusMesaPublisher);
    }

    @Test
    void recusaPagamentoInsuficienteSemGravarOuLiberarMesa() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of(
                new PagamentoMesaRepository.PedidoPagamento(101L, new BigDecimal("100.00"))));
        when(pagamentos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        ProcessarPagamentoRequest request = request("pix", "50.00");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request));

        assertEquals(ErrorCode.VALOR_PAGAMENTO_INSUFICIENTE, exception.getErro());
        verify(pagamentos, never()).gravarPagamento(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(pagamentos, never()).marcarPedidosPagos(5L);
        verify(pagamentos, never()).liberarMesa(5L);
        verify(statusMesaPublisher, never()).publicarDisponivel(5L, 10);
    }

    @Test
    void recusaContaJaPaga() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of());
        when(pagamentos.existePedidoPago(5L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request("pix", "10.00")));

        assertEquals(ErrorCode.PEDIDO_JA_PAGO, exception.getErro());
        verify(pagamentos, never()).gravarPagamento(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void recusaMesaForaDoStatusDePagamento() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "OCUPADA")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request("pix", "10.00")));

        assertEquals(ErrorCode.MESA_NAO_EM_PAGAMENTO, exception.getErro());
        verify(pagamentos, never()).buscarPedidosAbertos(5L);
    }

    @Test
    void recusaMesaInexistente() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request("pix", "10.00")));

        assertEquals(ErrorCode.MESA_NAO_ENCONTRADA, exception.getErro());
        verify(pagamentos, never()).buscarPedidosAbertos(5L);
    }

    @Test
    void recusaMesaSemPedidoAberto() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of());
        when(pagamentos.existePedidoPago(5L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request("pix", "10.00")));

        assertEquals(ErrorCode.MESA_SEM_PEDIDO_ABERTO, exception.getErro());
    }

    @Test
    void exigeDinheiroParaDevolverValorExcedente() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of(
                new PagamentoMesaRepository.PedidoPagamento(101L, new BigDecimal("100.00"))));
        when(pagamentos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request("pix", "120.00")));

        assertEquals(ErrorCode.TROCO_REQUER_DINHEIRO, exception.getErro());
        verify(pagamentos, never()).gravarPagamento(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(pagamentos, never()).liberarMesa(5L);
    }

    @Test
    void calculaTaxaDescontoETrocoNoPagamentoEmDinheiro() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of(
                new PagamentoMesaRepository.PedidoPagamento(101L, new BigDecimal("100.00"))));
        when(pagamentos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                List.of(new FormaPagamentoRequest("dinheiro", new BigDecimal("115.00"))),
                new BigDecimal("5.00"));

        var response = service.processarPagamento(5L, request);

        assertEquals(new BigDecimal("100.00"), response.subtotal());
        assertEquals(new BigDecimal("10.00"), response.taxaServico());
        assertEquals(new BigDecimal("5.00"), response.desconto());
        assertEquals(new BigDecimal("105.00"), response.total());
        assertEquals(new BigDecimal("115.00"), response.totalPago());
        assertEquals(new BigDecimal("10.00"), response.troco());
        verify(pagamentos).gravarPagamento(
                101L,
                FormaPagamento.DINHEIRO,
                new BigDecimal("115.00"),
                new BigDecimal("10.00"));
    }

    @Test
    void recusaDescontoMaiorQueSubtotalMaisTaxaSemGravarPagamento() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of(
                new PagamentoMesaRepository.PedidoPagamento(101L, new BigDecimal("100.00"))));
        when(pagamentos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                List.of(new FormaPagamentoRequest("dinheiro", new BigDecimal("1.00"))),
                new BigDecimal("110.01"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.processarPagamento(5L, request));

        assertEquals(ErrorCode.DESCONTO_INVALIDO, exception.getErro());
        verify(pagamentos, never()).gravarPagamento(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(pagamentos, never()).marcarPedidosPagos(5L);
        verify(pagamentos, never()).liberarMesa(5L);
    }

    @Test
    void fechaContaComPagamentoDivididoEPublicaMesaDisponivel() {
        when(pagamentos.bloquearMesa(5L)).thenReturn(Optional.of(
                new PagamentoMesaRepository.MesaPagamento(5L, 10, "EM_PAGAMENTO")));
        when(pagamentos.buscarPedidosAbertos(5L)).thenReturn(List.of(
                new PagamentoMesaRepository.PedidoPagamento(101L, new BigDecimal("100.00")),
                new PagamentoMesaRepository.PedidoPagamento(102L, new BigDecimal("50.00"))));
        when(pagamentos.taxaServicoPercentual()).thenReturn(new BigDecimal("10.00"));
        ProcessarPagamentoRequest request = new ProcessarPagamentoRequest(
                List.of(
                        new FormaPagamentoRequest("pix", new BigDecimal("100.00")),
                        new FormaPagamentoRequest("dinheiro", new BigDecimal("65.00"))),
                BigDecimal.ZERO);

        var response = service.processarPagamento(5L, request);

        assertEquals(new BigDecimal("150.00"), response.subtotal());
        assertEquals(new BigDecimal("15.00"), response.taxaServico());
        assertEquals(new BigDecimal("165.00"), response.total());
        assertEquals(new BigDecimal("165.00"), response.totalPago());
        assertEquals(new BigDecimal("0.00"), response.troco());
        assertEquals("disponivel", response.statusMesa());
        verify(pagamentos).marcarPedidosPagos(5L);
        verify(pagamentos).liberarMesa(5L);
        verify(statusMesaPublisher).publicarDisponivel(5L, 10);
    }

    private ProcessarPagamentoRequest request(String forma, String valor) {
        return new ProcessarPagamentoRequest(
                List.of(new FormaPagamentoRequest(forma, new BigDecimal(valor))),
                BigDecimal.ZERO);
    }
}