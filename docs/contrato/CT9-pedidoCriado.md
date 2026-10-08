# CT9: pedidoCriado

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | WebSocket |
| **Quem chama** | Backend, sempre que um novo pedido é registrado |
| **Quem responde** | Frontend, nos painéis abertos da Cozinha. Não há resposta. |
| **Atende** | E4 |
| **Como chamar** | Canal `/topic/cozinha`, mensagem do tipo `pedidoCriado` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| numeroMesa | inteiro | sim | 10 |
| horario | data e hora | sim | 2026-10-13T14:32:00 |
| itens | lista de objeto ItemPedidoNotificacao | sim | [ { "nome": "Bruschetta Italiana", "quantidade": 2, "observacao": "Sem cebola" } ] |

#### objeto ItemPedidoNotificacao

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | Bruschetta Italiana |
| quantidade | inteiro | sim | 2 |
| observacao | texto | não | Sem cebola |

### Saída

não tem

### Erros

| Quando | O que volta |
| :--- | :--- |
| O painel da cozinha perde a conexão Wi-Fi | Nada. Ao reconectar, o frontend busca os pedidos pendentes via HTTP |
