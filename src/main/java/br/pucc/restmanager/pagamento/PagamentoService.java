package br.pucc.restmanager.pagamento;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pagamento.domain.FormaPagamento;
import br.pucc.restmanager.pagamento.dto.FormaPagamentoRequest;
import br.pucc.restmanager.pagamento.dto.ProcessarPagamentoRequest;
import br.pucc.restmanager.pagamento.dto.ProcessarPagamentoResponse;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PagamentoService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final PagamentoMesaRepository pagamentos;
    private final StatusMesaPublisher statusMesaPublisher;

    public PagamentoService(
            PagamentoMesaRepository pagamentos,
            StatusMesaPublisher statusMesaPublisher) {
        this.pagamentos = pagamentos;
        this.statusMesaPublisher = statusMesaPublisher;
    }

    @Transactional
    public ProcessarPagamentoResponse processarPagamento(
            Long mesaId,
            ProcessarPagamentoRequest request) {
        PagamentoMesaRepository.MesaPagamento mesa = pagamentos.bloquearMesa(mesaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESA_NAO_ENCONTRADA));
        if (!"EM_PAGAMENTO".equals(mesa.status())) {
            throw new BusinessException(ErrorCode.MESA_NAO_EM_PAGAMENTO);
        }

        List<PagamentoMesaRepository.PedidoPagamento> pedidos =
                pagamentos.buscarPedidosAbertos(mesaId);
        if (pedidos.isEmpty()) {
            ErrorCode erro = pagamentos.existePedidoPago(mesaId)
                    ? ErrorCode.PEDIDO_JA_PAGO
                    : ErrorCode.MESA_SEM_PEDIDO_ABERTO;
            throw new BusinessException(erro);
        }
        validarRequisicao(request);

        BigDecimal subtotal = pedidos.stream()
                .map(PagamentoMesaRepository.PedidoPagamento::subtotal)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxaServico = subtotal
                .multiply(pagamentos.taxaServicoPercentual())
                .divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal desconto = request.desconto().setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxaServico).subtract(desconto)
                .setScale(2, RoundingMode.HALF_UP);
        if (desconto.compareTo(subtotal.add(taxaServico)) > 0) {
            throw new BusinessException(ErrorCode.DESCONTO_INVALIDO);
        }

        List<PagamentoEntrada> entradas = request.pagamentos().stream()
                .map(this::converter)
                .sorted(Comparator.comparing(entrada ->
                        entrada.forma() == FormaPagamento.DINHEIRO))
                .toList();
        BigDecimal totalPago = entradas.stream()
                .map(PagamentoEntrada::valor)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (totalPago.compareTo(total) < 0) {
            throw new BusinessException(ErrorCode.VALOR_PAGAMENTO_INSUFICIENTE);
        }

        BigDecimal troco = totalPago.subtract(total).setScale(2, RoundingMode.HALF_UP);
        List<SaldoPedido> saldos = distribuirTotal(pedidos, total, subtotal);
        gravarEntradas(entradas, saldos, troco);
        pagamentos.marcarPedidosPagos(mesaId);
        pagamentos.liberarMesa(mesaId);
        publicarStatusMesa(mesa);

        return new ProcessarPagamentoResponse(
                mesa.id(),
                subtotal,
                taxaServico,
                desconto,
                total,
                totalPago,
                troco,
                pedidos.stream().map(PagamentoMesaRepository.PedidoPagamento::id).toList(),
                "disponivel");
    }

    private void validarRequisicao(ProcessarPagamentoRequest request) {
        if (request == null || request.pagamentos() == null || request.pagamentos().isEmpty()) {
            throw new BusinessException(ErrorCode.PAGAMENTO_INVALIDO);
        }
        if (request.desconto() == null || request.desconto().signum() < 0) {
            throw new BusinessException(ErrorCode.DESCONTO_INVALIDO);
        }
        for (FormaPagamentoRequest pagamento : request.pagamentos()) {
            if (pagamento == null || pagamento.formaPagamento() == null
                    || pagamento.valor() == null || pagamento.valor().signum() <= 0) {
                throw new BusinessException(ErrorCode.PAGAMENTO_INVALIDO);
            }
        }
    }

    private PagamentoEntrada converter(FormaPagamentoRequest pagamento) {
        try {
            return new PagamentoEntrada(
                    FormaPagamento.valueOf(pagamento.formaPagamento().trim().toUpperCase(Locale.ROOT)),
                    pagamento.valor().setScale(2, RoundingMode.UNNECESSARY));
        } catch (IllegalArgumentException | ArithmeticException ex) {
            throw new BusinessException(ErrorCode.PAGAMENTO_INVALIDO);
        }
    }

    private List<SaldoPedido> distribuirTotal(
            List<PagamentoMesaRepository.PedidoPagamento> pedidos,
            BigDecimal total,
            BigDecimal subtotal) {
        List<SaldoPedido> saldos = new ArrayList<>();
        BigDecimal distribuido = ZERO;
        for (int i = 0; i < pedidos.size(); i++) {
            BigDecimal valor;
            if (i == pedidos.size() - 1) {
                valor = total.subtract(distribuido);
            } else if (subtotal.signum() == 0) {
                valor = ZERO;
            } else {
                valor = total.multiply(pedidos.get(i).subtotal())
                        .divide(subtotal, 2, RoundingMode.HALF_UP);
                distribuido = distribuido.add(valor);
            }
            saldos.add(new SaldoPedido(pedidos.get(i).id(), valor));
        }
        return saldos;
    }

    private void gravarEntradas(
            List<PagamentoEntrada> entradas,
            List<SaldoPedido> saldos,
            BigDecimal trocoTotal) {
        int indicePedido = 0;
        BigDecimal restantePedido = saldos.isEmpty() ? ZERO : saldos.get(0).valor();
        for (int indiceEntrada = 0; indiceEntrada < entradas.size(); indiceEntrada++) {
            PagamentoEntrada entrada = entradas.get(indiceEntrada);
            BigDecimal restanteEntrada = entrada.valor();
            boolean ultimaEntrada = indiceEntrada == entradas.size() - 1;
            while (restanteEntrada.signum() > 0) {
                while (indicePedido < saldos.size() && restantePedido.signum() == 0) {
                    indicePedido++;
                    if (indicePedido < saldos.size()) {
                        restantePedido = saldos.get(indicePedido).valor();
                    }
                }
                if (indicePedido >= saldos.size()) {
                    BigDecimal troco = restanteEntrada;
                    if (entrada.forma() != FormaPagamento.DINHEIRO) {
                        throw new BusinessException(ErrorCode.TROCO_REQUER_DINHEIRO);
                    }
                    pagamentos.gravarPagamento(
                            saldos.get(saldos.size() - 1).pedidoId(),
                            entrada.forma(),
                            troco,
                            troco);
                    break;
                }

                BigDecimal aplicado = restanteEntrada.min(restantePedido);
                boolean finalDoTotal = ultimaEntrada
                        && restanteEntrada.compareTo(restantePedido) > 0
                        && trocoTotal.signum() > 0;
                if (finalDoTotal && entrada.forma() != FormaPagamento.DINHEIRO) {
                    throw new BusinessException(ErrorCode.TROCO_REQUER_DINHEIRO);
                }
                BigDecimal valorLancado = finalDoTotal ? aplicado.add(trocoTotal) : aplicado;
                BigDecimal trocoLancado = finalDoTotal ? trocoTotal : ZERO;
                pagamentos.gravarPagamento(
                        saldos.get(indicePedido).pedidoId(),
                        entrada.forma(),
                        valorLancado,
                        trocoLancado);
                restanteEntrada = restanteEntrada.subtract(aplicado);
                restantePedido = restantePedido.subtract(aplicado);
                if (finalDoTotal) {
                    break;
                }
            }
        }
    }

    private void publicarStatusMesa(PagamentoMesaRepository.MesaPagamento mesa) {
        Runnable publicar = () -> statusMesaPublisher.publicarDisponivel(mesa.id(), mesa.numero());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publicar.run();
                }
            });
        } else {
            publicar.run();
        }
    }

    private record PagamentoEntrada(FormaPagamento forma, BigDecimal valor) {
    }

    private record SaldoPedido(Long pedidoId, BigDecimal valor) {
    }
}