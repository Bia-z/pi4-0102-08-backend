package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.dto.StatusItemResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class CancelamentoItemController {

    private final CancelamentoItemService cancelamento;

    public CancelamentoItemController(CancelamentoItemService cancelamento) {
        this.cancelamento = cancelamento;
    }

    @PostMapping("/{pedidoId}/itens/{itemId}/cancelar")
    public StatusItemResponse cancelarItem(@PathVariable Long pedidoId, @PathVariable Long itemId) {
        return cancelamento.cancelarItem(pedidoId, itemId);
    }
}
