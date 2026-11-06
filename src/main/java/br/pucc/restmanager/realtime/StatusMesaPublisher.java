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
        publicarStatusAtualizado(mesaId, numero, "disponivel");
    }

    public void publicarStatusAtualizado(Long mesaId, int numero, String status) {
        messaging.convertAndSend(
                "/topic/mesas",
                new StatusMesaMessage(
                        "statusMesaAtualizado",
                        mesaId,
                        numero,
                        status,
                        LocalDateTime.now()));
    }
}