package br.pucc.restmanager.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.cardapio.domain.Categoria;
import br.pucc.restmanager.cardapio.domain.Produto;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.pedido.domain.Pedido;
import br.pucc.restmanager.realtime.dto.PedidoCriadoMessage;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PainelCozinhaServiceTest {

    @Mock
    PedidoRepository pedidos;

    @Test
    void montaMensagemDaCozinhaComMesaEItens() {
        Categoria categoria = new Categoria("Entradas", 1, true);
        Produto bruschetta = new Produto(categoria, "Bruschetta Italiana", null, new BigDecimal("28.00"), true, Set.of());
        Usuario garcom = new Usuario("garcom1", "Carlos", null, "hash", Perfil.GARCOM, true);
        Pedido pedido = new Pedido(new Mesa(10, 4), garcom);
        pedido.adicionarItem(bruschetta, 2, "Sem cebola");
        when(pedidos.findComItensNosStatus(anyCollection())).thenReturn(List.of(pedido));

        List<PedidoCriadoMessage> mensagens = new PainelCozinhaService(pedidos).listarEmAberto();

        PedidoCriadoMessage mensagem = mensagens.get(0);
        assertEquals(10, mensagem.numeroMesa());
        assertEquals("Bruschetta Italiana", mensagem.itens().get(0).nome());
        assertEquals(2, mensagem.itens().get(0).quantidade());
        assertEquals("Sem cebola", mensagem.itens().get(0).observacao());
        assertEquals("aguardando", mensagem.itens().get(0).status());
        assertEquals(new BigDecimal("56.00"), pedido.getValorTotal());
    }
}
