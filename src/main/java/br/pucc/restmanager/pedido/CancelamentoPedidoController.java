package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.dto.CancelarPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoCanceladoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class CancelamentoPedidoController {

    private final CancelamentoPedidoService cancelamento;

    public CancelamentoPedidoController(CancelamentoPedidoService cancelamento) {
        this.cancelamento = cancelamento;
    }

    @PostMapping("/{pedidoId}/cancelar")
    public PedidoCanceladoResponse cancelar(
            @PathVariable Long pedidoId, @Valid @RequestBody CancelarPedidoRequest request) {
        return cancelamento.cancelar(pedidoId, request);
    }
}
