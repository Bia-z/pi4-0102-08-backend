package br.pucc.restmanager.mesa;

import br.pucc.restmanager.mesa.domain.Mesa;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    boolean existsByNumero(int numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mesa m WHERE m.id = :id AND m.ativo = true")
    Optional<Mesa> buscarAtivaPorIdComBloqueio(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT m FROM Mesa m
            WHERE m.ativo = true AND m.status = br.pucc.restmanager.mesa.domain.StatusMesa.EM_PAGAMENTO
              AND m.pagamentoIniciadoPor.id = :operadorId
            """)
    List<Mesa> buscarPagamentosIniciadosPorComBloqueio(@Param("operadorId") Long operadorId);

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