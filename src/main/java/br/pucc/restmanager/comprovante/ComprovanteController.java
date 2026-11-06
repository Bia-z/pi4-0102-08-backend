package br.pucc.restmanager.comprovante;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class ComprovanteController {

    private final ComprovanteService comprovantes;

    public ComprovanteController(ComprovanteService comprovantes) {
        this.comprovantes = comprovantes;
    }

    @GetMapping("/{pedidoId}/comprovante-pdf")
    public ResponseEntity<byte[]> gerarComprovantePDF(@PathVariable Long pedidoId) {
        byte[] pdf = comprovantes.gerarComprovantePDF(pedidoId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("comprovante-pedido-" + pedidoId + ".pdf")
                                .build()
                                .toString())
                .body(pdf);
    }
}