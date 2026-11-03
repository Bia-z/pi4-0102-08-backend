package br.pucc.restmanager.auth;

import br.pucc.restmanager.usuario.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-minutos}") long minutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = minutos * 60 * 1000;
    }

    // O token carrega o código (subject) e o perfil em minúsculas, como na ficha
    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(usuario.getCodigo())
                .claim("perfil", usuario.getPerfil().name().toLowerCase())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    public String extrairCodigo(String token) {
        return lerClaims(token).getSubject();
    }

    public String extrairPerfil(String token) {
        return lerClaims(token).get("perfil", String.class);
    }

    private Claims lerClaims(String token) {
        return Jwts.parser().verifyWith(chave).build()
                .parseSignedClaims(token).getPayload();
    }
}