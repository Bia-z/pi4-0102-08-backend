package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select item from ItemPedido item
            join fetch item.pedido pedido
            join fetch pedido.mesa
            where item.id = :itemId and pedido.id = :pedidoId
            """)
    Optional<ItemPedido> findByIdAndPedidoId(
            @Param("itemId") Long itemId,
            @Param("pedidoId") Long pedidoId);
}