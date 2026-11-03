package br.pucc.restmanager.common;

public class BusinessException extends RuntimeException {
    private final ErrorCode erro;

    public BusinessException(ErrorCode erro) {
        super(erro.name());
        this.erro = erro;
    }

    public ErrorCode getErro() {
        return erro;
    }
}