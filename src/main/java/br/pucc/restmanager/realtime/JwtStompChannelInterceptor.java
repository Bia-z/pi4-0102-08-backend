package br.pucc.restmanager.realtime;

import br.pucc.restmanager.auth.JwtService;
import br.pucc.restmanager.usuario.domain.Perfil;
import io.jsonwebtoken.JwtException;
import java.util.List;
import java.util.Locale;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtStompChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new AccessDeniedException("Cabeçalho STOMP ausente");
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(authenticate(accessor));
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (!podeAssinar(accessor, accessor.getDestination())) {
                throw new AccessDeniedException("Inscrição STOMP não autorizada");
            }
        }
        return message;
    }

    // /topic/mesas (CT21) e /topic/pedidos (CT11) são para qualquer usuário autenticado:
    // Garçom, Cozinha e Caixa acompanham os dois. /topic/cozinha (CT9) e /topic/comandas
    // continuam restritos à Cozinha e ao Administrador.
    private boolean podeAssinar(StompHeaderAccessor accessor, String destination) {
        if (accessor.getUser() == null || destination == null) {
            return false;
        }
        return switch (destination) {
            case "/topic/mesas", "/topic/pedidos" -> true;
            case "/topic/cozinha", "/topic/comandas" -> podeAcessarCozinha(accessor);
            default -> false;
        };
    }

    private boolean podeAcessarCozinha(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority)
                        || "ROLE_COZINHA".equals(authority));
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.regionMatches(
                true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new AccessDeniedException("Token ausente");
        }

        try {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            String codigo = jwtService.extrairCodigo(token);
            String perfilToken = jwtService.extrairPerfil(token);
            if (codigo == null || codigo.isBlank() || perfilToken == null || perfilToken.isBlank()) {
                throw new IllegalArgumentException("Claims obrigatórias ausentes");
            }
            Perfil perfil = Perfil.valueOf(perfilToken.toUpperCase(Locale.ROOT));
            return UsernamePasswordAuthenticationToken.authenticated(
                    codigo,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AccessDeniedException("Token inválido", ex);
        }
    }
}
