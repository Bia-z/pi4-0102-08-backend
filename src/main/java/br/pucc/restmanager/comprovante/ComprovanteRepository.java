package br.pucc.restmanager.comprovante;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ComprovanteRepository {

    private final JdbcTemplate jdbc;

    public ComprovanteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PedidoReferencia> buscarPedido(Long pedidoId) {
        return jdbc.query("""
                        SELECT p.id, p.status, m.id AS mesa_id, m.numero AS numero_mesa
                        FROM tb_pedido p
                        JOIN tb_mesa m ON m.id = p.mesa_id
                        WHERE p.id = ?
                        """,
                (rs, rowNum) -> new PedidoReferencia(
                        rs.getLong("id"),
                        rs.getString("status"),
                        rs.getLong("mesa_id"),
                        rs.getInt("numero_mesa")),
                pedidoId).stream().findFirst();
    }

    public ComprovanteDados buscarDados(PedidoReferencia pedido) {
        List<Long> pedidoIds = buscarPedidosDaConta(pedido);
        if (pedidoIds.isEmpty()) {
            pedidoIds = List.of(pedido.id());
        }

        String placeholders = String.join(",", pedidoIds.stream().map(id -> "?").toList());
        Object[] argumentos = pedidoIds.toArray();
        List<ComprovanteDados.Item> itens = jdbc.query("""
                        SELECT pr.nome, i.quantidade, i.preco_unitario
                        FROM tb_item_pedido i
                        JOIN tb_produto pr ON pr.id = i.produto_id
                        WHERE i.status <> 'CANCELADO'
                          AND i.pedido_id IN (%s)
                        ORDER BY i.id
                        """.formatted(placeholders),
                (rs, rowNum) -> new ComprovanteDados.Item(
                        rs.getString("nome"),
                        rs.getInt("quantidade"),
                        rs.getBigDecimal("preco_unitario")),
                argumentos);
        List<ComprovanteDados.Pagamento> pagamentos = jdbc.query("""
                        SELECT forma_pagamento, valor, troco
                        FROM tb_pagamento
                        WHERE pedido_id IN (%s)
                        ORDER BY id
                        """.formatted(placeholders),
                (rs, rowNum) -> new ComprovanteDados.Pagamento(
                        rs.getString("forma_pagamento"),
                        rs.getBigDecimal("valor"),
                        rs.getBigDecimal("troco")),
                argumentos);

        BigDecimal subtotal = itens.stream()
                .map(ComprovanteDados.Item::subtotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        BigDecimal percentual = jdbc.queryForObject(
                "SELECT taxa_servico_percentual FROM tb_configuracao WHERE id = 1",
                BigDecimal.class);
        BigDecimal taxa = subtotal.multiply(percentual)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = pagamentos.stream()
                .map(ComprovanteDados.Pagamento::valorAplicado)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        Timestamp horario = jdbc.queryForObject("""
                        SELECT MAX(pg.data_pagamento)
                        FROM tb_pagamento pg
                        WHERE pg.pedido_id IN (%s)
                        """.formatted(placeholders),
                Timestamp.class,
                argumentos);
        return new ComprovanteDados(
                pedido.numeroMesa(),
                horario == null ? null : horario.toLocalDateTime(),
                percentual,
                subtotal,
                taxa,
                total,
                itens,
                pagamentos);
    }

    private List<Long> buscarPedidosDaConta(PedidoReferencia pedido) {
        return jdbc.queryForList("""
                        SELECT DISTINCT p.id
                        FROM tb_pedido p
                        JOIN tb_pagamento pg ON pg.pedido_id = p.id
                        WHERE p.mesa_id = ?
                          AND p.status = 'PAGO'
                          AND pg.data_pagamento IN (
                              SELECT pg_ref.data_pagamento
                              FROM tb_pagamento pg_ref
                              WHERE pg_ref.pedido_id = ?
                          )
                        ORDER BY p.id
                        """,
                Long.class,
                pedido.mesaId(),
                pedido.id());
    }

    public record PedidoReferencia(Long id, String status, Long mesaId, int numeroMesa) {
    }
}
