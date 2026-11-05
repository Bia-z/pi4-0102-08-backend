package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.realtime.dto.PedidoCriadoMessage;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PainelCozinhaService {

    private static final List<StatusItem> EM_ABERTO = List.of(StatusItem.AGUARDANDO, StatusItem.EM_PREPARO);

    private final PedidoRepository pedidos;

    public PainelCozinhaService(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public List<PedidoCriadoMessage> listarEmAberto() {
        return pedidos.findComItensNosStatus(EM_ABERTO).stream()
                .map(this::paraMensagem)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoCriadoMessage buscar(Long pedidoId) {
        return paraMensagem(pedidos.findById(pedidoId).orElseThrow());
    }

    PedidoCriadoMessage paraMensagem(Pedido pedido) {
        List<PedidoCriadoMessage.Item> itens = pedido.getItens().stream()
                .filter(item -> item.getStatus() != StatusItem.CANCELADO)
                .map(this::paraItem)
                .toList();
        return new PedidoCriadoMessage(
                pedido.getId(),
                pedido.getMesa().getNumero(),
                pedido.getDataCriacao(),
                itens);
    }

    private PedidoCriadoMessage.Item paraItem(ItemPedido item) {
        return new PedidoCriadoMessage.Item(
                item.getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getObservacao(),
                item.getStatus().name().toLowerCase(Locale.ROOT));
    }
}
