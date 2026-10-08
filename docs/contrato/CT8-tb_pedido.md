# CT8: tb_pedido

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E3, E4, E5, E9 |
| **Como chamar** | Tabelas `tb_pedido` e `tb_item_pedido`, chave `id` |

*Entrada e saída: os campos gravados são os mesmos campos lidos.*

#### Tabela tb_pedido

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 101 |
| mesa_id | inteiro | sim | 5 |
| garcom_id | inteiro | sim | 2 |
| status | um de: aguardando, em_preparo, pronto, pago, cancelado | sim | aguardando |
| valor_total | decimal | sim | 56.00 |
| data_criacao | data e hora | sim | 2026-10-13T14:32:00 |

#### Tabela tb_item_pedido

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 501 |
| pedido_id | inteiro | sim | 101 |
| produto_id | inteiro | sim | 10 |
| quantidade | inteiro | sim | 2 |
| preco_unitario | decimal | sim | 28.00 |
| observacao | texto | não | Sem cebola |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O `mesa_id` ou `garcom_id` não existem nas tabelas pai | O banco recusa a gravação (Foreign Key Violation) |
