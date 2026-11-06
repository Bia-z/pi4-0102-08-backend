package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.dto.CriarPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criarPedido(@Valid @RequestBody CriarPedidoRequest request) {
        return pedidos.criarPedido(request);
    }
}