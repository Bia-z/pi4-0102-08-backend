package br.pucc.restmanager.realtime;

import br.pucc.restmanager.realtime.dto.StatusMesaMessage;
import java.time.LocalDateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatusMesaPublisher {

    private final SimpMessagingTemplate messaging;

    public StatusMesaPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publicarDisponivel(Long mesaId, int numero) {
        messaging.convertAndSend(
                "/topic/mesas",
                new StatusMesaMessage(
                        "statusMesaAtualizado",
                        mesaId,
                        numero,
                        "disponivel",
                        LocalDateTime.now()));
    }
}