package br.pucc.restmanager.pedido;

import br.pucc.restmanager.cardapio.ProdutoRepository;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.comanda.ImpressaoService;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.MesaRepository;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.CriarPedidoRequest;
import br.pucc.restmanager.pedido.dto.ItemPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoListResponse;
import br.pucc.restmanager.pedido.dto.PedidoResponse;
import br.pucc.restmanager.pedido.dto.StatusItemResponse;
import br.pucc.restmanager.realtime.PedidoCriadoPublisher;
import br.pucc.restmanager.realtime.StatusPedidoPublisher;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PedidoService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final MesaRepository mesas;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final ItemPedidoRepository itens;
    private final PedidoCriadoPublisher pedidoCriadoPublisher;
    private final StatusPedidoPublisher statusPedidoPublisher;
    private final ImpressaoService impressao;

    public PedidoService(
            MesaRepository mesas,
            ProdutoRepository produtos,
            UsuarioRepository usuarios,
            PedidoRepository pedidos,
            ItemPedidoRepository itens,
            PedidoCriadoPublisher pedidoCriadoPublisher,
            StatusPedidoPublisher statusPedidoPublisher,
            ImpressaoService impressao) {
        this.mesas = mesas;
        this.produtos = produtos;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.itens = itens;
        this.pedidoCriadoPublisher = pedidoCriadoPublisher;
        this.statusPedidoPublisher = statusPedidoPublisher;
        this.impressao = impressao;
    }

    @Transactional
    public PedidoResponse criarPedido(CriarPedidoRequest request) {
        Mesa mesa = mesas.buscarAtivaPorIdComBloqueio(request.mesaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MESA_NAO_ENCONTRADA));
        if (mesa.getStatus() == StatusMesa.EM_PAGAMENTO) {
            throw new BusinessException(ErrorCode.MESA_EM_PAGAMENTO);
        }
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new BusinessException(ErrorCode.ITENS_PEDIDO_OBRIGATORIOS);
        }

        List<ItemValidado> itens = request.itens().stream()
                .map(this::validarItem)
                .toList();
        Usuario garcom = usuarios.findById(request.garcomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));

        BigDecimal subtotal = itens.stream()
                .map(item -> item.produto().getPreco()
                        .multiply(BigDecimal.valueOf(item.request().quantidade())))
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxa = subtotal.multiply(pedidos.taxaServicoPercentual())
                .divide(CEM, 2, RoundingMode.HALF_UP);

        Pedido pedido = new Pedido(mesa, garcom, subtotal.add(taxa));
        itens.forEach(item -> pedido.adicionarItem(new ItemPedido(
                item.produto(),
                item.request().quantidade(),
                item.request().observacao())));

        mesa.ocupar();
        Pedido salvo = pedidos.save(pedido);
        pedidoCriadoPublisher.publicar(salvo);
        return PedidoResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public List<PedidoListResponse> listarPedidos(
            StatusPedido status,
            StatusItem statusItem,
            Long garcomId,
            Long mesaId,
            boolean cozinha) {
        List<Pedido> pedidosAbertos = pedidos.listarAbertos(
                status, statusItem, garcomId, mesaId, cozinha);
        if (pedidosAbertos.isEmpty()) {
            return List.of();
        }
        BigDecimal taxaServicoPercentual = pedidos.taxaServicoPercentual();
        return pedidosAbertos.stream()
                .filter(pedido -> !cozinha || pedido.getItens().stream()
                        .anyMatch(item -> item.getStatus() == StatusItem.AGUARDANDO
                                || item.getStatus() == StatusItem.EM_PREPARO))
                .map(pedido -> PedidoListResponse.from(pedido, cozinha, taxaServicoPercentual))
                .toList();
    }

    @Transactional
    public StatusItemResponse atualizarStatusPedido(
            Long pedidoId,
            Long itemId,
            StatusItem novoStatus) {
        if (!pedidos.existsById(pedidoId)) {
            throw new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO);
        }
        ItemPedido item = itens.findByIdAndPedidoId(itemId, pedidoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_PEDIDO_NAO_ENCONTRADO));
        boolean imprimirComanda = item.getStatus() == StatusItem.AGUARDANDO
                && novoStatus == StatusItem.EM_PREPARO;
        item.atualizarStatus(novoStatus);
        ItemPedido salvo = itens.save(item);
        statusPedidoPublisher.publicar(salvo);
        if (imprimirComanda) {
            Runnable publicarComanda = () -> impressao.publicarSeConfigurado(pedidoId, itemId);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publicarComanda.run();
                    }
                });
            } else {
                publicarComanda.run();
            }
        }
        return StatusItemResponse.from(salvo);
    }

    private ItemValidado validarItem(ItemPedidoRequest request) {
        if (request == null || request.quantidade() == null || request.quantidade() < 1) {
            throw new BusinessException(ErrorCode.QUANTIDADE_INVALIDA);
        }
        if (request.produtoId() == null) {
            throw new BusinessException(ErrorCode.PRODUTO_NAO_ENCONTRADO);
        }
        Produto produto = produtos.findById(request.produtoId())
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUTO_NAO_ENCONTRADO));
        return new ItemValidado(request, produto);
    }

    private record ItemValidado(ItemPedidoRequest request, Produto produto) {
    }
}