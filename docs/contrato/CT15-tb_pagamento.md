# CT15: tb_pagamento

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E7 |
| **Como chamar** | Tabela `tb_pagamento`, chave `id` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedido_id | inteiro | sim | 101 |
| forma_pagamento | um de: dinheiro, credito, debito, pix, vale | sim | pix |
| valor | decimal | sim | 100.00 |
| data_pagamento | data e hora | sim | 2026-10-13T15:10:00 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 801 |
| pedido_id | inteiro | sim | 101 |
| forma_pagamento | um de: dinheiro, credito, debito, pix, vale | sim | pix |
| valor | decimal | sim | 100.00 |
| data_pagamento | data e hora | sim | 2026-10-13T15:10:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O `pedido_id` não existe na tabela `tb_pedido` | O banco recusa a inserção (Foreign Key Violation) |
