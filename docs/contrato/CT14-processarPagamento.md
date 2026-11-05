# CT14: processarPagamento

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E7 |
| **Como chamar** | POST `/api/mesas/{mesaId}/pagamento/processar` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| pagamentos | lista de objeto PagamentoEntrada | sim | [ { "formaPagamento": "pix", "valor": 100.00 }, { "formaPagamento": "dinheiro", "valor": 43.00 } ] |
| desconto | decimal | não | 0.00 |

#### objeto PagamentoEntrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| formaPagamento | um de: dinheiro, credito, debito, pix, vale | sim | pix |
| valor | decimal | sim | 100.00 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| subtotal | decimal | sim | 130.00 |
| totalPago | decimal | sim | 143.00 |
| taxaServico | decimal | sim | 14.30 |
| desconto | decimal | sim | 1.30 |
| total | decimal | sim | 143.00 |
| troco | decimal | sim | 0.00 |
| pedidosPagos | lista de inteiros | sim | [101, 102] |
| statusMesa | um de: disponivel, ocupada, reservada, em_pagamento | sim | disponivel |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A soma dos pagamentos é menor que o total da conta | Erro VALOR_PAGAMENTO_INSUFICIENTE |
| Todos os pedidos da mesa já constam como pagos | Erro PEDIDO_JA_PAGO |
| A mesa não está em pagamento (o caixa não chamou CT19 antes) | Erro MESA_NAO_EM_PAGAMENTO |
| Não há pedido aberto na mesa | Erro MESA_SEM_PEDIDO_ABERTO |
| Um valor excedente não pode ser devolvido sem pagamento em dinheiro | Erro TROCO_REQUER_DINHEIRO |

O subtotal considera apenas itens não cancelados. A taxa de serviço é aplicada sobre
esse subtotal; o desconto opcional é subtraído do resultado e não pode ser negativo
nem maior que o subtotal acrescido da taxa. A requisição é atômica: qualquer erro
mantém os pagamentos, os pedidos e a mesa inalterados. Após o commit, é publicado
`statusMesaAtualizado` no canal `/topic/mesas`.
