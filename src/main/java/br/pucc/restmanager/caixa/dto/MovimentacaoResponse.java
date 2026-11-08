package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponse(
        Long id, Long caixaId, String tipo, BigDecimal valor, String motivo, LocalDateTime dataHora) {
}