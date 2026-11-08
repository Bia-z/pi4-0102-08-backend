package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.RelatorioDiaResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caixa/relatorios")
public class RelatorioController {

    private final RelatorioService relatorios;
    private final RelatorioPdfGenerator pdf;

    public RelatorioController(RelatorioService relatorios, RelatorioPdfGenerator pdf) {
        this.relatorios = relatorios;
        this.pdf = pdf;
    }

    @GetMapping("/{data}")
    public RelatorioDiaResponse gerar(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return relatorios.gerar(data);
    }

    @GetMapping(value = "/{data}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"relatorio-caixa-" + data + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.gerar(relatorios.gerar(data)));
    }
}