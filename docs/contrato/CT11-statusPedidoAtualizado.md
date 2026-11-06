# CT11: statusPedidoAtualizado

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | WebSocket |
| **Quem chama** | Backend, sempre que o status de um pedido é alterado |
| **Quem responde** | Frontend, em todos os painéis abertos (Garçom, Cozinha e Caixa). Não há resposta. |
| **Atende** | E5 |
| **Como chamar** | Canal `/topic/pedidos`, mensagem do tipo `statusPedidoAtualizado` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| itemId | inteiro | sim | 201 |
| numeroMesa | inteiro | sim | 10 |
| status | um de: aguardando, em_preparo, pronto | sim | pronto |
| atualizadoEm | data e hora | sim | 2026-10-13T14:35:00 |

### Saída

não tem

### Erros

| Quando | O que volta |
| :--- | :--- |
| O dispositivo do garçom estava desconectado na hora do envio | Nada. Ao interagir com a tela, o frontend atualiza a lista de pedidos |
