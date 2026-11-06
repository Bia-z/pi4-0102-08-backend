package br.pucc.restmanager.pedido;

import br.pucc.restmanager.cardapio.ProdutoRepository;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.MesaRepository;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.dto.CriarPedidoRequest;
import br.pucc.restmanager.pedido.dto.ItemPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoResponse;
import br.pucc.restmanager.realtime.PedidoCriadoPublisher;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final MesaRepository mesas;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final PedidoCriadoPublisher pedidoCriadoPublisher;

    public PedidoService(
            MesaRepository mesas,
            ProdutoRepository produtos,
            UsuarioRepository usuarios,
            PedidoRepository pedidos,
            PedidoCriadoPublisher pedidoCriadoPublisher) {
        this.mesas = mesas;
        this.produtos = produtos;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.pedidoCriadoPublisher = pedidoCriadoPublisher;
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