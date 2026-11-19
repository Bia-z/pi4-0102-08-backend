package br.pucc.restmanager.pedido;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.CancelarPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoCanceladoResponse;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cancela um pedido que já foi pago (fechado). A operação exige o código e a senha de um
 * administrador. Os pagamentos continuam gravados em tb_pagamento, mas as somas do caixa e dos
 * relatórios desconsideram pedidos cancelados.
 */
@Service
public class CancelamentoPedidoService {

    private static final Logger LOG = LoggerFactory.getLogger(CancelamentoPedidoService.class);

    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public CancelamentoPedidoService(
            PedidoRepository pedidos,
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PedidoCanceladoResponse cancelar(Long pedidoId, CancelarPedidoRequest request) {
        String codigo = request == null ? null : request.codigoUsuario();
        String senha = request == null ? null : request.senha();
        validarAdministrador(codigo, senha);

        Pedido pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO));
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new BusinessException(ErrorCode.PEDIDO_JA_CANCELADO);
        }
        if (pedido.getStatus() != StatusPedido.PAGO) {
            throw new BusinessException(ErrorCode.PEDIDO_NAO_FECHADO);
        }

        pedido.cancelar();
        pedidos.save(pedido);
        LOG.info("Pedido {} pago foi cancelado pelo administrador {}", pedido.getId(), codigo.trim());
        return new PedidoCanceladoResponse(
                pedido.getId(), pedido.getStatus().name().toLowerCase(Locale.ROOT));
    }

    private void validarAdministrador(String codigoUsuario, String senha) {
        if (senha == null || senha.isBlank() || codigoUsuario == null || codigoUsuario.isBlank()) {
            throw new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS);
        }
        boolean valido = usuarios.findByCodigo(codigoUsuario.trim())
                .filter(usuario -> usuario.isAtivo()
                        && usuario.getPerfil() == Perfil.ADMIN
                        && passwordEncoder.matches(senha, usuario.getSenha()))
                .isPresent();
        if (!valido) {
            throw new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS);
        }
    }
}
