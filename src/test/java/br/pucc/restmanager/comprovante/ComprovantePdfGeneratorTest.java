package br.pucc.restmanager.comprovante;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

class ComprovantePdfGeneratorTest {

    private final ComprovantePdfGenerator generator = new ComprovantePdfGenerator();

    @Test
    void geraPdfComDadosDaContaEIgnoraItensCancelados() {
        ComprovanteDados dados = new ComprovanteDados(
                10,
                LocalDateTime.of(2026, 11, 6, 12, 0),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("2.00"),
                new BigDecimal("22.00"),
                List.of(new ComprovanteDados.Item("Produto ativo", 2, new BigDecimal("10.00"))),
                List.of(new ComprovanteDados.Pagamento(
                        "PIX", new BigDecimal("22.00"), new BigDecimal("0.00"))));

        String conteudo = new String(generator.gerar(dados), Charset.forName("windows-1252"));

        assertTrue(conteudo.startsWith("%PDF-1.4"));
        assertTrue(conteudo.contains("Mesa: 10"));
        assertTrue(conteudo.contains("Horario: 06/11/2026 12:00"));
        assertTrue(conteudo.contains("2 x Produto ativo"));
        assertTrue(conteudo.contains("Subtotal: R$ 20,00"));
        assertTrue(conteudo.contains("Taxa de servico \\(10,00%\\): R$ 2,00"));
        assertTrue(conteudo.contains("TOTAL: R$ 22,00"));
        assertTrue(conteudo.contains("PIX: R$ 22,00"));
        assertTrue(!conteudo.contains("cancelado"));
    }

    @Test
    void geraPdfComDescontoETrocoRegistradosNoPagamento() {
        ComprovanteDados dados = new ComprovanteDados(
                10,
                LocalDateTime.of(2026, 11, 6, 12, 0),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("2.00"),
                new BigDecimal("21.00"),
                List.of(new ComprovanteDados.Item("Produto", 2, new BigDecimal("10.00"))),
                List.of(new ComprovanteDados.Pagamento(
                        "DINHEIRO", new BigDecimal("24.00"), new BigDecimal("3.00"))));

        String conteudo = new String(generator.gerar(dados), Charset.forName("windows-1252"));

        assertTrue(conteudo.contains("Taxa de servico \\(10,00%\\): R$ 2,00"));
        assertTrue(conteudo.contains("Desconto: -R$ 1,00"));
        assertTrue(conteudo.contains("TOTAL: R$ 21,00"));
        assertTrue(conteudo.contains("DINHEIRO: R$ 24,00"));
        assertTrue(conteudo.contains("Troco: R$ 3,00"));
    }
}
