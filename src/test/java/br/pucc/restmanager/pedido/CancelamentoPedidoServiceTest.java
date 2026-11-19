package br.pucc.restmanager.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.CancelarPedidoRequest;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CancelamentoPedidoServiceTest {

    @Mock
    PedidoRepository pedidos;

    @Mock
    UsuarioRepository usuarios;

    @Mock
    PasswordEncoder passwordEncoder;

    private CancelamentoPedidoService service;
    private Pedido pedido;

    @BeforeEach
    void montar() {
        service = new CancelamentoPedidoService(pedidos, usuarios, passwordEncoder);
        Mesa mesa = new Mesa(7, 4);
        Usuario garcom = new Usuario("g1", "Garcom", null, "hash", Perfil.GARCOM, true);
        pedido = new Pedido(mesa, garcom, new BigDecimal("52.80"));
        ReflectionTestUtils.setField(pedido, "id", 100L);
        ReflectionTestUtils.setField(pedido, "status", StatusPedido.PAGO);
    }

    @Test
    void cancelaPedidoPagoComCredenciaisDeAdministrador() {
        when(usuarios.findByCodigo("admin")).thenReturn(Optional.of(usuario(Perfil.ADMIN, true)));
        when(passwordEncoder.matches("senha-admin", "hash-senha")).thenReturn(true);
        when(pedidos.findById(100L)).thenReturn(Optional.of(pedido));

        var resposta = service.cancelar(100L, new CancelarPedidoRequest("admin", "senha-admin"));

        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(100L, resposta.pedidoId());
        assertEquals("cancelado", resposta.statusPedido());
        verify(pedidos).save(pedido);
    }

    @Test
    void recusaSenhaIncorretaSemTocarNoPedido() {
        when(usuarios.findByCodigo("admin")).thenReturn(Optional.of(usuario(Perfil.ADMIN, true)));
        when(passwordEncoder.matches("errada", "hash-senha")).thenReturn(false);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cancelar(100L, new CancelarPedidoRequest("admin", "errada")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, erro.getErro());
        assertEquals(StatusPedido.PAGO, pedido.getStatus());
        verify(pedidos, never()).findById(any());
        verify(pedidos, never()).save(any());
    }

    @Test
    void naoAceitaCredenciaisDeQuemNaoEAdministrador() {
        when(usuarios.findByCodigo("caixa")).thenReturn(Optional.of(usuario(Perfil.CAIXA, true)));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cancelar(100L, new CancelarPedidoRequest("caixa", "senha-caixa")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, erro.getErro());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(pedidos, never()).save(any());
    }

    @Test
    void naoAceitaAdministradorInativo() {
        when(usuarios.findByCodigo("admin")).thenReturn(Optional.of(usuario(Perfil.ADMIN, false)));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cancelar(100L, new CancelarPedidoRequest("admin", "senha-admin")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, erro.getErro());
        verify(pedidos, never()).save(any());
    }

    @Test
    void exigeCodigoESenha() {
        for (CancelarPedidoRequest request : new CancelarPedidoRequest[] {
                null,
                new CancelarPedidoRequest(null, "senha-admin"),
                new CancelarPedidoRequest(" ", "senha-admin"),
                new CancelarPedidoRequest("admin", null),
                new CancelarPedidoRequest("admin", " ")}) {
            BusinessException erro = assertThrows(BusinessException.class,
                    () -> service.cancelar(100L, request));
            assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, erro.getErro());
        }
        verifyNoInteractions(usuarios, pedidos, passwordEncoder);
    }

    @Test
    void recusaPedidoQueAindaNaoFoiPago() {
        ReflectionTestUtils.setField(pedido, "status", StatusPedido.EM_PREPARO);
        administradorValido();
        when(pedidos.findById(100L)).thenReturn(Optional.of(pedido));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cancelar(100L, new CancelarPedidoRequest("admin", "senha-admin")));

        assertEquals(ErrorCode.PEDIDO_NAO_FECHADO, erro.getErro());
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
        verify(pedidos, never()).save(any());
    }

    @Test
    void recusaPedidoJaCancelado() {
        ReflectionTestUtils.setField(pedido, "status", StatusPedido.CANCELADO);
        administradorValido();
        when(pedidos.findById(100L)).thenReturn(Optional.of(pedido));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cancelar(100L, new CancelarPedidoRequest("admin", "senha-admin")));

        assertEquals(ErrorCode.PEDIDO_JA_CANCELADO, erro.getErro());
        verify(pedidos, never()).save(any());
    }

    @Test
    void informaPedidoInexistente() {
        administradorValido();
        when(pedidos.findById(999L)).thenReturn(Optional.empty());

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.cancelar(999L, new CancelarPedidoRequest("admin", "senha-admin")));

        assertEquals(ErrorCode.PEDIDO_NAO_ENCONTRADO, erro.getErro());
    }

    private void administradorValido() {
        when(usuarios.findByCodigo("admin")).thenReturn(Optional.of(usuario(Perfil.ADMIN, true)));
        when(passwordEncoder.matches("senha-admin", "hash-senha")).thenReturn(true);
    }

    private Usuario usuario(Perfil perfil, boolean ativo) {
        return new Usuario("codigo", "Nome", null, "hash-senha", perfil, ativo);
    }
}
