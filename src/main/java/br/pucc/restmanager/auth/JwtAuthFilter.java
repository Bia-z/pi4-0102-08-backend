package br.pucc.restmanager.auth;

import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(
            JwtService jwtService,
            AuthenticationEntryPoint authenticationEntryPoint,
            UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.usuarios = usuarios;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        UsernamePasswordAuthenticationToken authentication;
        try {
            if (token.isEmpty()) {
                throw new IllegalArgumentException("Token vazio");
            }

            String codigo = jwtService.extrairCodigo(token);
            String perfilToken = jwtService.extrairPerfil(token);
            if (codigo == null || codigo.isBlank() || perfilToken == null || perfilToken.isBlank()) {
                throw new IllegalArgumentException("Claims obrigatórias ausentes");
            }

            Perfil perfil = Perfil.valueOf(perfilToken.toUpperCase(Locale.ROOT));

            usuarios.findByCodigo(codigo)
                    .filter(usuario -> usuario.isAtivo() && usuario.getPerfil() == perfil)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Usuário inexistente, inativo ou com perfil alterado"));

            authentication = UsernamePasswordAuthenticationToken.authenticated(
                    codigo,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request, response, new BadCredentialsException("Token inválido", ex));
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}