package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConfiguracaoRepository {

    private static final String COLUNAS = """
            nome_restaurante, taxa_servico_percentual, taxa_servico_ativa,
            imprimir_comanda_ao_aceitar, comanda_unica, imprimir_comprovante_ao_fechar
            """;

    private final JdbcTemplate jdbc;

    public ConfiguracaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ConfiguracaoResponse buscar() {
        jdbc.update("INSERT INTO tb_configuracao (id) VALUES (1) ON CONFLICT (id) DO NOTHING");
        return jdbc.queryForObject("SELECT " + COLUNAS + " FROM tb_configuracao WHERE id = 1",
                (rs, row) -> configuracao(rs));
    }

    public ConfiguracaoResponse atualizar(
            String nome, BigDecimal taxaServico, boolean taxaServicoAtiva, boolean imprimirComanda,
            boolean comandaUnica, boolean imprimirComprovante) {
        return jdbc.queryForObject("""
                        INSERT INTO tb_configuracao (id, nome_restaurante, taxa_servico_percentual,
                                                     taxa_servico_ativa, imprimir_comanda_ao_aceitar,
                                                     comanda_unica, imprimir_comprovante_ao_fechar)
                        VALUES (1, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (id) DO UPDATE SET
                            nome_restaurante = EXCLUDED.nome_restaurante,
                            taxa_servico_percentual = EXCLUDED.taxa_servico_percentual,
                            taxa_servico_ativa = EXCLUDED.taxa_servico_ativa,
                            imprimir_comanda_ao_aceitar = EXCLUDED.imprimir_comanda_ao_aceitar,
                            comanda_unica = EXCLUDED.comanda_unica,
                            imprimir_comprovante_ao_fechar = EXCLUDED.imprimir_comprovante_ao_fechar
                        RETURNING
                        """ + COLUNAS,
                (rs, row) -> configuracao(rs),
                nome, taxaServico, taxaServicoAtiva, imprimirComanda, comandaUnica, imprimirComprovante);
    }

    private ConfiguracaoResponse configuracao(ResultSet rs) throws SQLException {
        return new ConfiguracaoResponse(
                rs.getString("nome_restaurante"),
                rs.getBigDecimal("taxa_servico_percentual"),
                rs.getBoolean("taxa_servico_ativa"),
                rs.getBoolean("imprimir_comanda_ao_aceitar"),
                rs.getBoolean("comanda_unica"),
                rs.getBoolean("imprimir_comprovante_ao_fechar"));
    }
}
