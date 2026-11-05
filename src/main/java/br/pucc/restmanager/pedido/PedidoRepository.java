package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("select distinct p from Pedido p join p.itens i where i.status in :status order by p.dataCriacao")
    List<Pedido> findComItensNosStatus(Collection<StatusItem> status);
}
