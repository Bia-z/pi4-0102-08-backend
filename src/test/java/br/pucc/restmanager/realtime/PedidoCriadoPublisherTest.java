package br.pucc.restmanager.realtime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.pedido.PainelCozinhaService;
import br.pucc.restmanager.pedido.PedidoCriadoEvent;
import br.pucc.restmanager.realtime.dto.PedidoCriadoMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class PedidoCriadoPublisherTest {

    @Mock
    SimpMessagingTemplate mensagens;

    @Mock
    PainelCozinhaService painel;

    @Test
    void enviaPedidoCriadoParaOCanalDaCozinha() {
        PedidoCriadoMessage mensagem = new PedidoCriadoMessage(101L, 10, LocalDateTime.now(), List.of());
        when(painel.buscar(101L)).thenReturn(mensagem);

        new PedidoCriadoPublisher(mensagens, painel).aoCriarPedido(new PedidoCriadoEvent(101L));

        verify(mensagens).convertAndSend("/topic/cozinha", mensagem);
    }
}
