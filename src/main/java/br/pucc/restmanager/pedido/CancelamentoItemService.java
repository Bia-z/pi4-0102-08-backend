package br.pucc.restmanager.pedido;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.MesaRepository;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.StatusItemResponse;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import br.pucc.restmanager.realtime.StatusPedidoPublisher;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Cancela um item de pedido que a cozinha ainda não começou a preparar.
 * O valor do pedido é recalculado; se todos os itens forem cancelados, o pedido
 * também é cancelado e a mesa volta a ficar disponível quando não sobra pedido nela.
 */
@Service
public class CancelamentoItemService {

    private static final List<StatusPedido> FECHADOS = List.of(StatusPedido.PAGO, StatusPedido.CANCELADO);

    private final PedidoRepository pedidos;
    private final ItemPedidoRepository itens;
    private final MesaRepository mesas;
    private final StatusPedidoPublisher statusPedidoPublisher;
    private final StatusMesaPublisher statusMesaPublisher;

    public CancelamentoItemService(
            PedidoRepository pedidos,
            ItemPedidoRepository itens,
            MesaRepository mesas,
            StatusPedidoPublisher statusPedidoPublisher,
            StatusMesaPublisher statusMesaPublisher) {
        this.pedidos = pedidos;
        this.itens = itens;
        this.mesas = mesas;
        this.statusPedidoPublisher = statusPedidoPublisher;
        this.statusMesaPublisher = statusMesaPublisher;
    }

    @Transactional
    public StatusItemResponse cancelarItem(Long pedidoId, Long itemId) {
        ItemPedido item = itens.findByIdAndPedidoId(itemId, pedidoId)
                .orElseThrow(() -> pedidos.existsById(pedidoId)
                        ? new BusinessException(ErrorCode.ITEM_PEDIDO_NAO_ENCONTRADO)
                        : new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO));
        Pedido pedido = item.getPedido();
        if (!pedido.isEmAberto()) {
            throw new BusinessException(ErrorCode.PEDIDO_NAO_EM_ABERTO);
        }
        Mesa mesa = mesas.buscarAtivaPorIdComBloqueio(pedido.getMesa().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MESA_NAO_ENCONTRADA));
        if (mesa.getStatus() == StatusMesa.EM_PAGAMENTO) {
            throw new BusinessException(ErrorCode.MESA_EM_PAGAMENTO);
        }

        item.cancelar();
        pedido.recalcularValorTotal(pedidos.taxaServicoPercentual());

        if (pedido.todosItensCancelados()) {
            pedido.cancelar();
            pedidos.save(pedido);
            if (!pedidos.existsByMesaIdAndStatusNotIn(mesa.getId(), FECHADOS)) {
                mesa.liberar();
                depoisDoCommit(() -> statusMesaPublisher.publicarDisponivel(mesa.getId(), mesa.getNumero()));
            }
        }

        ItemPedido salvo = itens.save(item);
        statusPedidoPublisher.publicar(salvo);
        return StatusItemResponse.from(salvo);
    }

    private void depoisDoCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }
}
