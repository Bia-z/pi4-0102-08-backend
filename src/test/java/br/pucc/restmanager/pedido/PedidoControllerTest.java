package br.pucc.restmanager.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.AtualizarStatusItemRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

class PedidoControllerTest {

    private final PedidoService service = mock(PedidoService.class);
    private final PedidoController controller = new PedidoController(service);

    @Test
    void retornaErroDaFichaQuandoFiltroDeStatusDoPedidoEInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.listarPedidos(
                        "desconhecido", null, null, null, authentication("ROLE_ADMIN")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(service, never()).listarPedidos(null, null, null, null, false);
    }

    @Test
    void retornaErroDaFichaQuandoFiltroDeStatusDoItemEInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.listarPedidos(
                        null, "desconhecido", null, null, authentication("ROLE_ADMIN")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(service, never()).listarPedidos(null, null, null, null, false);
    }

    @Test
    void aceitaStatusEmMinusculasEIdentificaPerfilDaCozinha() {
        Authentication authentication = authentication("ROLE_COZINHA");

        controller.listarPedidos(
                "em_preparo", "aguardando", 2L, 5L, authentication);

        verify(service).listarPedidos(
                StatusPedido.EM_PREPARO,
                StatusItem.AGUARDANDO,
                2L,
                5L,
                true);
    }

    @Test
    void exigeMesaIdNaListagemDoCaixa() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.listarPedidos(null, null, null, null, authentication("ROLE_CAIXA")));

        assertEquals(ErrorCode.ACESSO_NEGADO, exception.getErro());
        verify(service, never()).listarPedidos(null, null, null, null, false);
    }

    @Test
    void converteStatusDoItemAntesDeDelegarAtualizacao() {
        controller.atualizarStatusPedido(
                100L, 200L, new AtualizarStatusItemRequest("em_preparo"));

        verify(service).atualizarStatusPedido(100L, 200L, StatusItem.EM_PREPARO);
    }

    @Test
    void recusaStatusDesconhecidoAoAtualizarItem() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.atualizarStatusPedido(
                        100L, 200L, new AtualizarStatusItemRequest("desconhecido")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(service, never()).atualizarStatusPedido(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any());
    }

    private Authentication authentication(String role) {
        Authentication authentication = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority(role)))
                .when(authentication).getAuthorities();
        return authentication;
    }
}
