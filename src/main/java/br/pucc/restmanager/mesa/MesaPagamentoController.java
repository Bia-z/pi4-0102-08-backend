package br.pucc.restmanager.mesa;

import br.pucc.restmanager.mesa.dto.PagamentoCanceladoResponse;
import br.pucc.restmanager.mesa.dto.PagamentoIniciadoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mesas")
public class MesaPagamentoController {

    private final MesaPagamentoService pagamentos;

    public MesaPagamentoController(MesaPagamentoService pagamentos) {
        this.pagamentos = pagamentos;
    }

    @PostMapping("/{mesaId}/pagamento/iniciar")
    public PagamentoIniciadoResponse iniciarPagamento(
            @PathVariable Long mesaId,
            Authentication authentication) {
        return pagamentos.iniciarPagamento(mesaId, authentication.getName());
    }

    @PostMapping("/{mesaId}/pagamento/cancelar")
    public PagamentoCanceladoResponse cancelarPagamento(
            @PathVariable Long mesaId,
            Authentication authentication) {
        return pagamentos.cancelarPagamento(mesaId, authentication.getName());
    }
}