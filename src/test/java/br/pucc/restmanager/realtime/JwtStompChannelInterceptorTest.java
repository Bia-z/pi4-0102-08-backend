package br.pucc.restmanager.realtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import br.pucc.restmanager.auth.JwtService;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

class JwtStompChannelInterceptorTest {

    private final JwtService jwt = new JwtService("segredo-de-teste-com-mais-de-32-caracteres-ok", 60);
    private final JwtStompChannelInterceptor interceptor = new JwtStompChannelInterceptor(jwt);
    private final MessageChannel canal = mock(MessageChannel.class);

    private Principal conectar(Perfil perfil) {
        Usuario usuario = new Usuario("U001", "Teste", null, "hash", perfil, true);
        StompHeaderAccessor connect = StompHeaderAccessor.create(StompCommand.CONNECT);
        connect.addNativeHeader("Authorization", "Bearer " + jwt.gerarToken(usuario));
        connect.setLeaveMutable(true);
        interceptor.preSend(mensagem(connect), canal);
        return connect.getUser();
    }

    private void assinar(Principal usuario, String destino) {
        StompHeaderAccessor subscribe = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        subscribe.setDestination(destino);
        subscribe.setSubscriptionId("sub-0");
        subscribe.setUser(usuario);
        subscribe.setLeaveMutable(true);
        interceptor.preSend(mensagem(subscribe), canal);
    }

    private Message<byte[]> mensagem(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void garcomAssinaPedidosEMesas() {
        Principal garcom = conectar(Perfil.GARCOM);

        assertDoesNotThrow(() -> assinar(garcom, "/topic/pedidos"));
        assertDoesNotThrow(() -> assinar(garcom, "/topic/mesas"));
    }

    @Test
    void cozinhaECaixaAssinamPedidos() {
        assertDoesNotThrow(() -> assinar(conectar(Perfil.COZINHA), "/topic/pedidos"));
        assertDoesNotThrow(() -> assinar(conectar(Perfil.CAIXA), "/topic/pedidos"));
    }

    @Test
    void garcomNaoAssinaCanaisDaCozinha() {
        Principal garcom = conectar(Perfil.GARCOM);

        assertThrows(AccessDeniedException.class, () -> assinar(garcom, "/topic/cozinha"));
        assertThrows(AccessDeniedException.class, () -> assinar(garcom, "/topic/comandas"));
    }

    @Test
    void cozinhaAssinaCanaisDaCozinha() {
        Principal cozinha = conectar(Perfil.COZINHA);

        assertDoesNotThrow(() -> assinar(cozinha, "/topic/cozinha"));
        assertDoesNotThrow(() -> assinar(cozinha, "/topic/comandas"));
    }

    @Test
    void canalDesconhecidoERecusado() {
        assertThrows(AccessDeniedException.class,
                () -> assinar(conectar(Perfil.ADMIN), "/topic/qualquer-coisa"));
    }

    @Test
    void assinaturaSemUsuarioAutenticadoERecusada() {
        assertThrows(AccessDeniedException.class, () -> assinar(null, "/topic/pedidos"));
    }

    @Test
    void conexaoSemTokenERecusada() {
        StompHeaderAccessor connect = StompHeaderAccessor.create(StompCommand.CONNECT);
        connect.setLeaveMutable(true);

        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(mensagem(connect), canal));
    }
}
