package br.pucc.restmanager.comprovante;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComprovanteDados(
        int numeroMesa,
        LocalDateTime horario,
        BigDecimal percentualTaxaServico,
        BigDecimal subtotal,
        BigDecimal taxaServico,
        BigDecimal total,
        List<Item> itens,
        List<Pagamento> pagamentos) {

    public ComprovanteDados {
        itens = List.copyOf(itens);
        pagamentos = List.copyOf(pagamentos);
    }

    public record Item(String nome, int quantidade, BigDecimal precoUnitario) {
        public BigDecimal subtotal() {
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    public record Pagamento(String forma, BigDecimal valor, BigDecimal troco) {
        public BigDecimal valorAplicado() {
            return valor.subtract(troco);
        }
    }
}
