package br.pucc.restmanager.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import br.pucc.restmanager.usuario.dto.CadastrarUsuarioRequest;
import br.pucc.restmanager.usuario.dto.UsuarioResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarios;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    CodigoFuncionarioGenerator codigoGenerator;
    UsuarioService service;

    @BeforeEach
    void montar() {
        codigoGenerator = new CodigoFuncionarioGenerator(usuarios);
        service = new UsuarioService(usuarios, encoder, codigoGenerator);
    }

    @Test
    void cadastraUsuarioComSenhaEmBcrypt() {
        when(usuarios.findByCodigo("FUNC001")).thenReturn(Optional.empty());
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = service.cadastrar(
                new CadastrarUsuarioRequest("Carlos Mendes", "123.456.789-00",
                        "11999999999", "FUNC001", "garcom", "senhaSegura123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertEquals("FUNC001", response.codigo());
        assertEquals("123.456.789-00", salvo.getCpf());
        assertEquals("11999999999", salvo.getTelefone());
        assertEquals(Perfil.GARCOM, salvo.getPerfil());
        assertNotEquals("senhaSegura123", salvo.getSenha());
        assertTrue(encoder.matches("senhaSegura123", salvo.getSenha()));
    }

    @Test
    void recusaCodigoRepetido() {
        when(usuarios.findByCodigo("FUNC001"))
                .thenReturn(Optional.of(new Usuario("FUNC001", "Outro", null, "hash", Perfil.GARCOM, true)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("FUNC001", "garcom")));

        assertEquals(ErrorCode.CODIGO_DUPLICADO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void recusaPerfilInvalido() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("FUNC001", "supervisor")));

        assertEquals(ErrorCode.PERFIL_INVALIDO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void geraCodigoQuandoNaoInformado() {
        when(usuarios.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = service.cadastrar(request(null, "garcom"));

        assertNotNull(response.codigo());
        assertEquals(20, response.codigo().length());
        verify(usuarios).save(any(Usuario.class));
    }

    private CadastrarUsuarioRequest request(String codigo, String perfil) {
        return new CadastrarUsuarioRequest("Carlos Mendes", "123.456.789-00",
                "11999999999", codigo, perfil, "senhaSegura123");
    }
}