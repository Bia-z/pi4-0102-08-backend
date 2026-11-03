package br.pucc.restmanager.usuario.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nome;
    private String email;
    private String senha; // hash BCrypt, nunca a senha pura

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    private boolean ativo;

    protected Usuario() {
        // exigido pelo JPA
    }

    public Usuario(String codigo, String nome, String email, String senha, Perfil perfil, boolean ativo) {
        this.codigo = codigo;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public Perfil getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
}