package br.pucc.restmanager.transferencia;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import br.pucc.restmanager.transferencia.dto.TransferenciaResponse;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransferenciaService {

    private final TransferenciaMesaRepository transferencias;
    private final UsuarioRepository usuarios;
    private final StatusMesaPublisher statusMesaPublisher;

    public TransferenciaService(
            TransferenciaMesaRepository transferencias,
            UsuarioRepository usuarios,
            StatusMesaPublisher statusMesaPublisher) {
        this.transferencias = transferencias;
        this.usuarios = usuarios;
        this.statusMesaPublisher = statusMesaPublisher;
    }

    @Transactional
    public TransferenciaResponse transferirMesa(
            Long pedidoId,
            Long mesaDestinoId,
            String codigoUsuario) {
        Long mesaOrigemId = transferencias.buscarMesaDoPedido(pedidoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO));
        if (mesaOrigemId.equals(mesaDestinoId)) {
            throw new BusinessException(ErrorCode.MESAS_IGUAIS);
        }

        List<TransferenciaMesaRepository.MesaTransferencia> mesas =
                transferencias.bloquearMesas(mesaOrigemId, mesaDestinoId);
        Map<Long, TransferenciaMesaRepository.MesaTransferencia> mesasPorId = mesas.stream()
                .collect(Collectors.toMap(
                        TransferenciaMesaRepository.MesaTransferencia::id,
                        Function.identity()));
        TransferenciaMesaRepository.MesaTransferencia origem = mesasPorId.get(mesaOrigemId);
        TransferenciaMesaRepository.MesaTransferencia destino = mesasPorId.get(mesaDestinoId);
        if (origem == null || destino == null) {
            throw new BusinessException(ErrorCode.MESA_NAO_ENCONTRADA);
        }
        if ("EM_PAGAMENTO".equals(origem.status()) || "EM_PAGAMENTO".equals(destino.status())) {
            throw new BusinessException(ErrorCode.MESA_EM_PAGAMENTO);
        }
        if (!"DISPONIVEL".equals(destino.status())) {
            throw new BusinessException(ErrorCode.MESA_DESTINO_OCUPADA);
        }

        TransferenciaMesaRepository.PedidoTransferencia pedido =
                transferencias.bloquearPedido(pedidoId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO));
        if (!mesaOrigemId.equals(pedido.mesaId())
                || "PAGO".equals(pedido.status())
                || "CANCELADO".equals(pedido.status())) {
            throw new BusinessException(ErrorCode.PEDIDO_NAO_EM_ABERTO);
        }

        Usuario usuario = usuarios.findByCodigo(codigoUsuario)
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));
        LocalDateTime transferidoEm = LocalDateTime.now();
        transferencias.moverPedido(pedidoId, mesaDestinoId);
        transferencias.atualizarStatusMesa(destino.id(), "OCUPADA");
        boolean origemContinuaOcupada = transferencias.existePedidoAberto(mesaOrigemId);
        if (!origemContinuaOcupada) {
            transferencias.atualizarStatusMesa(origem.id(), "DISPONIVEL");
        }
        transferencias.registrarTransferencia(
                pedidoId, mesaOrigemId, mesaDestinoId, usuario.getId(), transferidoEm);
        publicarStatusMesas(
                origem,
                origemContinuaOcupada
                        ? origem.status().toLowerCase(Locale.ROOT)
                        : "disponivel",
                destino,
                "ocupada");

        return new TransferenciaResponse(pedidoId, mesaOrigemId, mesaDestinoId, transferidoEm);
    }

    private void publicarStatusMesas(
            TransferenciaMesaRepository.MesaTransferencia origem,
            String statusOrigem,
            TransferenciaMesaRepository.MesaTransferencia destino,
            String statusDestino) {
        Runnable publicar = () -> {
            statusMesaPublisher.publicarStatusAtualizado(
                    origem.id(), origem.numero(), statusOrigem);
            statusMesaPublisher.publicarStatusAtualizado(
                    destino.id(), destino.numero(), statusDestino);
        };
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
}