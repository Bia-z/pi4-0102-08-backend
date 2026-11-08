package br.pucc.restmanager.comanda;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.caixa.ConfiguracaoService;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import br.pucc.restmanager.comanda.dto.ComandaResumoResponse;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ImpressaoServiceTest {

    @Mock
    private ComandaService comandas;

    @Mock
    private ConfiguracaoService configuracoes;

    @Mock
    private SimpMessagingTemplate messaging;

    @Test
    void geraPdfComDadosDoPedido() {
        when(comandas.obter(7L)).thenReturn(comanda());
        var service = new ImpressaoService(comandas, configuracoes, messaging);

        String conteudo = new String(service.gerarPdf(7L), Charset.forName("windows-1252"));

        assertTrue(conteudo.startsWith("%PDF-1.4"));
        assertTrue(conteudo.contains("COMANDA DO PEDIDO 7"));
        assertTrue(conteudo.contains("Mesa: 12"));
        assertTrue(conteudo.contains("2 x Suco"));
        assertTrue(conteudo.contains("Sem gelo"));
    }

    @Test
    void publicaComandaNaCozinhaQuandoOpcaoEstaHabilitada() {
        when(configuracoes.buscar()).thenReturn(new ConfiguracaoResponse("Restaurante", null, true));
        when(comandas.obter(7L)).thenReturn(comanda());
        var service = new ImpressaoService(comandas, configuracoes, messaging);

        service.publicarSeConfigurado(7L);

        verify(messaging).convertAndSend("/topic/comandas", comanda());
    }

    private ComandaResumoResponse comanda() {
        return new ComandaResumoResponse(
                7L, 12, "Garcom", LocalDateTime.of(2026, 11, 8, 12, 30),
                List.of(new ComandaResumoResponse.Item("Suco", 2, "Sem gelo", "aguardando")));
    }
}
