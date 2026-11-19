package br.pucc.restmanager.caixa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.caixa.dto.ConfiguracaoRequest;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {

    @Mock
    private ConfiguracaoRepository repository;

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void atualizaTaxaDeServicoDentroDoIntervaloPermitido() {
        var request = requestComSenha("12.50", "caixaConfirma", "senha-segura");
        var response = new ConfiguracaoResponse(
                "Restaurante", new BigDecimal("12.50"), false, true, false, true);
        when(usuarios.findByCodigo("caixaConfirma")).thenReturn(Optional.of(usuario(Perfil.CAIXA, true)));
        when(passwordEncoder.matches("senha-segura", "hash-senha")).thenReturn(true);
        when(repository.atualizar("Restaurante", new BigDecimal("12.50"), false, true, false, true))
                .thenReturn(response);

        assertEquals(response, service().atualizar(request));
    }

    @Test
    void administradorTambemPodeAutorizarAAlteracao() {
        var request = requestComSenha("10.00", "admin", "senha-admin");
        var response = new ConfiguracaoResponse(
                "Restaurante", new BigDecimal("10.00"), false, true, false, true);
        when(usuarios.findByCodigo("admin")).thenReturn(Optional.of(usuario(Perfil.ADMIN, true)));
        when(passwordEncoder.matches("senha-admin", "hash-senha")).thenReturn(true);
        when(repository.atualizar("Restaurante", new BigDecimal("10.00"), false, true, false, true))
                .thenReturn(response);

        assertEquals(response, service().atualizar(request));
    }

    @Test
    void recusaSenhaIncorretaSemAlterarAConfiguracao() {
        when(usuarios.findByCodigo("caixaConfirma")).thenReturn(Optional.of(usuario(Perfil.CAIXA, true)));
        when(passwordEncoder.matches("errada", "hash-senha")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().atualizar(requestComSenha("12.50", "caixaConfirma", "errada")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, exception.getErro());
        nuncaAtualiza();
    }

    @Test
    void recusaCodigoOuSenhaEmBranco() {
        BusinessException semSenha = assertThrows(
                BusinessException.class,
                () -> service().atualizar(requestComSenha("12.50", "caixaConfirma", " ")));
        BusinessException semCodigo = assertThrows(
                BusinessException.class,
                () -> service().atualizar(requestComSenha("12.50", null, "senha-segura")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, semSenha.getErro());
        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, semCodigo.getErro());
        verify(usuarios, never()).findByCodigo(ArgumentMatchers.anyString());
        nuncaAtualiza();
    }

    @Test
    void naoAceitaCredenciaisDePerfilSemPermissaoOuUsuarioInativo() {
        when(usuarios.findByCodigo("garcom")).thenReturn(Optional.of(usuario(Perfil.GARCOM, true)));
        when(usuarios.findByCodigo("caixaInativo")).thenReturn(Optional.of(usuario(Perfil.CAIXA, false)));

        BusinessException garcom = assertThrows(
                BusinessException.class,
                () -> service().atualizar(requestComSenha("12.50", "garcom", "senha-valida")));
        BusinessException inativo = assertThrows(
                BusinessException.class,
                () -> service().atualizar(requestComSenha("12.50", "caixaInativo", "senha-valida")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, garcom.getErro());
        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, inativo.getErro());
        verify(passwordEncoder, never()).matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        nuncaAtualiza();
    }

    @Test
    void recusaTaxaAcimaDeCemPorCento() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().atualizar(
                        new ConfiguracaoRequest(
                                "Restaurante", new BigDecimal("100.01"), true, true, true, false,
                                "caixaConfirma", "senha-segura")));

        assertEquals(ErrorCode.CONFIGURACAO_INVALIDA, exception.getErro());
        nuncaAtualiza();
    }

    @Test
    void exigeTodasAsOpcoesDeImpressao() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().atualizar(
                        new ConfiguracaoRequest(
                                "Restaurante", new BigDecimal("10.00"), true, true, null, false,
                                "caixaConfirma", "senha-segura")));

        assertEquals(ErrorCode.CONFIGURACAO_INVALIDA, exception.getErro());
    }

    private ConfiguracaoService service() {
        return new ConfiguracaoService(repository, usuarios, passwordEncoder);
    }

    private ConfiguracaoRequest requestComSenha(String taxa, String codigo, String senha) {
        return new ConfiguracaoRequest(
                "Restaurante", new BigDecimal(taxa), false, true, false, true, codigo, senha);
    }

    private Usuario usuario(Perfil perfil, boolean ativo) {
        return new Usuario("confirmador", "Operador", null, "hash-senha", perfil, ativo);
    }

    private void nuncaAtualiza() {
        verify(repository, never())
                .atualizar(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
                        ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean());
    }
}
