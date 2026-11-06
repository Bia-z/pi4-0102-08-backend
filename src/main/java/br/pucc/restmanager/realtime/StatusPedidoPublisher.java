package br.pucc.restmanager.realtime;

import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.realtime.dto.StatusPedidoMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class StatusPedidoPublisher {

    private final SimpMessagingTemplate messaging;

    public StatusPedidoPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publicar(ItemPedido item) {
        StatusPedidoMessage mensagem = StatusPedidoMessage.from(item);
        Runnable enviar = () -> messaging.convertAndSend("/topic/pedidos", mensagem);
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