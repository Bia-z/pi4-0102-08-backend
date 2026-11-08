package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.ConfiguracaoRequest;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracoes;

    public ConfiguracaoService(ConfiguracaoRepository configuracoes) {
        this.configuracoes = configuracoes;
    }

    public ConfiguracaoResponse buscar() {
        return configuracoes.buscar();
    }

    public ConfiguracaoResponse atualizar(ConfiguracaoRequest request) {
        if (request == null || request.nomeRestaurante() == null
                || request.nomeRestaurante().isBlank() || request.nomeRestaurante().trim().length() > 120
                || request.taxaServicoPercentual() == null
                || request.taxaServicoPercentual().signum() < 0
                || request.taxaServicoPercentual().compareTo(new BigDecimal("100")) > 0
                || request.imprimirComandaAoAceitar() == null) {
            throw new BusinessException(ErrorCode.CONFIGURACAO_INVALIDA);
        }
        try {
            BigDecimal percentual = request.taxaServicoPercentual()
                    .setScale(2, RoundingMode.UNNECESSARY);
            return configuracoes.atualizar(
                    request.nomeRestaurante().trim(), percentual, request.imprimirComandaAoAceitar());
        } catch (ArithmeticException ex) {
            throw new BusinessException(ErrorCode.CONFIGURACAO_INVALIDA);
        }
    }
}