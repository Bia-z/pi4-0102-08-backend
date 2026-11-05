package br.pucc.restmanager.realtime;

import br.pucc.restmanager.pedido.PainelCozinhaService;
import br.pucc.restmanager.pedido.PedidoCriadoEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PedidoCriadoPublisher {

    static final String CANAL = "/topic/cozinha";

    private final SimpMessagingTemplate mensagens;
    private final PainelCozinhaService painel;

    public PedidoCriadoPublisher(SimpMessagingTemplate mensagens, PainelCozinhaService painel) {
        this.mensagens = mensagens;
        this.painel = painel;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarPedido(PedidoCriadoEvent evento) {
        mensagens.convertAndSend(CANAL, painel.buscar(evento.pedidoId()));
    }
}
