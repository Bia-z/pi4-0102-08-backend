package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.AbrirCaixaRequest;
import br.pucc.restmanager.caixa.dto.CaixaResponse;
import br.pucc.restmanager.caixa.dto.FecharCaixaRequest;
import br.pucc.restmanager.caixa.dto.FechamentoResumoResponse;
import br.pucc.restmanager.caixa.dto.HistoricoFechamentoResponse;
import br.pucc.restmanager.caixa.dto.MovimentacaoRequest;
import br.pucc.restmanager.caixa.dto.MovimentacaoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caixa")
public class CaixaController {

    private final CaixaService caixas;

    public CaixaController(CaixaService caixas) {
        this.caixas = caixas;
    }

    @GetMapping("/aberto")
    public CaixaResponse obterAberto() {
        return caixas.obterAberto();
    }

    @PostMapping("/abrir")
    @ResponseStatus(HttpStatus.CREATED)
    public CaixaResponse abrir(@Valid @RequestBody AbrirCaixaRequest request, Authentication auth) {
        return caixas.abrir(request, auth.getName());
    }

    @PostMapping("/fechar")
    public FechamentoResumoResponse fechar(@Valid @RequestBody FecharCaixaRequest request) {
        return caixas.fechar(request);
    }

    @GetMapping("/historico")
    public HistoricoFechamentoResponse historico() {
        return caixas.historico();
    }

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse movimentar(
            @Valid @RequestBody MovimentacaoRequest request, Authentication auth) {
        return caixas.movimentar(request, auth.getName());
    }

    @GetMapping("/movimentacoes")
    public List<MovimentacaoResponse> movimentacoes() {
        return caixas.movimentacoesDoCaixaAberto();
    }
}