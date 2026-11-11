package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.CaixaResponse;
import br.pucc.restmanager.caixa.dto.MovimentacaoResponse;
import br.pucc.restmanager.caixa.dto.ResumoTurnoResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CaixaRepository {

    private static final String COLUNAS_CAIXA = """
            c.id, c.operador_id, c.turno, c.status, c.valor_abertura, c.aberto_em,
            c.valor_fechamento, c.valor_esperado, c.fechado_em
            """;

    private final JdbcTemplate jdbc;

    public CaixaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<CaixaResponse> caixaAberto() {
        return jdbc.query("SELECT " + COLUNAS_CAIXA + " FROM tb_caixa c WHERE c.status = 'ABERTO'",
                (rs, row) -> caixa(rs.getLong("id"), rs.getLong("operador_id"),
                        rs.getString("turno"), rs.getString("status"),
                        rs.getBigDecimal("valor_abertura"), rs.getTimestamp("aberto_em"),
                        rs.getBigDecimal("valor_fechamento"), rs.getBigDecimal("valor_esperado"),
                        rs.getTimestamp("fechado_em"))).stream().findFirst();
    }

    public Optional<CaixaResponse> caixaAbertoComBloqueio() {
        return jdbc.query("SELECT " + COLUNAS_CAIXA
                        + " FROM tb_caixa c WHERE c.status = 'ABERTO' FOR UPDATE",
                (rs, row) -> caixa(rs.getLong("id"), rs.getLong("operador_id"),
                        rs.getString("turno"), rs.getString("status"),
                        rs.getBigDecimal("valor_abertura"), rs.getTimestamp("aberto_em"),
                        rs.getBigDecimal("valor_fechamento"), rs.getBigDecimal("valor_esperado"),
                        rs.getTimestamp("fechado_em"))).stream().findFirst();
    }

    public CaixaResponse abrir(String codigoOperador, String turno, BigDecimal valorAbertura) {
        Long id = jdbc.queryForObject("""
                        INSERT INTO tb_caixa (operador_id, turno, valor_abertura)
                        SELECT id, ?, ? FROM tb_usuario WHERE codigo = ? AND ativo = TRUE
                        RETURNING id
                        """,
                Long.class,
                turno, valorAbertura, codigoOperador);
        return buscarPorId(id).orElseThrow();
    }

    public BigDecimal calcularValorEsperado(Long caixaId) {
        return jdbc.queryForObject("""
                        SELECT c.valor_abertura
                           + COALESCE((
                               SELECT SUM(pg.valor - pg.troco)
                               FROM tb_pagamento pg
                               WHERE pg.caixa_id = c.id AND pg.forma_pagamento = 'DINHEIRO'
                           ), 0)
                           + COALESCE((
                               SELECT SUM(CASE WHEN mc.tipo = 'REFORCO' THEN mc.valor ELSE -mc.valor END)
                               FROM tb_movimentacao_caixa mc WHERE mc.caixa_id = c.id
                           ), 0)
                        FROM tb_caixa c WHERE c.id = ?
                        """,
                BigDecimal.class,
                caixaId);
    }

    public Optional<CaixaResponse> fechar(Long caixaId, BigDecimal valorFechamento, BigDecimal esperado) {
        int atualizados = jdbc.update("""
                        UPDATE tb_caixa
                        SET status = 'FECHADO', valor_fechamento = ?, valor_esperado = ?,
                            fechado_em = CURRENT_TIMESTAMP
                        WHERE id = ? AND status = 'ABERTO'
                        """,
                valorFechamento, esperado, caixaId);
        return atualizados == 0 ? Optional.empty() : buscarPorId(caixaId);
    }

    public boolean existeMesaEmAberto() {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1 FROM tb_mesa
                            WHERE ativo = TRUE AND status IN ('OCUPADA', 'EM_PAGAMENTO')
                        )
                        """,
                Boolean.class));
    }

    public Optional<ResumoTurnoResponse> resumo(Long caixaId) {
        return buscarPorId(caixaId).map(caixa -> jdbc.queryForObject("""
                        SELECT COALESCE(SUM(pg.valor - pg.troco), 0) AS recebido,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'DINHEIRO'), 0) AS dinheiro,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'CREDITO'), 0) AS credito,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'DEBITO'), 0) AS debito,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'PIX'), 0) AS pix,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'VALE'), 0) AS vale,
                               COUNT(pg.id) AS quantidade,
                               (SELECT COALESCE(SUM(mc.valor), 0) FROM tb_movimentacao_caixa mc
                                WHERE mc.caixa_id = ? AND mc.tipo = 'SANGRIA') AS sangrias,
                               (SELECT COALESCE(SUM(mc.valor), 0) FROM tb_movimentacao_caixa mc
                                WHERE mc.caixa_id = ? AND mc.tipo = 'REFORCO') AS reforcos
                        FROM tb_pagamento pg
                        WHERE pg.caixa_id = ?
                        """,
                (rs, row) -> new ResumoTurnoResponse(caixa,
                        rs.getBigDecimal("recebido"), rs.getBigDecimal("dinheiro"),
                        rs.getBigDecimal("credito"), rs.getBigDecimal("debito"),
                        rs.getBigDecimal("pix"), rs.getBigDecimal("vale"),
                        rs.getLong("quantidade"), rs.getBigDecimal("sangrias"),
                        rs.getBigDecimal("reforcos")),
                caixaId, caixaId, caixaId));
    }

    public List<CaixaResponse> historico() {
        return jdbc.query("SELECT " + COLUNAS_CAIXA
                        + " FROM tb_caixa c WHERE c.status = 'FECHADO' ORDER BY c.fechado_em DESC",
                (rs, row) -> caixa(rs.getLong("id"), rs.getLong("operador_id"),
                        rs.getString("turno"), rs.getString("status"),
                        rs.getBigDecimal("valor_abertura"), rs.getTimestamp("aberto_em"),
                        rs.getBigDecimal("valor_fechamento"), rs.getBigDecimal("valor_esperado"),
                        rs.getTimestamp("fechado_em")));
    }

    public long idOperador(String codigo) {
        Long id = jdbc.queryForObject(
                "SELECT id FROM tb_usuario WHERE codigo = ? AND ativo = TRUE",
                Long.class, codigo);
        return id;
    }

    public MovimentacaoResponse movimentar(
            Long caixaId, long usuarioId, String tipo, BigDecimal valor, String motivo) {
        return jdbc.queryForObject("""
                        INSERT INTO tb_movimentacao_caixa (caixa_id, usuario_id, tipo, valor, motivo)
                        VALUES (?, ?, ?, ?, ?) RETURNING id, caixa_id, tipo, valor, motivo, data_hora
                        """,
                (rs, row) -> movimentacao(
                        rs.getLong("id"), rs.getLong("caixa_id"), rs.getString("tipo"),
                        rs.getBigDecimal("valor"), rs.getString("motivo"), rs.getTimestamp("data_hora")),
                caixaId, usuarioId, tipo, valor, motivo);
    }

    public List<MovimentacaoResponse> movimentacoes(Long caixaId) {
        return jdbc.query("""
                        SELECT id, caixa_id, tipo, valor, motivo, data_hora
                        FROM tb_movimentacao_caixa WHERE caixa_id = ? ORDER BY data_hora, id
                        """,
                (rs, row) -> movimentacao(rs.getLong("id"), rs.getLong("caixa_id"),
                        rs.getString("tipo"), rs.getBigDecimal("valor"),
                        rs.getString("motivo"), rs.getTimestamp("data_hora")),
                caixaId);
    }

    public RelatorioValores relatorio(LocalDate data) {
        return jdbc.queryForObject("""
                        SELECT COALESCE(SUM(pg.valor - pg.troco), 0) AS recebido,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'DINHEIRO'), 0) AS dinheiro,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'CREDITO'), 0) AS credito,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'DEBITO'), 0) AS debito,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'PIX'), 0) AS pix,
                               COALESCE(SUM(pg.valor - pg.troco) FILTER
                                   (WHERE pg.forma_pagamento = 'VALE'), 0) AS vale,
                               COUNT(pg.id) AS quantidade
                        FROM tb_pagamento pg
                        WHERE pg.data_pagamento >= ? AND pg.data_pagamento < ?
                        """,
                (rs, row) -> new RelatorioValores(
                        rs.getBigDecimal("recebido"), rs.getBigDecimal("dinheiro"),
                        rs.getBigDecimal("credito"), rs.getBigDecimal("debito"),
                        rs.getBigDecimal("pix"), rs.getBigDecimal("vale"),
                        rs.getLong("quantidade")),
                Timestamp.valueOf(data.atStartOfDay()),
                Timestamp.valueOf(data.plusDays(1).atStartOfDay()));
    }

    public List<MovimentacaoResponse> movimentacoes(LocalDate data) {
        return jdbc.query("""
                        SELECT id, caixa_id, tipo, valor, motivo, data_hora
                        FROM tb_movimentacao_caixa
                        WHERE data_hora >= ? AND data_hora < ?
                        ORDER BY data_hora, id
                        """,
                (rs, row) -> movimentacao(rs.getLong("id"), rs.getLong("caixa_id"),
                        rs.getString("tipo"), rs.getBigDecimal("valor"),
                        rs.getString("motivo"), rs.getTimestamp("data_hora")),
                Timestamp.valueOf(data.atStartOfDay()),
                Timestamp.valueOf(data.plusDays(1).atStartOfDay()));
    }

    private Optional<CaixaResponse> buscarPorId(Long id) {
        return jdbc.query("SELECT " + COLUNAS_CAIXA + " FROM tb_caixa c WHERE c.id = ?",
                (rs, row) -> caixa(rs.getLong("id"), rs.getLong("operador_id"),
                        rs.getString("turno"), rs.getString("status"),
                        rs.getBigDecimal("valor_abertura"), rs.getTimestamp("aberto_em"),
                        rs.getBigDecimal("valor_fechamento"), rs.getBigDecimal("valor_esperado"),
                        rs.getTimestamp("fechado_em")), id).stream().findFirst();
    }

    private CaixaResponse caixa(
            Long id, Long operadorId, String turno, String status, BigDecimal abertura,
            Timestamp abertoEm, BigDecimal fechamento, BigDecimal esperado, Timestamp fechadoEm) {
        return new CaixaResponse(id, operadorId, turno, status, abertura,
                local(abertoEm), fechamento, esperado, local(fechadoEm));
    }

    private MovimentacaoResponse movimentacao(
            Long id, Long caixaId, String tipo, BigDecimal valor, String motivo, Timestamp dataHora) {
        return new MovimentacaoResponse(id, caixaId, tipo, valor, motivo, local(dataHora));
    }

    private java.time.LocalDateTime local(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    public record RelatorioValores(
            BigDecimal recebido, BigDecimal dinheiro, BigDecimal credito,
            BigDecimal debito, BigDecimal pix, BigDecimal vale, long quantidade) {
    }
}