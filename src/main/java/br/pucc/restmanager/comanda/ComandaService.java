package br.pucc.restmanager.comanda;

import br.pucc.restmanager.comanda.dto.ComandaResumoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pedido.PedidoRepository;
import br.pucc.restmanager.pedido.domain.Pedido;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComandaService {

    private final PedidoRepository pedidos;

    public ComandaService(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public ComandaResumoResponse obter(Long pedidoId) {
        Pedido pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO));
        return new ComandaResumoResponse(
                pedido.getId(),
                pedido.getMesa().getNumero(),
                pedido.getGarcom().getNome(),
                pedido.getDataCriacao(),
                pedido.getItens().stream()
                        .map(item -> new ComandaResumoResponse.Item(
                                item.getProduto().getNome(),
                                item.getQuantidade(),
                                item.getObservacao(),
                                item.getStatus().name().toLowerCase(Locale.ROOT)))
                        .toList());
    }
}