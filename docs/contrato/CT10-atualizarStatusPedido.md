# CT10: atualizarStatusPedido

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E5 |
| **Como chamar** | PATCH `/api/pedidos/{pedidoId}/status` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| novoStatus | um de: aguardando, em_preparo, pronto, pago, cancelado | sim | em_preparo |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| status | um de: aguardando, em_preparo, pronto, pago, cancelado | sim | em_preparo |
| atualizadoEm | data e hora | sim | 2026-10-13T14:35:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O pedido informado não existe | Erro PEDIDO_NAO_ENCONTRADO |
| O novo status viola a sequência permitida da máquina de estados | Erro TRANSICAO_STATUS_INVALIDA |
