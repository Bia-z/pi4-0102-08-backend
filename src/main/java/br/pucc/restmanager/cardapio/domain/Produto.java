package br.pucc.restmanager.cardapio.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 120, unique = true)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagemProduto> imagens = new ArrayList<>();

    protected Produto() {
    }

    public Produto(
            Categoria categoria,
            String nome,
            String descricao,
            BigDecimal preco,
            boolean ativo) {
        this.categoria = categoria;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.ativo = ativo;
        this.criadoEm = LocalDateTime.now();
    }

    public void adicionarImagemPrincipal(String caminho) {
        if (caminho != null && !caminho.isBlank()) {
            imagens.add(new ImagemProduto(this, caminho, true, 0));
        }
    }

    public void atualizar(
            Categoria categoria,
            String nome,
            String descricao,
            BigDecimal preco,
            boolean ativo,
            String imagemUrl) {
        this.categoria = categoria;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.ativo = ativo;
        this.imagens.clear();
        adicionarImagemPrincipal(imagemUrl);
    }

    public void desativar() {
        ativo = false;
    }

    public Long getId() {
        return id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getImagemUrl() {
        return imagens.stream()
                .filter(ImagemProduto::isPrincipal)
                .map(ImagemProduto::getCaminho)
                .findFirst()
                .orElse(null);
    }
}