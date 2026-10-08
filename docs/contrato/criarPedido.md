# CT7: criarPedido

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E3, E4 |
| **Como chamar** | POST `/api/pedidos` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| garcomId | inteiro | sim | 2 |
| itens | lista de objeto ItemPedidoEntrada | sim | [ { "produtoId": 10, "quantidade": 2, "observacao": "Sem cebola" } ] |

#### objeto ItemPedidoEntrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| produtoId | inteiro | sim | 10 |
| quantidade | inteiro | sim | 2 |
| observacao | texto | não | Sem cebola |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 101 |
| mesaId | inteiro | sim | 5 |
| status | um de: aguardando, em_preparo, pronto, pago, cancelado | sim | aguardando |
| valorTotal | decimal | sim | 56.00 |
| dataCriacao | data e hora | sim | 2026-10-13T14:32:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A mesa selecionada não existe | Erro MESA_NAO_ENCONTRADA |
| Um dos produtos da lista não existe | Erro PRODUTO_NAO_ENCONTRADO |
