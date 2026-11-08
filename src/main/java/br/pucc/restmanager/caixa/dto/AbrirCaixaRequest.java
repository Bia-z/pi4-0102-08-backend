
package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record AbrirCaixaRequest(BigDecimal valorAbertura, String turno) {
}
