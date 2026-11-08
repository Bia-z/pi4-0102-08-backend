package br.pucc.restmanager.pedido;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import br.pucc.restmanager.pedido.dto.AtualizarStatusItemRequest;
import br.pucc.restmanager.pedido.dto.CriarPedidoRequest;
import br.pucc.restmanager.pedido.dto.PedidoListResponse;
import br.pucc.restmanager.pedido.dto.PedidoResponse;
import br.pucc.restmanager.pedido.dto.StatusItemResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping
    public List<PedidoListResponse> listarPedidos(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "statusItem", required = false) String statusItem,
            @RequestParam(name = "garcomId", required = false) Long garcomId,
            @RequestParam(name = "mesaId", required = false) Long mesaId,
            Authentication authentication) {
        boolean cozinha = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_COZINHA"));
        boolean caixa = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CAIXA"));
        if (caixa && mesaId == null) {
            throw new BusinessException(ErrorCode.ACESSO_NEGADO);
        }
        return pedidos.listarPedidos(
                converterStatus(status, StatusPedido.class),
                converterStatus(statusItem, StatusItem.class),
                garcomId,
                mesaId,
                cozinha);
    }

    private <T extends Enum<T>> T converterStatus(String valor, Class<T> tipo) {
        if (valor == null) {
            return null;
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido", ex);
        }
    }

    @PatchMapping("/{pedidoId}/itens/{itemId}/status")
    public StatusItemResponse atualizarStatusPedido(
            @PathVariable Long pedidoId,
            @PathVariable Long itemId,
            @Valid @RequestBody AtualizarStatusItemRequest request) {
        StatusItem novoStatus = converterStatus(request.novoStatus(), StatusItem.class);
        return pedidos.atualizarStatusPedido(pedidoId, itemId, novoStatus);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criarPedido(@Valid @RequestBody CriarPedidoRequest request) {
        return pedidos.criarPedido(request);
    }
}