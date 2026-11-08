package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConfiguracaoRepository {

    private final JdbcTemplate jdbc;

    public ConfiguracaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ConfiguracaoResponse buscar() {
        jdbc.update("""
                INSERT INTO tb_configuracao (id, nome_restaurante, taxa_servico_percentual,
                                             imprimir_comanda_ao_aceitar)
                VALUES (1, 'RestManager', 10.00, TRUE)
                ON CONFLICT (id) DO NOTHING
                """);
        return jdbc.queryForObject("""
                        SELECT nome_restaurante, taxa_servico_percentual, imprimir_comanda_ao_aceitar
                        FROM tb_configuracao WHERE id = 1
                        """,
                (rs, row) -> new ConfiguracaoResponse(
                        rs.getString("nome_restaurante"),
                        rs.getBigDecimal("taxa_servico_percentual"),
                        rs.getBoolean("imprimir_comanda_ao_aceitar")));
    }

    public ConfiguracaoResponse atualizar(
            String nome, BigDecimal taxaServico, boolean imprimirComanda) {
        return jdbc.queryForObject("""
                        INSERT INTO tb_configuracao (id, nome_restaurante, taxa_servico_percentual,
                                                     imprimir_comanda_ao_aceitar)
                        VALUES (1, ?, ?, ?)
                        ON CONFLICT (id) DO UPDATE SET
                            nome_restaurante = EXCLUDED.nome_restaurante,
                            taxa_servico_percentual = EXCLUDED.taxa_servico_percentual,
                            imprimir_comanda_ao_aceitar = EXCLUDED.imprimir_comanda_ao_aceitar
                        RETURNING nome_restaurante, taxa_servico_percentual, imprimir_comanda_ao_aceitar
                        """,
                (rs, row) -> new ConfiguracaoResponse(
                        rs.getString("nome_restaurante"),
                        rs.getBigDecimal("taxa_servico_percentual"),
                        rs.getBoolean("imprimir_comanda_ao_aceitar")),
                nome, taxaServico, imprimirComanda);
    }
}