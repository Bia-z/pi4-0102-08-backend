# CT13: tb_pedido_extrato

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E6 |
| **Como chamar** | Consulta com `JOIN` entre `tb_mesa`, `tb_pedido`, `tb_item_pedido`, `tb_produto` e `tb_pagamento` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesa_id | inteiro | sim | 5 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesa_id | inteiro | sim | 5 |
| numero_mesa | inteiro | sim | 10 |
| mesa_status | um de: disponivel, ocupada, reservada | sim | ocupada |
| pedido_id | inteiro | sim | 101 |
| data_abertura | data e hora | sim | 2026-09-14T19:42:00 |
| itens_consumidos | lista de objeto ItemExtrato | sim | [ { "produto_id": 10, "nome_produto": "Picanha Grelhada", "quantidade": 2, "preco_unitario": 89.00, "subtotal": 178.00 } ] |
| pagamentos_realizados | lista de objeto PagamentoExtrato | não | [ { "forma_pagamento": "pix", "valor_pago": 100.00, "data_pagamento": "2026-09-14T20:10:00" } ] |
| valor_subtotal | decimal | sim | 310.00 |
| taxa_servico | decimal | sim | 31.00 |
| valor_total_consumido | decimal | sim | 341.00 |

#### objeto ItemExtrato

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| produto_id | inteiro | sim | 10 |
| nome_produto | texto | sim | Picanha Grelhada |
| quantidade | inteiro | sim | 2 |
| preco_unitario | decimal | sim | 89.00 |
| subtotal | decimal | sim | 178.00 |

#### objeto PagamentoExtrato

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| forma_pagamento | um de: dinheiro, credito, debito, pix, vale | sim | pix |
| valor_pago | decimal | sim | 100.00 |
| data_pagamento | data e hora | sim | 2026-09-14T20:10:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A consulta falha devido a bloqueio de tabela | O banco retorna timeout de consulta |
| Não existem registros para a mesa informada | O banco retorna um resultado vazio |
