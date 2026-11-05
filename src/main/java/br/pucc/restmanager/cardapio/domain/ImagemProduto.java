package br.pucc.restmanager.cardapio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_imagem_produto")
public class ImagemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, length = 255)
    private String caminho;

    @Column(nullable = false)
    private boolean principal;

    @Column(nullable = false)
    private int ordem;

    protected ImagemProduto() {
    }

    public ImagemProduto(Produto produto, String caminho, boolean principal, int ordem) {
        this.produto = produto;
        this.caminho = caminho;
        this.principal = principal;
        this.ordem = ordem;
    }

    public String getCaminho() {
        return caminho;
    }

    public boolean isPrincipal() {
        return principal;
    }
}