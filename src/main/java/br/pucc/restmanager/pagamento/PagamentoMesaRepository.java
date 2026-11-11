package br.pucc.restmanager.pagamento;

import br.pucc.restmanager.pagamento.domain.FormaPagamento;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PagamentoMesaRepository {

    private final JdbcTemplate jdbc;

    public PagamentoMesaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<MesaPagamento> bloquearMesa(Long mesaId) {
        return jdbc.query("""
                        SELECT id, numero, status
                        FROM tb_mesa
                        WHERE id = ? AND ativo = TRUE
                        FOR UPDATE
                        """,
                (rs, rowNum) -> new MesaPagamento(
                        rs.getLong("id"),
                        rs.getInt("numero"),
                        rs.getString("status")),
                mesaId).stream().findFirst();
    }

    public List<PedidoPagamento> buscarPedidosAbertos(Long mesaId) {
        return jdbc.query("""
                        SELECT p.id,
                               COALESCE(SUM(
                                   CASE WHEN i.status <> 'CANCELADO'
                                       THEN i.quantidade * i.preco_unitario
                                       ELSE 0
                                   END
                               ), 0) AS subtotal
                        FROM tb_pedido p
                        LEFT JOIN tb_item_pedido i ON i.pedido_id = p.id
                        WHERE p.mesa_id = ?
                          AND p.status NOT IN ('PAGO', 'CANCELADO')
                        GROUP BY p.id
                        ORDER BY p.id
                        """,
                (rs, rowNum) -> new PedidoPagamento(
                        rs.getLong("id"),
                        rs.getBigDecimal("subtotal")),
                mesaId);
    }

    public boolean existePedidoPago(Long mesaId) {
        Boolean existe = jdbc.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1 FROM tb_pedido
                            WHERE mesa_id = ? AND status = 'PAGO'
                        )
                        """,
                Boolean.class,
                mesaId);
        return Boolean.TRUE.equals(existe);
    }

    public BigDecimal taxaServicoPercentual() {
        return jdbc.queryForObject(
                "SELECT CASE WHEN taxa_servico_ativa THEN taxa_servico_percentual ELSE 0 END "
                        + "FROM tb_configuracao WHERE id = 1",
                BigDecimal.class);
    }

    public void bloquearCaixaAberto() {
        jdbc.query("SELECT id FROM tb_caixa WHERE status = 'ABERTO' FOR UPDATE",
                (rs, row) -> rs.getLong("id"));
    }

    public void gravarPagamento(
            Long pedidoId,
            FormaPagamento forma,
            BigDecimal valor,
            BigDecimal troco) {
        jdbc.update("""
                        INSERT INTO tb_pagamento (pedido_id, caixa_id, forma_pagamento, valor, troco)
                        VALUES (?, (SELECT id FROM tb_caixa WHERE status = 'ABERTO'), ?, ?, ?)
                        """,
                pedidoId,
                forma.name(),
                valor,
                troco);
    }

    public void marcarPedidosPagos(Long mesaId) {
        jdbc.update("""
                        UPDATE tb_pedido
                        SET status = 'PAGO'
                        WHERE mesa_id = ? AND status NOT IN ('PAGO', 'CANCELADO')
                        """,
                mesaId);
    }

    public void liberarMesa(Long mesaId) {
        jdbc.update("""
                        UPDATE tb_mesa
                        SET status = 'DISPONIVEL',
                            pagamento_iniciado_por = NULL,
                            pagamento_iniciado_em = NULL,
                            version = version + 1
                        WHERE id = ?
                        """,
                mesaId);
    }

    public record MesaPagamento(Long id, int numero, String status) {
    }

    public record PedidoPagamento(Long id, BigDecimal subtotal) {
    }
}
