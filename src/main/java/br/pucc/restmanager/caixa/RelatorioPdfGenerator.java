package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.RelatorioDiaResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RelatorioPdfGenerator {

    private static final Charset CHARSET = Charset.forName("windows-1252");

    public byte[] gerar(RelatorioDiaResponse relatorio) {
        List<String> linhas = new ArrayList<>(List.of(
                "RELATORIO DO CAIXA - " + relatorio.data(),
                "Total recebido: " + moeda(relatorio.totalRecebido()),
                "Dinheiro: " + moeda(relatorio.totalDinheiro()),
                "Credito: " + moeda(relatorio.totalCredito()),
                "Debito: " + moeda(relatorio.totalDebito()),
                "Pix: " + moeda(relatorio.totalPix()),
                "Vale-alimentacao: " + moeda(relatorio.totalVale()),
                "Quantidade de pagamentos: " + relatorio.quantidadePagamentos(),
                "",
                "MOVIMENTACOES"));
        relatorio.movimentacoes().forEach(movimentacao -> linhas.add(
                movimentacao.dataHora() + " " + movimentacao.tipo() + " "
                        + moeda(movimentacao.valor())
                        + (movimentacao.motivo() == null ? "" : " - " + movimentacao.motivo())));
        StringBuilder content = new StringBuilder("BT\n/F1 10 Tf\n50 750 Td\n14 TL\n");
        for (String linha : linhas) {
            content.append('(').append(escapar(linha)).append(") Tj\nT*\n");
        }
        content.append("ET");
        byte[] stream = content.toString().getBytes(CHARSET);
        List<byte[]> objects = new ArrayList<>();
        objects.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(bytes("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objects.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>"));
        objects.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
                + "/Encoding /WinAnsiEncoding >>"));
        ByteArrayOutputStream streamObject = new ByteArrayOutputStream();
        streamObject.writeBytes(bytes("<< /Length " + stream.length + " >>\nstream\n"));
        streamObject.writeBytes(stream);
        streamObject.writeBytes(bytes("\nendstream"));
        objects.add(streamObject.toByteArray());

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.writeBytes(bytes("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.size());
            pdf.writeBytes(bytes((i + 1) + " 0 obj\n"));
            pdf.writeBytes(objects.get(i));
            pdf.writeBytes(bytes("\nendobj\n"));
        }
        int xref = pdf.size();
        pdf.writeBytes(bytes("xref\n0 " + offsets.size() + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++) {
            pdf.writeBytes(bytes(String.format("%010d 00000 n \n", offsets.get(i))));
        }
        pdf.writeBytes(bytes("trailer\n<< /Size " + offsets.size()
                + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF"));
        return pdf.toByteArray();
    }

    private String moeda(java.math.BigDecimal valor) {
        return "R$ " + valor.toPlainString().replace('.', ',');
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private byte[] bytes(String valor) {
        return valor.getBytes(CHARSET);
    }
}