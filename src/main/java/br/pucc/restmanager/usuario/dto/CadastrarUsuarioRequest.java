package br.pucc.restmanager.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarUsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 14) String cpf,
        @NotBlank @Size(max = 20) String telefone,
        @Size(max = 20) String codigo,
        @NotBlank String perfil,
        @NotBlank String senha) {
}