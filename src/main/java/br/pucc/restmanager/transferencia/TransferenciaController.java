package br.pucc.restmanager.transferencia;

import br.pucc.restmanager.transferencia.dto.TransferenciaResponse;
import br.pucc.restmanager.transferencia.dto.TransferirMesaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class TransferenciaController {

    private final TransferenciaService transferencias;

    public TransferenciaController(TransferenciaService transferencias) {
        this.transferencias = transferencias;
    }

    @PostMapping("/{pedidoId}/transferir-mesa")
    @ResponseStatus(HttpStatus.OK)
    public TransferenciaResponse transferirMesa(
            @PathVariable Long pedidoId,
            @Valid @RequestBody TransferirMesaRequest request,
            Authentication authentication) {
        return transferencias.transferirMesa(
                pedidoId, request.mesaDestinoId(), authentication.getName());
    }
}