package br.pucc.restmanager.caixa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.caixa.dto.AbrirCaixaRequest;
import br.pucc.restmanager.caixa.dto.CaixaResponse;
import br.pucc.restmanager.caixa.dto.FecharCaixaRequest;
import br.pucc.restmanager.caixa.dto.MovimentacaoRequest;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CaixaServiceTest {

    @Mock
    private CaixaRepository repository;

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void abreCaixaComValorEOperadorInformados() {
        CaixaResponse caixa = caixa("ABERTO", new BigDecimal("100.00"), null, null);
        when(repository.caixaAberto()).thenReturn(Optional.empty());
        when(repository.abrir("caixa1", "NOITE", new BigDecimal("100.00"))).thenReturn(caixa);

        CaixaResponse resultado = service().abrir(
                new AbrirCaixaRequest(new BigDecimal("100"), "noite"), "caixa1");

        assertEquals(caixa, resultado);
        verify(repository).abrir("caixa1", "NOITE", new BigDecimal("100.00"));
    }

    @Test
    void naoPermiteAbrirSegundoCaixa() {
        when(repository.caixaAberto()).thenReturn(Optional.of(
                caixa("ABERTO", BigDecimal.ZERO, null, null)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().abrir(
                        new AbrirCaixaRequest(BigDecimal.ZERO, "MANHA"), "caixa2"));

        assertEquals(ErrorCode.CAIXA_JA_ABERTO, exception.getErro());
    }

    @Test
    void fechamentoComparaValorContadoAoEsperado() {
        CaixaResponse aberto = caixa("ABERTO", new BigDecimal("100.00"), null, null);
        CaixaResponse fechado = caixa(
                "FECHADO", new BigDecimal("100.00"),
                new BigDecimal("150.00"), new BigDecimal("140.00"));
        when(repository.caixaAbertoComBloqueio()).thenReturn(Optional.of(aberto));
        when(repository.calcularValorEsperado(1L)).thenReturn(new BigDecimal("140.00"));
        when(repository.fechar(1L, new BigDecimal("150.00"), new BigDecimal("140.00")))
                .thenReturn(Optional.of(fechado));
        when(usuarios.findByCodigo("caixaConfirma")).thenReturn(Optional.of(usuario(Perfil.CAIXA)));
        when(passwordEncoder.matches("senha-segura", "hash-senha")).thenReturn(true);

        var resumo = service().fechar(
                new FecharCaixaRequest(new BigDecimal("150"), "caixaConfirma", "senha-segura"));

        assertEquals(new BigDecimal("140.00"), resumo.valorEsperado());
        assertEquals(new BigDecimal("10.00"), resumo.diferenca());
    }

    @Test
    void rejeitaValoresMonetariosComMaisDeDuasCasas() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().abrir(
                        new AbrirCaixaRequest(new BigDecimal("1.001"), null), "caixa1"));

        assertEquals(ErrorCode.VALOR_INVALIDO, exception.getErro());
    }

    @Test
    void naoPermiteSangriaAcimaDoSaldoEsperado() {
        when(repository.caixaAbertoComBloqueio()).thenReturn(Optional.of(
                caixa("ABERTO", new BigDecimal("100.00"), null, null)));
        when(repository.calcularValorEsperado(1L)).thenReturn(new BigDecimal("100.00"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().movimentar(
                        new MovimentacaoRequest("SANGRIA", new BigDecimal("100.01"), "retirada"),
                        "caixa1"));

        assertEquals(ErrorCode.SALDO_CAIXA_INSUFICIENTE, exception.getErro());
        verify(repository, never()).movimentar(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void permiteSangriaIgualAoSaldoDisponivel() {
        when(repository.caixaAbertoComBloqueio()).thenReturn(Optional.of(
                caixa("ABERTO", new BigDecimal("100.00"), null, null)));
        when(repository.calcularValorEsperado(1L)).thenReturn(new BigDecimal("100.00"));
        when(repository.idOperador("caixa1")).thenReturn(2L);

        service().movimentar(
                new MovimentacaoRequest("SANGRIA", new BigDecimal("100.00"), "retirada"),
                "caixa1");

        verify(repository).movimentar(
                1L, 2L, "SANGRIA", new BigDecimal("100.00"), "retirada");
    }

    @Test
    void naoFechaCaixaQuandoSenhaInformadaEstaIncorreta() {
        when(usuarios.findByCodigo("adminConfirma")).thenReturn(Optional.of(usuario(Perfil.ADMIN)));
        when(passwordEncoder.matches("senha-errada", "hash-senha")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().fechar(
                        new FecharCaixaRequest(
                                new BigDecimal("150"), "adminConfirma", "senha-errada")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, exception.getErro());
        verify(repository, never()).caixaAbertoComBloqueio();
        verify(repository, never()).fechar(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void exigeSenhaParaFecharCaixa() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().fechar(
                        new FecharCaixaRequest(new BigDecimal("150"), "caixaConfirma", " ")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, exception.getErro());
        verify(usuarios, never()).findByCodigo("caixaConfirma");
    }

    @Test
    void naoAceitaCredenciaisDeUsuarioComOutroPerfil() {
        when(usuarios.findByCodigo("garcom")).thenReturn(Optional.of(usuario(Perfil.GARCOM)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service().fechar(
                        new FecharCaixaRequest(new BigDecimal("150"), "garcom", "senha-valida")));

        assertEquals(ErrorCode.CREDENCIAIS_INVALIDAS, exception.getErro());
        verify(passwordEncoder, never()).matches("senha-valida", "hash-senha");
        verify(repository, never()).caixaAbertoComBloqueio();
    }

    @Test
    void aceitaConfirmacaoComCredenciaisDeAdministrador() {
        CaixaResponse aberto = caixa("ABERTO", new BigDecimal("100.00"), null, null);
        CaixaResponse fechado = caixa(
                "FECHADO", new BigDecimal("100.00"),
                new BigDecimal("150.00"), new BigDecimal("140.00"));
        when(usuarios.findByCodigo("admin")).thenReturn(Optional.of(usuario(Perfil.ADMIN)));
        when(passwordEncoder.matches("senha-admin", "hash-senha")).thenReturn(true);
        when(repository.caixaAbertoComBloqueio()).thenReturn(Optional.of(aberto));
        when(repository.calcularValorEsperado(1L)).thenReturn(new BigDecimal("140.00"));
        when(repository.fechar(1L, new BigDecimal("150.00"), new BigDecimal("140.00")))
                .thenReturn(Optional.of(fechado));

        service().fechar(new FecharCaixaRequest(
                new BigDecimal("150"), "admin", "senha-admin"));

        verify(repository).fechar(1L, new BigDecimal("150.00"), new BigDecimal("140.00"));
    }

    private CaixaService service() {
        return new CaixaService(repository, usuarios, passwordEncoder);
    }

    private Usuario usuario(Perfil perfil) {
        return new Usuario("confirmador", "Operador", null, "hash-senha", perfil, true);
    }

    private CaixaResponse caixa(
            String status, BigDecimal abertura, BigDecimal fechamento, BigDecimal esperado) {
        return new CaixaResponse(1L, 2L, "NOITE", status, abertura,
                LocalDateTime.now(), fechamento, esperado, null);
    }
}