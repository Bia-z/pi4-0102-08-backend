package br.pucc.restmanager.mesa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.pedido.PedidoRepository;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MesaPagamentoServiceTest {

    @Mock
    private MesaRepository mesas;

    @Mock
    private PedidoRepository pedidos;

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private StatusMesaPublisher publisher;

    private MesaPagamentoService service;
    private Mesa mesa;
    private Usuario operador;

    @BeforeEach
    void setUp() {
        service = new MesaPagamentoService(mesas, pedidos, usuarios, publisher);
        mesa = new Mesa(10, 4);
        ReflectionTestUtils.setField(mesa, "id", 5L);
        ReflectionTestUtils.setField(mesa, "status", StatusMesa.OCUPADA);
        operador = new Usuario("cx1", "Caixa", null, "hash", Perfil.CAIXA, true);
        ReflectionTestUtils.setField(operador, "id", 8L);
        when(mesas.buscarAtivaPorIdComBloqueio(5L)).thenReturn(Optional.of(mesa));
    }

    @Test
    void iniciaPagamentoComMesaOcupadaEPublicaStatus() {
        when(pedidos.listarAbertos(null, null, null, 5L, false))
                .thenReturn(List.of(org.mockito.Mockito.mock(Pedido.class)));
        when(usuarios.findByCodigo("cx1")).thenReturn(Optional.of(operador));

        var resposta = service.iniciarPagamento(5L, "cx1");

        assertEquals(5L, resposta.mesaId());
        assertEquals(10, resposta.numero());
        assertEquals("em_pagamento", resposta.status());
        assertEquals(8L, resposta.iniciadoPor());
        assertEquals(StatusMesa.EM_PAGAMENTO, mesa.getStatus());
        assertEquals(operador, mesa.getPagamentoIniciadoPor());
        verify(publisher).publicarStatusAtualizado(5L, 10, "em_pagamento");
    }

    @Test
    void recusaIniciarPagamentoQuandoMesaJaEstaEmPagamento() {
        ReflectionTestUtils.setField(mesa, "status", StatusMesa.EM_PAGAMENTO);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.iniciarPagamento(5L, "cx1"));

        assertEquals(ErrorCode.MESA_JA_EM_PAGAMENTO, exception.getErro());
        verify(pedidos, never()).listarAbertos(null, null, null, 5L, false);
        verify(publisher, never()).publicarStatusAtualizado(5L, 10, "em_pagamento");
    }

    @Test
    void cancelaPagamentoDoOperadorResponsavelEDevolveMesaParaOcupada() {
        ReflectionTestUtils.setField(mesa, "status", StatusMesa.EM_PAGAMENTO);
        ReflectionTestUtils.setField(mesa, "pagamentoIniciadoPor", operador);
        when(usuarios.findByCodigo("cx1")).thenReturn(Optional.of(operador));

        var resposta = service.cancelarPagamento(5L, "cx1");

        assertEquals(5L, resposta.mesaId());
        assertEquals("ocupada", resposta.status());
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
        assertEquals(null, mesa.getPagamentoIniciadoPor());
        verify(publisher).publicarStatusAtualizado(5L, 10, "ocupada");
    }

    @Test
    void recusaCancelamentoPorOperadorDiferente() {
        ReflectionTestUtils.setField(mesa, "status", StatusMesa.EM_PAGAMENTO);
        ReflectionTestUtils.setField(mesa, "pagamentoIniciadoPor", operador);
        Usuario outroOperador = new Usuario("cx2", "Outro caixa", null, "hash", Perfil.CAIXA, true);
        ReflectionTestUtils.setField(outroOperador, "id", 9L);
        when(usuarios.findByCodigo("cx2")).thenReturn(Optional.of(outroOperador));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.cancelarPagamento(5L, "cx2"));

        assertEquals(ErrorCode.ACESSO_NEGADO, exception.getErro());
        assertEquals(StatusMesa.EM_PAGAMENTO, mesa.getStatus());
        verify(publisher, never()).publicarStatusAtualizado(5L, 10, "ocupada");
    }
}