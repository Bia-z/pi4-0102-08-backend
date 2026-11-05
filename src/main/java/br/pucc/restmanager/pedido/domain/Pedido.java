package br.pucc.restmanager.pedido.domain;

import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.usuario.domain.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garcom_id", nullable = false)
    private Usuario garcom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Mesa mesa, Usuario garcom) {
        this.mesa = mesa;
        this.garcom = garcom;
        this.status = StatusPedido.AGUARDANDO;
        this.valorTotal = BigDecimal.ZERO;
        this.dataCriacao = LocalDateTime.now();
    }

    public ItemPedido adicionarItem(Produto produto, int quantidade, String observacao) {
        ItemPedido item = new ItemPedido(this, produto, quantidade, observacao);
        itens.add(item);
        valorTotal = valorTotal.add(item.getSubtotal());
        return item;
    }

    public Long getId() {
        return id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public Usuario getGarcom() {
        return garcom;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }
}
