package br.pucc.restmanager.transferencia.dto;

import jakarta.validation.constraints.NotNull;

public record TransferirMesaRequest(@NotNull Long mesaDestinoId) {
}