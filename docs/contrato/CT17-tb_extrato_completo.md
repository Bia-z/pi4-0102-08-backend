# CT17: tb_extrato_completo

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E8 |
| **Como chamar** | Consulta com `JOIN` entre `tb_pedido`, `tb_item_pedido`, `tb_produto` e `tb_pagamento` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedido_id | inteiro | sim | 101 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedido_id | inteiro | sim | 101 |
| numero_mesa | inteiro | sim | 10 |
| data_abertura | data e hora | sim | 2026-09-14T19:42:00 |
| itens | lista de objeto ItemRelatorio | sim | [ { "nome_produto": "Bruschetta Italiana", "quantidade": 2, "preco_unitario": 28.00, "total_item": 56.00 } ] |
| pagamentos | lista de objeto PagamentoRelatorio | sim | [ { "forma_pagamento": "pix", "valor_pago": 56.00 } ] |
| taxa_servico | decimal | sim | 5.60 |
| valor_total | decimal | sim | 61.60 |

#### objeto ItemRelatorio

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome_produto | texto | sim | Bruschetta Italiana |
| quantidade | inteiro | sim | 2 |
| preco_unitario | decimal | sim | 28.00 |
| total_item | decimal | sim | 56.00 |

#### objeto PagamentoRelatorio

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| forma_pagamento | um de: dinheiro, credito, debito, pix, vale | sim | pix |
| valor_pago | decimal | sim | 56.00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| Inexistência de dados para o pedido consultado | Retorna um conjunto de dados vazio |
