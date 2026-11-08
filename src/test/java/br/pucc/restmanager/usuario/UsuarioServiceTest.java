package br.pucc.restmanager.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import br.pucc.restmanager.usuario.dto.AtualizarUsuarioRequest;
import br.pucc.restmanager.usuario.dto.UsuarioResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);
        when(usuarios.existsByEmailIgnoreCase("carlos@restmanager.com")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = service.cadastrar(
                new CadastrarUsuarioRequest("Carlos Mendes", "carlos@restmanager.com", "529.982.247-25",
                        "11999999999", "FUNC001", "garcom", "senhaSegura123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertEquals("FUNC001", response.codigo());
        assertEquals("carlos@restmanager.com", salvo.getEmail());
        assertEquals("52998224725", salvo.getCpf());
        assertEquals("11999999999", salvo.getTelefone());
        assertEquals(Perfil.GARCOM, salvo.getPerfil());
        assertNotEquals("senhaSegura123", salvo.getSenha());
        assertTrue(encoder.matches("senhaSegura123", salvo.getSenha()));
    }

    @Test
    void recusaCodigoRepetido() {
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);
        when(usuarios.findByCodigo("FUNC001"))
                .thenReturn(Optional.of(new Usuario("FUNC001", "Outro", null, "hash", Perfil.GARCOM, true)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("FUNC001", "garcom")));

        assertEquals(ErrorCode.CODIGO_DUPLICADO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void recusaPerfilInvalido() {
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("FUNC001", "supervisor")));

        assertEquals(ErrorCode.PERFIL_INVALIDO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void geraCodigoQuandoNaoInformado() {
        when(usuarios.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = service.cadastrar(request(null, "garcom"));

        assertNotNull(response.codigo());
        assertEquals(20, response.codigo().length());
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertNull(captor.getValue().getEmail());
    }

    @Test
    void recusaCpfComDigitosVerificadoresInvalidos() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarUsuarioRequest(
                        "Carlos Mendes", null, "123.456.789-00", "11999999999",
                        "FUNC001", "garcom", "senhaSegura123")));

        assertEquals(ErrorCode.CPF_INVALIDO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void recusaCpfJaCadastrado() {
        when(usuarios.existsByCpf("52998224725")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(request("FUNC001", "garcom")));

        assertEquals(ErrorCode.CPF_DUPLICADO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void recusaTelefoneInvalido() {
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarUsuarioRequest(
                        "Carlos Mendes", null, "529.982.247-25", "12345",
                        "FUNC001", "garcom", "senhaSegura123")));

        assertEquals(ErrorCode.TELEFONE_INVALIDO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void recusaEmailInvalido() {
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarUsuarioRequest(
                        "Carlos Mendes", "email-invalido", "529.982.247-25", "11999999999",
                        "FUNC001", "garcom", "senhaSegura123")));

        assertEquals(ErrorCode.EMAIL_INVALIDO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void recusaEmailJaCadastradoIgnorandoMaiusculas() {
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);
        when(usuarios.existsByEmailIgnoreCase("carlos@restmanager.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarUsuarioRequest(
                        "Carlos Mendes", "Carlos@RestManager.com", "529.982.247-25",
                        "11999999999", "FUNC001", "garcom", "senhaSegura123")));

        assertEquals(ErrorCode.EMAIL_DUPLICADO, exception.getErro());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void atualizaDadosDoFuncionarioMantendoSenhaQuandoNaoInformada() {
        Usuario usuario = new Usuario(
                "FUNC001", "Carlos Antigo", null, "52998224725", "11999999999",
                "hash-atual", Perfil.GARCOM, true);
        ReflectionTestUtils.setField(usuario, "id", 1L);
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = service.atualizar(1L, new AtualizarUsuarioRequest(
                "FUNC002", "Carlos Mendes", null, "529.982.247-25", "11988887777",
                "caixa", true, ""));

        assertEquals("FUNC002", response.codigo());
        assertEquals("Carlos Mendes", usuario.getNome());
        assertEquals("11988887777", usuario.getTelefone());
        assertEquals(Perfil.CAIXA, usuario.getPerfil());
        assertEquals("hash-atual", usuario.getSenha());
        assertTrue(usuario.isAtivo());
    }

    @Test
    void naoPermiteDesativarPropriaConta() {
        Usuario administrador = new Usuario(
                "ADMIN1", "Admin", null, "hash", Perfil.ADMIN, true);
        ReflectionTestUtils.setField(administrador, "id", 1L);
        when(usuarios.findById(1L)).thenReturn(Optional.of(administrador));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.desativar(1L, "ADMIN1"));

        assertEquals(ErrorCode.ADMINISTRADOR_NAO_PODE_SE_DESATIVAR, exception.getErro());
        assertTrue(administrador.isAtivo());
    }

    private CadastrarUsuarioRequest request(String codigo, String perfil) {
        return new CadastrarUsuarioRequest("Carlos Mendes", null, "529.982.247-25",
                "11999999999", codigo, perfil, "senhaSegura123");
    }
}