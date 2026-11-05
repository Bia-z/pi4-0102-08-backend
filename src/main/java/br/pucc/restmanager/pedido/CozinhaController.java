package br.pucc.restmanager.pedido;

import br.pucc.restmanager.realtime.dto.PedidoCriadoMessage;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cozinha")
public class CozinhaController {

    private final PainelCozinhaService painel;

    public CozinhaController(PainelCozinhaService painel) {
        this.painel = painel;
    }

    @GetMapping("/pedidos")
    public List<PedidoCriadoMessage> pedidosEmAberto() {
        return painel.listarEmAberto();
    }
}
