package br.pucc.restmanager.realtime;

import br.pucc.restmanager.mesa.PagamentoEmAndamentoMonitor;
import java.security.Principal;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class SessaoWebSocketListener {

    private final PagamentoEmAndamentoMonitor monitor;

    public SessaoWebSocketListener(PagamentoEmAndamentoMonitor monitor) {
        this.monitor = monitor;
    }

    @EventListener
    public void conectado(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal usuario = event.getUser();
        monitor.conectado(
                usuario == null ? null : usuario.getName(),
                accessor.getSessionId());
    }

    @EventListener
    public void desconectado(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal usuario = event.getUser();
        monitor.desconectado(
                usuario == null ? null : usuario.getName(),
                accessor.getSessionId());
    }
}