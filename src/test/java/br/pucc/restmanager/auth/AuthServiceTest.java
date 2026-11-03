package br.pucc.restmanager.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.auth.dto.LoginRequest;
import br.pucc.restmanager.auth.dto.LoginResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarios; // banco "de mentira": sem Postgres

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    JwtService jwt = new JwtService("segredo-de-teste-com-mais-de-32-caracteres-ok", 60);
    AuthService service;

    @BeforeEach
    void montar() {
        service = new AuthService(usuarios, encoder, jwt);
    }

    private Usuario usuario(boolean ativo) {
        return new Usuario("G001", "Lucas", "lucas@restmanager.com",
                encoder.encode("123456"), Perfil.GARCOM, ativo);
    }

    @Test
    void loginCorretoDevolveTokenComPerfil() {
        when(usuarios.findByCodigo("G001")).thenReturn(Optional.of(usuario(true)));

        LoginResponse resposta = service.autenticar(new LoginRequest("G001", "123456"));

        assertEquals("Bearer", resposta.tipoToken());
        assertEquals("garcom", resposta.perfil());
        assertEquals("garcom", jwt.extrairPerfil(resposta.token()));
        assertEquals("G001", jwt.extrairCodigo(resposta.token()));
    }

    @Test
    void senhaErradaRecusaComCredenciaisInvalidas() {
        when(usuarios.findByCodigo("G001")).thenReturn(Optional.of(usuario(true)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.autenticar(new LoginRequest("G001", "senha-errada")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, ex.getErro());
    }

    @Test
    void codigoInexistenteRecusaComCredenciaisInvalidas() {
        when(usuarios.findByCodigo("X999")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.autenticar(new LoginRequest("X999", "123456")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, ex.getErro());
    }

    @Test
    void usuarioInativoRecusaComUsuarioInativo() {
        when(usuarios.findByCodigo("G001")).thenReturn(Optional.of(usuario(false)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.autenticar(new LoginRequest("G001", "123456")));

        assertEquals(ErrorCode.USUARIO_INATIVO, ex.getErro());
    }
}