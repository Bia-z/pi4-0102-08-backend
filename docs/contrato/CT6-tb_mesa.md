# CT6: tb_mesa

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E2, E3, E7, E9 |
| **Como chamar** | Tabela `tb_mesa`, chave `id` |

*Entrada e saída: os campos gravados são os mesmos campos lidos.*

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| capacidade | inteiro | sim | 4 |
| status | um de: disponivel, ocupada, reservada | sim | ocupada |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O número da mesa enviado viola a restrição UNIQUE | O banco recusa a gravação |
