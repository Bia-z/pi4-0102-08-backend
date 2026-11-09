package br.pucc.restmanager.auth.dto;

public record LoginResponse(String token, String tipoToken, String perfil, Long usuarioId) {
}
