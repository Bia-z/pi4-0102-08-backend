package br.pucc.restmanager.mesa;

import br.pucc.restmanager.mesa.domain.Mesa;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    boolean existsByNumero(int numero);

    @Query(value = """
            SELECT m.id AS "mesaId",
                   m.numero AS numero,
                   LOWER(m.status) AS status,
                   COALESCE(
                       ROUND(
                           SUM(i.quantidade * i.preco_unitario)
                               * (1 + COALESCE(c.taxa_servico_percentual, 0) / 100),
                           2
                       ),
                       0
                   ) AS "valorConsumido"
            FROM tb_mesa m
            LEFT JOIN tb_pedido p
                ON p.mesa_id = m.id
                AND p.status NOT IN ('PAGO', 'CANCELADO')
            LEFT JOIN tb_item_pedido i
                ON i.pedido_id = p.id
                AND i.status <> 'CANCELADO'
            LEFT JOIN tb_configuracao c ON c.id = 1
            WHERE m.ativo = TRUE
            GROUP BY m.id, m.numero, m.status, c.taxa_servico_percentual
            ORDER BY m.numero
            """, nativeQuery = true)
    List<MesaMapaProjection> obterMapaMesas();

    interface MesaMapaProjection {
        Long getMesaId();

        Integer getNumero();

        String getStatus();

        BigDecimal getValorConsumido();
    }
}