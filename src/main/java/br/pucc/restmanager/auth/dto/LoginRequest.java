package br.pucc.restmanager.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String codigo, @NotBlank String senha) {
}