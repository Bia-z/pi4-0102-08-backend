package br.pucc.restmanager.realtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.pedido.domain.ItemPedido;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.realtime.dto.PedidoCriadoMessage;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
class PedidoCriadoPublisherTest {

    @Mock
    SimpMessagingTemplate messaging;

    private PedidoCriadoPublisher publisher;

    @BeforeEach
    void montar() {
        publisher = new PedidoCriadoPublisher(messaging);
    }

    @Test
    void publicaPedidoComMesaItensEObservacoesNoCanalDaCozinha() {
        Mesa mesa = new Mesa(10, 4);
        ReflectionTestUtils.setField(mesa, "id", 5L);
        Usuario garcom = new Usuario("g1", "Garcom", null, "hash", Perfil.GARCOM, true);
        Produto produto = new Produto(
                new Categoria("Pratos", 1, true),
                "Prato feito",
                null,
                new BigDecimal("20.00"),
                true,
                Set.of());
        Pedido pedido = new Pedido(mesa, garcom, new BigDecimal("22.00"));
        ReflectionTestUtils.setField(pedido, "id", 101L);
        ReflectionTestUtils.setField(
                pedido, "dataCriacao", LocalDateTime.of(2026, 11, 6, 12, 0));
        pedido.adicionarItem(new ItemPedido(produto, 2, "Sem molho"));
        pedido.adicionarItem(new ItemPedido(produto, 1, null));

        publisher.publicar(pedido);

        ArgumentCaptor<PedidoCriadoMessage> mensagem =
                ArgumentCaptor.forClass(PedidoCriadoMessage.class);
        verify(messaging).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/cozinha"),
                mensagem.capture());
        assertEquals("pedidoCriado", mensagem.getValue().tipo());
        assertEquals(101L, mensagem.getValue().pedidoId());
        assertEquals(10, mensagem.getValue().numeroMesa());
        assertEquals(LocalDateTime.of(2026, 11, 6, 12, 0), mensagem.getValue().horario());
        assertEquals(
                List.of(
                        new PedidoCriadoMessage.Item("Prato feito", 2, "Sem molho"),
                        new PedidoCriadoMessage.Item("Prato feito", 1, null)),
                mensagem.getValue().itens());
    }

    @Test
    void publicaSomenteDepoisDoCommitDaTransacao() {
        Pedido pedido = pedidoSimples();
        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publicar(pedido);
            verifyNoInteractions(messaging);

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);

            verify(messaging).convertAndSend(
                    org.mockito.ArgumentMatchers.eq("/topic/cozinha"),
                    org.mockito.ArgumentMatchers.any(PedidoCriadoMessage.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private Pedido pedidoSimples() {
        Mesa mesa = new Mesa(10, 4);
        Usuario garcom = new Usuario("g1", "Garcom", null, "hash", Perfil.GARCOM, true);
        Produto produto = new Produto(
                new Categoria("Pratos", 1, true),
                "Prato feito",
                null,
                new BigDecimal("20.00"),
                true,
                Set.of());
        Pedido pedido = new Pedido(mesa, garcom, new BigDecimal("22.00"));
        ReflectionTestUtils.setField(pedido, "id", 101L);
        pedido.adicionarItem(new ItemPedido(produto, 1, null));
        return pedido;
    }
}