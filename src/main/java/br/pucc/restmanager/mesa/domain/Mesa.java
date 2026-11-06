package br.pucc.restmanager.mesa.domain;

import br.pucc.restmanager.usuario.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMesa status;

    @Column(nullable = false)
    private boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_iniciado_por")
    private Usuario pagamentoIniciadoPor;

    private LocalDateTime pagamentoIniciadoEm;

    @Version
    private Long version;

    protected Mesa() {
    }

    public Mesa(int numero, int capacidade) {
        this.numero = numero;
        this.capacidade = capacidade;
        this.status = StatusMesa.DISPONIVEL;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public StatusMesa getStatus() {
        return status;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Usuario getPagamentoIniciadoPor() {
        return pagamentoIniciadoPor;
    }

    public LocalDateTime getPagamentoIniciadoEm() {
        return pagamentoIniciadoEm;
    }

    public Long getVersion() {
        return version;
    }

    public void ocupar() {
        if (status == StatusMesa.DISPONIVEL || status == StatusMesa.RESERVADA) {
            status = StatusMesa.OCUPADA;
        }
    }
}