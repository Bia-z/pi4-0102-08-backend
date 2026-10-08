# CT18: transferirMesa

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E9 |
| **Como chamar** | POST `/api/pedidos/{pedidoId}/transferir-mesa` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| mesaDestinoId | inteiro | sim | 8 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| mesaOrigemId | inteiro | sim | 5 |
| mesaDestinoId | inteiro | sim | 8 |
| transferidoEm | data e hora | sim | 2026-10-13T15:20:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A mesa de destino já se encontra ocupada | Erro MESA_DESTINO_OCUPADA |
| O pedido a ser transferido não se encontra em aberto | Erro PEDIDO_NAO_A_ABERTO |
