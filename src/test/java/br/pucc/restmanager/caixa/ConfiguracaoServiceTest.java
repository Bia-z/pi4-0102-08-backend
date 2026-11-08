package br.pucc.restmanager.caixa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.caixa.dto.ConfiguracaoRequest;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {

    @Mock
    private ConfiguracaoRepository repository;

    @Test
    void atualizaTaxaDeServicoDentroDoIntervaloPermitido() {
        var request = new ConfiguracaoRequest("Restaurante", new BigDecimal("12.50"), true);
        var response = new ConfiguracaoResponse("Restaurante", new BigDecimal("12.50"), true);
        when(repository.atualizar("Restaurante", new BigDecimal("12.50"), true)).thenReturn(response);

        assertEquals(response, new ConfiguracaoService(repository).atualizar(request));
    }

    @Test
    void recusaTaxaAcimaDeCemPorCento() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> new ConfiguracaoService(repository).atualizar(
                        new ConfiguracaoRequest("Restaurante", new BigDecimal("100.01"), true)));

        assertEquals(ErrorCode.CONFIGURACAO_INVALIDA, exception.getErro());
        verify(repository, org.mockito.Mockito.never())
                .atualizar(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean());
    }
}
