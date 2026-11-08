package br.pucc.restmanager.caixa.dto;

import java.math.BigDecimal;

public record FecharCaixaRequest(BigDecimal valorFechamento, String codigoUsuario, String senha) {
}