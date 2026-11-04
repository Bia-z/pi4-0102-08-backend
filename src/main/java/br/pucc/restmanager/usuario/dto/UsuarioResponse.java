package br.pucc.restmanager.usuario.dto;

import br.pucc.restmanager.usuario.domain.Usuario;
import java.util.Locale;

public record UsuarioResponse(
        Long id,
        String nome,
        String cpf,
        String telefone,
        String codigo,
        String perfil,
        boolean ativo) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getTelefone(),
                usuario.getCodigo(),
                usuario.getPerfil().name().toLowerCase(Locale.ROOT),
                usuario.isAtivo());
    }
}