package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.AbrirCaixaRequest;
import br.pucc.restmanager.caixa.dto.CaixaResponse;
import br.pucc.restmanager.caixa.dto.FecharCaixaRequest;
import br.pucc.restmanager.caixa.dto.FechamentoResumoResponse;
import br.pucc.restmanager.caixa.dto.HistoricoFechamentoResponse;
import br.pucc.restmanager.caixa.dto.MovimentacaoRequest;
import br.pucc.restmanager.caixa.dto.MovimentacaoResponse;
import br.pucc.restmanager.caixa.domain.TipoMovimentacao;
import br.pucc.restmanager.caixa.domain.Turno;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaixaService {

    private final CaixaRepository caixas;

    public CaixaService(CaixaRepository caixas) {
        this.caixas = caixas;
    }

    @Transactional
    public CaixaResponse abrir(AbrirCaixaRequest request, String codigoOperador) {
        if (request == null || request.valorAbertura() == null
                || request.valorAbertura().signum() < 0) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
        BigDecimal abertura = moeda(request.valorAbertura());
        String turno = null;
        if (request.turno() != null && !request.turno().isBlank()) {
            try {
                turno = Turno.valueOf(request.turno().trim().toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ErrorCode.TURNO_INVALIDO);
            }
        }
        if (caixas.caixaAberto().isPresent()) {
            throw new BusinessException(ErrorCode.CAIXA_JA_ABERTO);
        }
        try {
            return caixas.abrir(codigoOperador, turno, abertura);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CAIXA_JA_ABERTO);
        }
    }

    public CaixaResponse obterAberto() {
        return caixas.caixaAberto()
                .orElseThrow(() -> new BusinessException(ErrorCode.CAIXA_NAO_ABERTO));
    }

    @Transactional
    public FechamentoResumoResponse fechar(FecharCaixaRequest request) {
        if (request == null || request.valorFechamento() == null
                || request.valorFechamento().signum() < 0) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
        BigDecimal fechamento = moeda(request.valorFechamento());
        CaixaResponse aberto = caixas.caixaAbertoComBloqueio()
                .orElseThrow(() -> new BusinessException(ErrorCode.CAIXA_NAO_ABERTO));
        BigDecimal esperado = caixas.calcularValorEsperado(aberto.id()).setScale(2, RoundingMode.HALF_UP);
        CaixaResponse fechado = caixas.fechar(aberto.id(), fechamento, esperado)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAIXA_NAO_ABERTO));
        return new FechamentoResumoResponse(fechado, esperado,
                fechamento.subtract(esperado).setScale(2, RoundingMode.HALF_UP));
    }

    public HistoricoFechamentoResponse historico() {
        return new HistoricoFechamentoResponse(caixas.historico());
    }

    @Transactional
    public MovimentacaoResponse movimentar(MovimentacaoRequest request, String codigoOperador) {
        if (request == null || request.valor() == null || request.valor().signum() <= 0) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
        TipoMovimentacao tipo;
        try {
            tipo = TipoMovimentacao.valueOf(
                    request.tipo() == null ? "" : request.tipo().trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.MOVIMENTACAO_INVALIDA);
        }
        String motivo = request.motivo() == null ? null : request.motivo().trim();
        if (motivo != null && motivo.isEmpty()) motivo = null;
        if (motivo != null && motivo.length() > 255) {
            throw new BusinessException(ErrorCode.MOVIMENTACAO_INVALIDA);
        }
        CaixaResponse caixa = caixas.caixaAbertoComBloqueio()
                .orElseThrow(() -> new BusinessException(ErrorCode.CAIXA_NAO_ABERTO));
        BigDecimal valor = moeda(request.valor());
        if (tipo == TipoMovimentacao.SANGRIA
                && caixas.calcularValorEsperado(caixa.id()).compareTo(valor) < 0) {
            throw new BusinessException(ErrorCode.MOVIMENTACAO_INVALIDA);
        }
        return caixas.movimentar(
                caixa.id(), caixas.idOperador(codigoOperador), tipo.name(), valor, motivo);
    }

    public java.util.List<MovimentacaoResponse> movimentacoesDoCaixaAberto() {
        return caixas.movimentacoes(obterAberto().id());
    }

    private BigDecimal moeda(BigDecimal valor) {
        try {
            return valor.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new BusinessException(ErrorCode.VALOR_INVALIDO);
        }
    }
}