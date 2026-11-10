package br.pucc.restmanager.comanda;

import br.pucc.restmanager.comanda.dto.ComandaResumoResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ComandaController {

    private final ComandaService comandas;
    private final ImpressaoService impressao;

    public ComandaController(ComandaService comandas, ImpressaoService impressao) {
        this.comandas = comandas;
        this.impressao = impressao;
    }

    @GetMapping("/comandas")
    public List<ComandaResumoResponse> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return comandas.listar(data);
    }

    @GetMapping("/pedidos/{pedidoId}/comanda")
    public ComandaResumoResponse obter(@PathVariable Long pedidoId) {
        return comandas.obter(pedidoId);
    }

    @GetMapping(value = "/pedidos/{pedidoId}/comanda/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarPdf(@PathVariable Long pedidoId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"comanda-" + pedidoId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(impressao.gerarPdf(pedidoId));
    }
}
