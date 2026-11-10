package br.pucc.restmanager.comanda;

import br.pucc.restmanager.comanda.dto.ComandaResumoResponse;
import br.pucc.restmanager.caixa.ConfiguracaoService;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImpressaoService {

    private static final Charset CHARSET = Charset.forName("windows-1252");
    private static final Logger LOGGER = LoggerFactory.getLogger(ImpressaoService.class);
    private final ComandaService comandas;
    private final ConfiguracaoService configuracoes;
    private final SimpMessagingTemplate messaging;

    public ImpressaoService(
            ComandaService comandas,
            ConfiguracaoService configuracoes,
            SimpMessagingTemplate messaging) {
        this.comandas = comandas;
        this.configuracoes = configuracoes;
        this.messaging = messaging;
    }

    public void publicarSeConfigurado(Long pedidoId, Long itemId) {
        try {
            ConfiguracaoResponse configuracao = configuracoes.buscar();
            if (!configuracao.imprimirComandaAoAceitar()) {
                return;
            }
            ComandaResumoResponse comanda = comandas.obter(pedidoId);
            if (!configuracao.comandaUnica()) {
                messaging.convertAndSend("/topic/comandas", comanda.somenteItem(itemId));
                return;
            }
            long aceitos = comanda.itens().stream()
                    .filter(item -> !"aguardando".equals(item.status()))
                    .count();
            if (aceitos == 1) {
                messaging.convertAndSend("/topic/comandas", comanda);
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao publicar a comanda do pedido {}", pedidoId, ex);
        }
    }

    public byte[] gerarPdf(Long pedidoId) {
        ComandaResumoResponse comanda = comandas.obter(pedidoId);
        List<String> linhas = new ArrayList<>(List.of(
                "COMANDA DO PEDIDO " + comanda.pedidoId(),
                "Mesa: " + comanda.numeroMesa(),
                "Garcom: " + comanda.nomeGarcom(),
                "Criada em: " + comanda.criadaEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "",
                "ITENS"));
        comanda.itens().forEach(item -> {
            linhas.add(item.quantidade() + " x " + item.nome() + " [" + item.status() + "]");
            if (item.observacao() != null && !item.observacao().isBlank()) {
                linhas.add("  Observacao: " + item.observacao());
            }
        });
        return gerarPdf(linhas);
    }

    private byte[] gerarPdf(List<String> linhas) {
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

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private byte[] bytes(String valor) {
        return valor.getBytes(CHARSET);
    }
}