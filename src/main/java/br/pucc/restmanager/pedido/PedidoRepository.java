package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.domain.Pedido;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query(value = "SELECT taxa_servico_percentual FROM tb_configuracao WHERE id = 1",
            nativeQuery = true)
    BigDecimal taxaServicoPercentual();
}