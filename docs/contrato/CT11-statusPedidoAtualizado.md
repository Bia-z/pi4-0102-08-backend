# CT11: statusPedidoAtualizado

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | WebSocket |
| **Quem chama** | Backend, sempre que o status de um pedido é alterado |
| **Quem responde** | Frontend, em todos os painéis abertos (Garçom e Cozinha). Não há resposta. |
| **Atende** | E5 |
| **Como chamar** | Canal `/topic/pedidos`, mensagem do tipo `statusPedidoAtualizado` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| numeroMesa | inteiro | sim | 10 |
| status | um de: aguardando, em_preparo, pronto, pago, cancelado | sim | pronto |

### Saída

não tem

### Erros

| Quando | O que volta |
| :--- | :--- |
| O dispositivo do garçom estava desconectado na hora do envio | Nada. Ao interagir com a tela, o frontend atualiza a lista de pedidos |
