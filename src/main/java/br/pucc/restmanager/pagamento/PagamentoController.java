package br.pucc.restmanager.pagamento;

import br.pucc.restmanager.pagamento.dto.ProcessarPagamentoRequest;
import br.pucc.restmanager.pagamento.dto.ProcessarPagamentoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mesas")
public class PagamentoController {

    private final PagamentoService pagamentos;

    public PagamentoController(PagamentoService pagamentos) {
        this.pagamentos = pagamentos;
    }

    @PostMapping("/{mesaId}/pagamento/processar")
    public ProcessarPagamentoResponse processarPagamento(
            @PathVariable Long mesaId,
            @Valid @RequestBody ProcessarPagamentoRequest request) {
        return pagamentos.processarPagamento(mesaId, request);
    }
}