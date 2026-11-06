package br.pucc.restmanager.realtime;

import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.realtime.dto.PedidoCriadoMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class PedidoCriadoPublisher {

    private final SimpMessagingTemplate messaging;

    public PedidoCriadoPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publicar(Pedido pedido) {
        PedidoCriadoMessage mensagem = PedidoCriadoMessage.from(pedido);
        Runnable enviar = () -> messaging.convertAndSend("/topic/cozinha", mensagem);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar.run();
                }
            });
        } else {
            enviar.run();
        }
    }
}