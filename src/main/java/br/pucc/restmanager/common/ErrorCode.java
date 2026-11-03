package br.pucc.restmanager.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    CREDENCIAIS_INVALIDAS(HttpStatus.UNAUTHORIZED),
    USUARIO_INATIVO(HttpStatus.FORBIDDEN);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}