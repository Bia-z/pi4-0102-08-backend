package br.pucc.restmanager.caixa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.caixa.dto.AbrirCaixaRequest;
import br.pucc.restmanager.caixa.dto.CaixaResponse;
import br.pucc.restmanager.caixa.dto.FecharCaixaRequest;
import br.pucc.restmanager.caixa.dto.MovimentacaoRequest;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaixaServiceTest {

    @Mock
    private CaixaRepository repository;

    @Test
    void abreCaixaComValorEOperadorInformados() {
        CaixaResponse caixa = caixa("ABERTO", new BigDecimal("100.00"), null, null);
        when(repository.caixaAberto()).thenReturn(Optional.empty());
        when(repository.abrir("caixa1", "NOITE", new BigDecimal("100.00"))).thenReturn(caixa);

        CaixaResponse resultado = new CaixaService(repository).abrir(
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
                () -> new CaixaService(repository).abrir(
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

        var resumo = new CaixaService(repository).fechar(
                new FecharCaixaRequest(new BigDecimal("150")));

        assertEquals(new BigDecimal("140.00"), resumo.valorEsperado());
        assertEquals(new BigDecimal("10.00"), resumo.diferenca());
    }

    @Test
    void rejeitaValoresMonetariosComMaisDeDuasCasas() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> new CaixaService(repository).abrir(
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
                () -> new CaixaService(repository).movimentar(
                        new MovimentacaoRequest("SANGRIA", new BigDecimal("100.01"), "retirada"),
                        "caixa1"));

        assertEquals(ErrorCode.MOVIMENTACAO_INVALIDA, exception.getErro());
    }

    private CaixaResponse caixa(
            String status, BigDecimal abertura, BigDecimal fechamento, BigDecimal esperado) {
        return new CaixaResponse(1L, 2L, "NOITE", status, abertura,
                LocalDateTime.now(), fechamento, esperado, null);
    }
}