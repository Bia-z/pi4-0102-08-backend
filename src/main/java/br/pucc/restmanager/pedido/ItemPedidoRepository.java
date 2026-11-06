package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}