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
| A mesa de destino está ocupada ou reservada | Erro MESA_DESTINO_OCUPADA |
| O pedido a ser transferido não se encontra em aberto | Erro PEDIDO_NAO_EM_ABERTO |
| A mesa de origem ou de destino está em pagamento | Erro MESA_EM_PAGAMENTO |
| A mesa de destino não existe ou está inativa | Erro MESA_NAO_ENCONTRADA |

A transferência altera somente `mesa_id` do pedido, preservando o pedido, garçom,
data, itens e observações. A mesa de destino passa a ocupada. A mesa de origem só
passa a disponível quando não restam pedidos em aberto nela. O registro inclui pedido,
mesas, usuário autenticado e data/hora. Após a confirmação da transação, o backend
publica `statusMesaAtualizado` para a origem e o destino em `/topic/mesas`.
