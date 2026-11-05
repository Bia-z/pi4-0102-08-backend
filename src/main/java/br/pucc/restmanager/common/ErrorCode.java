package br.pucc.restmanager.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    CREDENCIAIS_INVALIDAS(HttpStatus.UNAUTHORIZED),
    TOKEN_AUSENTE(HttpStatus.UNAUTHORIZED),
    TOKEN_INVALIDO(HttpStatus.UNAUTHORIZED),
    USUARIO_INATIVO(HttpStatus.FORBIDDEN),
    ACESSO_NEGADO(HttpStatus.FORBIDDEN),
    CODIGO_DUPLICADO(HttpStatus.CONFLICT),
    PERFIL_INVALIDO(HttpStatus.BAD_REQUEST),
    CPF_INVALIDO(HttpStatus.BAD_REQUEST),
    CPF_DUPLICADO(HttpStatus.CONFLICT),
    TELEFONE_INVALIDO(HttpStatus.BAD_REQUEST),
    EMAIL_INVALIDO(HttpStatus.BAD_REQUEST),
    EMAIL_DUPLICADO(HttpStatus.CONFLICT),
    CATEGORIA_NAO_ENCONTRADA(HttpStatus.NOT_FOUND),
    PRECO_INVALIDO(HttpStatus.BAD_REQUEST),
    PRODUTO_DUPLICADO(HttpStatus.CONFLICT),
    INGREDIENTE_NAO_ENCONTRADO(HttpStatus.NOT_FOUND),
    MESA_DUPLICADA(HttpStatus.CONFLICT),
    CAPACIDADE_INVALIDA(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}