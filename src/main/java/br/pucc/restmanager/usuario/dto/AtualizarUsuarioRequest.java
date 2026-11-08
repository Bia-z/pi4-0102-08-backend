package br.pucc.restmanager.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 160) String email,
        @NotBlank @Size(max = 14) String cpf,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank String perfil,
        @NotNull Boolean ativo,
        String senha) {
}