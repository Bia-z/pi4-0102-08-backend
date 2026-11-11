package br.pucc.restmanager.comanda;

import br.pucc.restmanager.comanda.dto.ComandaResumoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pedido.PedidoRepository;
import br.pucc.restmanager.pedido.domain.Pedido;
import java.time.LocalDate;
import java.util.List;
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
        return comanda(pedidos.findById(pedidoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO)));
    }

    @Transactional(readOnly = true)
    public List<ComandaResumoResponse> listar(LocalDate data) {
        return pedidos.buscarCriadosEntre(data.atStartOfDay(), data.plusDays(1).atStartOfDay())
                .stream()
                .map(this::comanda)
                .toList();
    }

    private ComandaResumoResponse comanda(Pedido pedido) {
        return new ComandaResumoResponse(
                pedido.getId(),
                pedido.getMesa().getNumero(),
                pedido.getGarcom().getNome(),
                pedido.getDataCriacao(),
                pedido.getItens().stream()
                        .map(item -> new ComandaResumoResponse.Item(
                                item.getId(),
                                item.getProduto().getNome(),
                                item.getQuantidade(),
                                item.getObservacao(),
                                item.getStatus().name().toLowerCase(Locale.ROOT)))
                        .toList());
    }
}
