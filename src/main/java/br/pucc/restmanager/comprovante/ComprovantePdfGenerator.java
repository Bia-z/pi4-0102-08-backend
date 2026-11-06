package br.pucc.restmanager.comprovante;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ComprovantePdfGenerator {

    private static final Charset PDF_CHARSET = Charset.forName("windows-1252");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    public byte[] gerar(ComprovanteDados dados) {
        try {
            return renderizar(dados);
        } catch (IOException | RuntimeException ex) {
            throw new PdfGenerationException(ex);
        }
    }

    private byte[] renderizar(ComprovanteDados dados) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("COMPROVANTE DA CONTA");
        linhas.add("Mesa: " + dados.numeroMesa());
        if (dados.horario() != null) {
            linhas.add("Horario: " + DATA_HORA.format(dados.horario()));
        }
        linhas.add("");
        linhas.add("ITENS");
        for (ComprovanteDados.Item item : dados.itens()) {
            linhas.add(item.quantidade() + " x " + item.nome() + "  "
                    + moeda(item.precoUnitario()) + "  Subtotal: " + moeda(item.subtotal()));
        }
        linhas.add("");
        linhas.add("Subtotal: " + moeda(dados.subtotal()));
        linhas.add("Taxa de servico (" + dados.percentualTaxaServico()
                .setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + "%): "
                + moeda(dados.taxaServico()));
        BigDecimal desconto = dados.subtotal().add(dados.taxaServico()).subtract(dados.total());
        if (desconto.signum() > 0) {
            linhas.add("Desconto: -" + moeda(desconto));
        }
        linhas.add("TOTAL: " + moeda(dados.total()));
        linhas.add("");
        linhas.add("PAGAMENTOS");
        for (ComprovanteDados.Pagamento pagamento : dados.pagamentos()) {
            linhas.add(pagamento.forma() + ": " + moeda(pagamento.valor()));
            if (pagamento.troco().compareTo(ZERO) > 0) {
                linhas.add("  Troco: " + moeda(pagamento.troco()));
            }
        }
        return criarDocumento(linhas);
    }

    private byte[] criarDocumento(List<String> linhas) throws IOException {
        List<List<String>> paginas = new ArrayList<>();
        for (int inicio = 0; inicio < linhas.size(); inicio += 48) {
            paginas.add(linhas.subList(inicio, Math.min(inicio + 48, linhas.size())));
        }
        int quantidadeObjetos = 3 + paginas.size() * 2;
        List<byte[]> objetos = new ArrayList<>(quantidadeObjetos);
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder filhos = new StringBuilder();
        for (int indice = 0; indice < paginas.size(); indice++) {
            filhos.append(4 + indice * 2).append(" 0 R ");
        }
        objetos.add(bytes("<< /Type /Pages /Kids [" + filhos + "] /Count "
                + paginas.size() + " >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
                + "/Encoding /WinAnsiEncoding >>"));

        for (int indice = 0; indice < paginas.size(); indice++) {
            byte[] conteudo = criarConteudo(paginas.get(indice));
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents "
                    + (5 + indice * 2) + " 0 R >>"));
            ByteArrayOutputStream fluxo = new ByteArrayOutputStream();
            fluxo.write(bytes("<< /Length " + conteudo.length + " >>\nstream\n"));
            fluxo.write(conteudo);
            fluxo.write(bytes("\nendstream"));
            objetos.add(fluxo.toByteArray());
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write(bytes("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int indice = 0; indice < objetos.size(); indice++) {
            offsets.add(pdf.size());
            pdf.write(bytes((indice + 1) + " 0 obj\n"));
            pdf.write(objetos.get(indice));
            pdf.write(bytes("\nendobj\n"));
        }
        int xref = pdf.size();
        pdf.write(bytes("xref\n0 " + offsets.size() + "\n"));
        pdf.write(bytes("0000000000 65535 f \n"));
        for (int indice = 1; indice < offsets.size(); indice++) {
            pdf.write(bytes(String.format("%010d 00000 n \n", offsets.get(indice))));
        }
        pdf.write(bytes("trailer\n<< /Size " + offsets.size() + " /Root 1 0 R >>\n"
                + "startxref\n" + xref + "\n%%EOF"));
        return pdf.toByteArray();
    }

    private byte[] criarConteudo(List<String> linhas) {
        StringBuilder conteudo = new StringBuilder("BT\n/F1 10 Tf\n50 750 Td\n14 TL\n");
        for (String linha : linhas) {
            conteudo.append('(')
                    .append(escapar(linha))
                    .append(") Tj\nT*\n");
        }
        conteudo.append("ET");
        return bytes(conteudo.toString());
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String moeda(BigDecimal valor) {
        return "R$ " + valor.setScale(2, RoundingMode.HALF_UP)
                .toPlainString()
                .replace('.', ',');
    }

    private byte[] bytes(String texto) {
        return texto.getBytes(PDF_CHARSET);
    }
}