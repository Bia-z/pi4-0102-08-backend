package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.ConfiguracaoRequest;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caixa/configuracao")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracoes;

    public ConfiguracaoController(ConfiguracaoService configuracoes) {
        this.configuracoes = configuracoes;
    }

    @GetMapping
    public ConfiguracaoResponse buscar() {
        return configuracoes.buscar();
    }

    @PutMapping
    public ConfiguracaoResponse atualizar(@Valid @RequestBody ConfiguracaoRequest request) {
        return configuracoes.atualizar(request);
    }
}