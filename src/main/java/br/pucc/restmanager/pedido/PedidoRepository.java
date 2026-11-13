package br.pucc.restmanager.pedido;

import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.pedido.domain.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query(value = """
            SELECT CASE WHEN taxa_servico_ativa THEN taxa_servico_percentual ELSE 0 END
            FROM tb_configuracao WHERE id = 1
            """, nativeQuery = true)
    BigDecimal taxaServicoPercentual();

    boolean existsByMesaIdAndStatusNotIn(Long mesaId, Collection<StatusPedido> status);

    @Query("""
            select distinct p from Pedido p
            join fetch p.mesa
            join fetch p.garcom
            left join fetch p.itens i
            left join fetch i.produto
            where p.dataCriacao >= :inicio and p.dataCriacao < :fim
            order by p.dataCriacao desc, p.id desc
            """)
    List<Pedido> buscarCriadosEntre(
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            select distinct p from Pedido p
            join fetch p.mesa m
            join fetch p.garcom g
            left join fetch p.itens i
            left join fetch i.produto
            where p.status not in (
                br.pucc.restmanager.pedido.domain.StatusPedido.PAGO,
                br.pucc.restmanager.pedido.domain.StatusPedido.CANCELADO)
              and (:status is null or p.status = :status)
              and (:statusItem is null or exists (
                  select itemFiltro.id from ItemPedido itemFiltro
                  where itemFiltro.pedido = p and itemFiltro.status = :statusItem))
              and (:garcomId is null or g.id = :garcomId)
              and (:mesaId is null or m.id = :mesaId)
              and (:cozinha = false or exists (
                  select itemCozinha.id from ItemPedido itemCozinha
                  where itemCozinha.pedido = p
                    and itemCozinha.status in (
                        br.pucc.restmanager.pedido.domain.StatusItem.AGUARDANDO,
                        br.pucc.restmanager.pedido.domain.StatusItem.EM_PREPARO)))
            order by p.dataCriacao asc, p.id asc
            """)
    List<Pedido> listarAbertos(
            @Param("status") StatusPedido status,
            @Param("statusItem") StatusItem statusItem,
            @Param("garcomId") Long garcomId,
            @Param("mesaId") Long mesaId,
            @Param("cozinha") boolean cozinha);
}