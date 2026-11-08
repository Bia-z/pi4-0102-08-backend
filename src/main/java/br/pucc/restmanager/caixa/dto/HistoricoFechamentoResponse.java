package br.pucc.restmanager.caixa.dto;

import java.util.List;

public record HistoricoFechamentoResponse(List<CaixaResponse> fechamentos) {
    public HistoricoFechamentoResponse {
        fechamentos = List.copyOf(fechamentos);
    }
}