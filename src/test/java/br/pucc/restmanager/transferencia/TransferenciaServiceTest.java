package br.pucc.restmanager.transferencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import br.pucc.restmanager.transferencia.dto.TransferenciaResponse;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    private static final Long PEDIDO_ID = 101L;
    private static final Long ORIGEM_ID = 5L;
    private static final Long DESTINO_ID = 8L;

    @Mock
    private TransferenciaMesaRepository transferencias;

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private StatusMesaPublisher publisher;

    @Mock
    private Usuario usuario;

    private TransferenciaService service;

    @BeforeEach
    void setUp() {
        service = new TransferenciaService(transferencias, usuarios, publisher);
    }

    @Test
    void pedidoInexistenteRetornaErroDaFicha() {
        when(transferencias.buscarMesaDoPedido(PEDIDO_ID)).thenReturn(Optional.empty());

        assertErro(ErrorCode.PEDIDO_NAO_ENCONTRADO);
        verify(transferencias, never()).bloquearMesas(ORIGEM_ID, DESTINO_ID);
    }

    @Test
    void mesaDeDestinoOcupadaRetornaErroDaFicha() {
        prepararMesas("OCUPADA");

        assertErro(ErrorCode.MESA_DESTINO_OCUPADA);
        verify(transferencias, never()).bloquearPedido(PEDIDO_ID);
    }

    @Test
    void mesaDeDestinoReservadaNaoEstaDisponivel() {
        prepararMesas("RESERVADA");

        assertErro(ErrorCode.MESA_DESTINO_OCUPADA);
    }

    @Test
    void mesaDeOrigemEmPagamentoRetornaErroDaFicha() {
        prepararMesas("DISPONIVEL", "EM_PAGAMENTO");

        assertErro(ErrorCode.MESA_EM_PAGAMENTO);
    }

    @Test
    void mesaDeDestinoEmPagamentoRetornaErroDaFicha() {
        prepararMesas("EM_PAGAMENTO");

        assertErro(ErrorCode.MESA_EM_PAGAMENTO);
    }

    @Test
    void pedidoPagoOuCanceladoNaoPodeSerTransferido() {
        prepararMesas("DISPONIVEL");
        when(transferencias.bloquearPedido(PEDIDO_ID)).thenReturn(Optional.of(
                new TransferenciaMesaRepository.PedidoTransferencia(
                        PEDIDO_ID, ORIGEM_ID, "PAGO")));

        assertErro(ErrorCode.PEDIDO_NAO_EM_ABERTO);
        verifyNoInteractions(usuarios, publisher);
    }

    @Test
    void transferePedidoSemLiberarOrigemSeAindaHaPedidosAbertos() {
        prepararPedidoAberto();
        when(transferencias.existePedidoAberto(ORIGEM_ID)).thenReturn(true);
        when(usuarios.findByCodigo("GARCOM-1")).thenReturn(Optional.of(usuario));
        when(usuario.getId()).thenReturn(17L);

        TransferenciaResponse resposta =
                service.transferirMesa(PEDIDO_ID, DESTINO_ID, "GARCOM-1");

        assertEquals(PEDIDO_ID, resposta.pedidoId());
        assertEquals(ORIGEM_ID, resposta.mesaOrigemId());
        assertEquals(DESTINO_ID, resposta.mesaDestinoId());
        verify(transferencias).moverPedido(PEDIDO_ID, DESTINO_ID);
        verify(transferencias).atualizarStatusMesa(DESTINO_ID, "OCUPADA");
        verify(transferencias, never()).atualizarStatusMesa(ORIGEM_ID, "DISPONIVEL");
        verify(transferencias).registrarTransferencia(
                PEDIDO_ID, ORIGEM_ID, DESTINO_ID, 17L, resposta.transferidoEm());
        verify(publisher).publicarStatusAtualizado(ORIGEM_ID, 5, "ocupada");
        verify(publisher).publicarStatusAtualizado(DESTINO_ID, 8, "ocupada");
    }

    @Test
    void liberaOrigemSemPedidosAbertosERegistraTransferencia() {
        prepararPedidoAberto();
        when(transferencias.existePedidoAberto(ORIGEM_ID)).thenReturn(false);
        when(usuarios.findByCodigo("GARCOM-1")).thenReturn(Optional.of(usuario));
        when(usuario.getId()).thenReturn(17L);

        TransferenciaResponse resposta =
                service.transferirMesa(PEDIDO_ID, DESTINO_ID, "GARCOM-1");

        verify(transferencias).atualizarStatusMesa(ORIGEM_ID, "DISPONIVEL");
        verify(transferencias).registrarTransferencia(
                PEDIDO_ID, ORIGEM_ID, DESTINO_ID, 17L, resposta.transferidoEm());
        verify(publisher).publicarStatusAtualizado(ORIGEM_ID, 5, "disponivel");
        verify(publisher).publicarStatusAtualizado(DESTINO_ID, 8, "ocupada");
    }

    @Test
    void destinoIgualAOrigemEhRecusado() {
        when(transferencias.buscarMesaDoPedido(PEDIDO_ID)).thenReturn(Optional.of(ORIGEM_ID));

        assertThrows(
                BusinessException.class,
                () -> service.transferirMesa(PEDIDO_ID, ORIGEM_ID, "GARCOM-1"));
        verify(transferencias, never()).bloquearMesas(ORIGEM_ID, ORIGEM_ID);
    }

    private void prepararMesas(String statusDestino) {
        prepararMesas(statusDestino, "OCUPADA");
    }

    private void prepararMesas(String statusDestino, String statusOrigem) {
        when(transferencias.buscarMesaDoPedido(PEDIDO_ID)).thenReturn(Optional.of(ORIGEM_ID));
        when(transferencias.bloquearMesas(ORIGEM_ID, DESTINO_ID)).thenReturn(List.of(
                new TransferenciaMesaRepository.MesaTransferencia(
                        ORIGEM_ID, 5, statusOrigem),
                new TransferenciaMesaRepository.MesaTransferencia(
                        DESTINO_ID, 8, statusDestino)));
    }

    private void prepararPedidoAberto() {
        prepararMesas("DISPONIVEL");
        when(transferencias.bloquearPedido(PEDIDO_ID)).thenReturn(Optional.of(
                new TransferenciaMesaRepository.PedidoTransferencia(
                        PEDIDO_ID, ORIGEM_ID, "EM_PREPARO")));
    }

    private void assertErro(ErrorCode esperado) {
        BusinessException erro = assertThrows(
                BusinessException.class,
                () -> service.transferirMesa(PEDIDO_ID, DESTINO_ID, "GARCOM-1"));
        assertEquals(esperado, erro.getErro());
    }
}