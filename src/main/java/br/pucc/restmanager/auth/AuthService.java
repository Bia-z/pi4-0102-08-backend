package br.pucc.restmanager.auth;

import br.pucc.restmanager.auth.dto.LoginRequest;
import br.pucc.restmanager.auth.dto.LoginResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public LoginResponse autenticar(LoginRequest req) {
        // Código inexistente e senha errada dão o MESMO erro: não revela qual dos dois falhou
        Usuario usuario = usuarios.findByCodigo(req.codigo())
                .orElseThrow(() -> new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS));

        if (!encoder.matches(req.senha(), usuario.getSenha())) {
            throw new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS);
        }

        // Só depois da senha certa: quem não sabe a senha não descobre se a conta está inativa
        if (!usuario.isAtivo()) {
            throw new BusinessException(ErrorCode.USUARIO_INATIVO);
        }

        String token = jwt.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", usuario.getPerfil().name().toLowerCase(Locale.ROOT));
    }
}