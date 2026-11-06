package br.pucc.restmanager.transferencia;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransferenciaMesaRepository {

    private final JdbcTemplate jdbc;

    public TransferenciaMesaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> buscarMesaDoPedido(Long pedidoId) {
        return jdbc.query("""
                        SELECT mesa_id
                        FROM tb_pedido
                        WHERE id = ?
                        """,
                (rs, rowNum) -> rs.getLong("mesa_id"),
                pedidoId).stream().findFirst();
    }

    public List<MesaTransferencia> bloquearMesas(Long mesaOrigemId, Long mesaDestinoId) {
        return jdbc.query("""
                        SELECT id, numero, status
                        FROM tb_mesa
                        WHERE ativo = TRUE AND id IN (?, ?)
                        ORDER BY id
                        FOR UPDATE
                        """,
                (rs, rowNum) -> new MesaTransferencia(
                        rs.getLong("id"),
                        rs.getInt("numero"),
                        rs.getString("status")),
                mesaOrigemId,
                mesaDestinoId);
    }

    public Optional<PedidoTransferencia> bloquearPedido(Long pedidoId) {
        return jdbc.query("""
                        SELECT id, mesa_id, status
                        FROM tb_pedido
                        WHERE id = ?
                        FOR UPDATE
                        """,
                (rs, rowNum) -> new PedidoTransferencia(
                        rs.getLong("id"),
                        rs.getLong("mesa_id"),
                        rs.getString("status")),
                pedidoId).stream().findFirst();
    }

    public boolean existePedidoAberto(Long mesaId) {
        Boolean existe = jdbc.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM tb_pedido
                            WHERE mesa_id = ?
                              AND status NOT IN ('PAGO', 'CANCELADO')
                        )
                        """,
                Boolean.class,
                mesaId);
        return Boolean.TRUE.equals(existe);
    }

    public void moverPedido(Long pedidoId, Long mesaDestinoId) {
        jdbc.update("UPDATE tb_pedido SET mesa_id = ? WHERE id = ?", mesaDestinoId, pedidoId);
    }

    public void atualizarStatusMesa(Long mesaId, String status) {
        jdbc.update(
                "UPDATE tb_mesa SET status = ?, version = version + 1 WHERE id = ?",
                status,
                mesaId);
    }

    public void registrarTransferencia(
            Long pedidoId,
            Long mesaOrigemId,
            Long mesaDestinoId,
            Long usuarioId,
            LocalDateTime transferidoEm) {
        jdbc.update("""
                        INSERT INTO tb_transferencia_mesa
                            (pedido_id, mesa_origem_id, mesa_destino_id, usuario_id, data_hora)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                pedidoId,
                mesaOrigemId,
                mesaDestinoId,
                usuarioId,
                Timestamp.valueOf(transferidoEm));
    }

    public record MesaTransferencia(Long id, int numero, String status) {
    }

    public record PedidoTransferencia(Long id, Long mesaId, String status) {
    }
}