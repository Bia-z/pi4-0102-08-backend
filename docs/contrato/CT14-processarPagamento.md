# CT14: processarPagamento

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E7 |
| **Como chamar** | POST `/api/pedidos/{pedidoId}/pagamentos` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| pagamentos | lista de objeto PagamentoEntrada | sim | [ { "formaPagamento": "pix", "valor": 100.00 }, { "formaPagamento": "dinheiro", "valor": 43.00 } ] |

#### objeto PagamentoEntrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| formaPagamento | um de: dinheiro, credito, debito, pix, vale | sim | pix |
| valor | decimal | sim | 100.00 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| totalPago | decimal | sim | 143.00 |
| taxaServico | decimal | sim | 14.30 |
| troco | decimal | sim | 0.00 |
| statusPedido | um de: aguardando, em_preparo, pronto, pago, cancelado | sim | pago |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A soma dos pagamentos é menor que o valor total do pedido | Erro VALOR_PAGAMENTO_INSUFICIENTE |
| O pedido já consta como pago | Erro PEDIDO_JA_PAGO |
