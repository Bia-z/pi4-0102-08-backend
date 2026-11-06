# CT10: atualizarStatusPedido

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E5 |
| **Como chamar** | PATCH `/api/pedidos/{pedidoId}/itens/{itemId}/status` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro (path) | sim | 101 |
| itemId | inteiro (path) | sim | 201 |
| novoStatus | um de: aguardando, em_preparo, pronto | sim | em_preparo |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| itemId | inteiro | sim | 201 |
| status | um de: aguardando, em_preparo, pronto | sim | em_preparo |
| atualizadoEm | data e hora | sim | 2026-10-13T14:35:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O pedido informado não existe | Erro PEDIDO_NAO_ENCONTRADO |
| O item não pertence ao pedido informado | Erro ITEM_PEDIDO_NAO_ENCONTRADO |
| O novo status não segue `aguardando` → `em_preparo` → `pronto` | Erro TRANSICAO_STATUS_INVALIDA |

O perfil `cozinha` ou `admin` pode executar a operação. O status é salvo
transacionalmente e, após o commit, é publicada uma mensagem `statusPedidoAtualizado`
no canal `/topic/pedidos`.
