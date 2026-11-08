package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.RelatorioDiaResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {

    private final CaixaRepository caixas;

    public RelatorioService(CaixaRepository caixas) {
        this.caixas = caixas;
    }

    public RelatorioDiaResponse gerar(LocalDate data) {
        CaixaRepository.RelatorioValores valores = caixas.relatorio(data);
        return new RelatorioDiaResponse(
                data, valores.recebido(), valores.dinheiro(), valores.credito(),
                valores.debito(), valores.pix(), valores.vale(), valores.quantidade(),
                caixas.movimentacoes(data));
    }
}