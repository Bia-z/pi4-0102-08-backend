package br.pucc.restmanager.mesa.dto;

import br.pucc.restmanager.mesa.domain.Mesa;
import java.util.Locale;

public record MesaResponse(Long id, int numero, int capacidade, String status) {

    public static MesaResponse from(Mesa mesa) {
        return new MesaResponse(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidade(),
                mesa.getStatus().name().toLowerCase(Locale.ROOT));
    }
}