package br.pucc.restmanager.cardapio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, unique = true)
    private String nome;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private boolean ativo;

    protected Categoria() {
    }

    public Categoria(String nome, int ordem, boolean ativo) {
        this.nome = nome;
        this.ordem = ordem;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getOrdem() {
        return ordem;
    }

    public boolean isAtivo() {
        return ativo;
    }
}