package br.pucc.restmanager.realtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.pedido.domain.StatusItem;
import br.pucc.restmanager.realtime.dto.StatusPedidoMessage;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class StatusPedidoPublisherTest {

    @Mock
    SimpMessagingTemplate messaging;

    private StatusPedidoPublisher publisher;

    @BeforeEach
    void montar() {
        publisher = new StatusPedidoPublisher(messaging);
    }

    @AfterEach
    void limparTransacao() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publicaStatusDeItemAtualizadoNoCanalDePedidos() {
        ItemPedido item = itemAtualizado();

        publisher.publicar(item);

        ArgumentCaptor<StatusPedidoMessage> mensagem =
                ArgumentCaptor.forClass(StatusPedidoMessage.class);
        verify(messaging).convertAndSend(eq("/topic/pedidos"), mensagem.capture());
        assertEquals("statusPedidoAtualizado", mensagem.getValue().tipo());
        assertEquals(101L, mensagem.getValue().pedidoId());
        assertEquals(201L, mensagem.getValue().itemId());
        assertEquals(10, mensagem.getValue().numeroMesa());
        assertEquals("em_preparo", mensagem.getValue().status());
        assertEquals(LocalDateTime.of(2026, 11, 6, 12, 0), mensagem.getValue().atualizadoEm());
    }

    @Test
    void publicaSomenteDepoisDoCommitDaTransacao() {
        TransactionSynchronizationManager.initSynchronization();
        publisher.publicar(itemAtualizado());
        verifyNoInteractions(messaging);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        verify(messaging).convertAndSend(eq("/topic/pedidos"),
                org.mockito.ArgumentMatchers.any(StatusPedidoMessage.class));
    }

    private ItemPedido itemAtualizado() {
        Mesa mesa = new Mesa(10, 4);
        Pedido pedido = new Pedido(
                mesa,
                new Usuario("g1", "Garcom", null, "hash", Perfil.GARCOM, true),
                new BigDecimal("22.00"));
        ReflectionTestUtils.setField(pedido, "id", 101L);
        ItemPedido item = new ItemPedido(
                new br.pucc.restmanager.cardapio.domain.Produto(
                        new br.pucc.restmanager.cardapio.domain.Categoria("Pratos", 1, true),
                        "Prato feito", null, new BigDecimal("20.00"), true),
                1,
                null);
        ReflectionTestUtils.setField(item, "id", 201L);
        ReflectionTestUtils.setField(item, "status", StatusItem.EM_PREPARO);
        ReflectionTestUtils.setField(item, "atualizadoEm", LocalDateTime.of(2026, 11, 6, 12, 0));
        pedido.adicionarItem(item);
        return item;
    }
}