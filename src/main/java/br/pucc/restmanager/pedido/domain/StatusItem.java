package br.pucc.restmanager.pedido.domain;

public enum StatusItem {
    AGUARDANDO,
    EM_PREPARO,
    PRONTO,
    CANCELADO;

    public boolean permiteTransicaoPara(StatusItem novoStatus) {
        return this == AGUARDANDO && novoStatus == EM_PREPARO
                || this == EM_PREPARO && novoStatus == PRONTO;
    }
}