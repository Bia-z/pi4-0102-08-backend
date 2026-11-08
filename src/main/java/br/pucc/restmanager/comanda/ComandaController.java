package br.pucc.restmanager.comanda;

import br.pucc.restmanager.comanda.dto.ComandaResumoResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos/{pedidoId}/comanda")
public class ComandaController {

    private final ComandaService comandas;
    private final ImpressaoService impressao;

    public ComandaController(ComandaService comandas, ImpressaoService impressao) {
        this.comandas = comandas;
        this.impressao = impressao;
    }

    @GetMapping
    public ComandaResumoResponse obter(@PathVariable Long pedidoId) {
        return comandas.obter(pedidoId);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarPdf(@PathVariable Long pedidoId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"comanda-" + pedidoId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(impressao.gerarPdf(pedidoId));
    }
}