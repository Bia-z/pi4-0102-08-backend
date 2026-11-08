package br.pucc.restmanager.mesa;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.mesa.dto.PagamentoCanceladoResponse;
import br.pucc.restmanager.mesa.dto.PagamentoIniciadoResponse;
import br.pucc.restmanager.pedido.PedidoRepository;
import br.pucc.restmanager.realtime.StatusMesaPublisher;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class MesaPagamentoService {

    private final MesaRepository mesas;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final StatusMesaPublisher statusMesaPublisher;

    public MesaPagamentoService(
            MesaRepository mesas,
            PedidoRepository pedidos,
            UsuarioRepository usuarios,
            StatusMesaPublisher statusMesaPublisher) {
        this.mesas = mesas;
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.statusMesaPublisher = statusMesaPublisher;
    }

    @Transactional
    public PagamentoIniciadoResponse iniciarPagamento(Long mesaId, String codigoOperador) {
        Mesa mesa = buscarMesaBloqueada(mesaId);
        if (mesa.getStatus() == StatusMesa.EM_PAGAMENTO) {
            throw new BusinessException(ErrorCode.MESA_JA_EM_PAGAMENTO);
        }
        if (mesa.getStatus() != StatusMesa.OCUPADA
                || pedidos.listarAbertos(null, null, null, mesaId, false).isEmpty()) {
            throw new BusinessException(ErrorCode.MESA_SEM_PEDIDO_ABERTO);
        }

        Usuario operador = usuarios.findByCodigo(codigoOperador)
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));
        LocalDateTime iniciadoEm = LocalDateTime.now();
        mesa.iniciarPagamento(operador, iniciadoEm);
        publicarStatus(mesa, StatusMesa.EM_PAGAMENTO);
        return new PagamentoIniciadoResponse(
                mesa.getId(),
                mesa.getNumero(),
                status(StatusMesa.EM_PAGAMENTO),
                operador.getId(),
                iniciadoEm);
    }

    @Transactional
    public PagamentoCanceladoResponse cancelarPagamento(Long mesaId, String codigoOperador) {
        Mesa mesa = buscarMesaBloqueada(mesaId);
        if (mesa.getStatus() != StatusMesa.EM_PAGAMENTO) {
            throw new BusinessException(ErrorCode.MESA_NAO_EM_PAGAMENTO);
        }

        Usuario operador = usuarios.findByCodigo(codigoOperador)
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));
        boolean administrador = operador.getPerfil() == Perfil.ADMIN;
        boolean operadorResponsavel = mesa.getPagamentoIniciadoPor() != null
                && mesa.getPagamentoIniciadoPor().getId().equals(operador.getId());
        if (!administrador && !operadorResponsavel) {
            throw new BusinessException(ErrorCode.ACESSO_NEGADO);
        }

        mesa.cancelarPagamento();
        publicarStatus(mesa, StatusMesa.OCUPADA);
        return new PagamentoCanceladoResponse(
                mesa.getId(),
                mesa.getNumero(),
                status(StatusMesa.OCUPADA));
    }

    @Transactional
    public void cancelarPagamento(String codigoOperador) {
        Usuario operador = usuarios.findByCodigo(codigoOperador)
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));
        for (Mesa mesa : mesas.buscarPagamentosIniciadosPorComBloqueio(operador.getId())) {
            mesa.cancelarPagamento();
            publicarStatus(mesa, StatusMesa.OCUPADA);
        }
    }

    private Mesa buscarMesaBloqueada(Long mesaId) {
        return mesas.buscarAtivaPorIdComBloqueio(mesaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESA_NAO_ENCONTRADA));
    }

    private void publicarStatus(Mesa mesa, StatusMesa status) {
        Runnable publicar = () -> statusMesaPublisher.publicarStatusAtualizado(
                mesa.getId(), mesa.getNumero(), status(status));
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

    private String status(StatusMesa status) {
        return status.name().toLowerCase(Locale.ROOT);
    }
}