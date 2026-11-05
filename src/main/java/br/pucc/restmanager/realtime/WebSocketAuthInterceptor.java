package br.pucc.restmanager.realtime;

import br.pucc.restmanager.auth.JwtService;
import io.jsonwebtoken.JwtException;
import java.util.List;
import java.util.Locale;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(autenticar(accessor.getFirstNativeHeader("Authorization")));
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken autenticar(String header) {
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new MessageDeliveryException("TOKEN_AUSENTE");
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            String codigo = jwtService.extrairCodigo(token);
            String perfil = jwtService.extrairPerfil(token).toUpperCase(Locale.ROOT);
            return UsernamePasswordAuthenticationToken.authenticated(
                    codigo, null, List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
        } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
            throw new MessageDeliveryException("TOKEN_INVALIDO");
        }
    }
}
