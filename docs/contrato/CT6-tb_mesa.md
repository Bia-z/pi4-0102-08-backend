# CT6: tb_mesa

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E2, E3, E7, E9 |
| **Como chamar** | Tabela `tb_mesa`, chave `id` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| capacidade | inteiro | sim | 4 |
| status | um de: DISPONIVEL, OCUPADA, RESERVADA, EM_PAGAMENTO | sim | OCUPADA |
| ativo | booleano | sim | true |
| pagamento_iniciado_por | inteiro (id do usuário) | só quando EM_PAGAMENTO | 5 |
| pagamento_iniciado_em | data e hora | só quando EM_PAGAMENTO | 2026-10-13T14:40:00 |
| version | inteiro (controle de concorrência, `@Version`) | sim | 0 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| capacidade | inteiro | sim | 4 |
| status | um de: DISPONIVEL, OCUPADA, RESERVADA, EM_PAGAMENTO | sim | OCUPADA |
| ativo | booleano | sim | true |
| pagamento_iniciado_por | inteiro (id do usuário) | só quando EM_PAGAMENTO | 5 |
| pagamento_iniciado_em | data e hora | só quando EM_PAGAMENTO | 2026-10-13T14:40:00 |
| version | inteiro (controle de concorrência, `@Version`) | sim | 0 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O número da mesa enviado viola a restrição UNIQUE | O banco recusa a gravação |
| Mesa EM_PAGAMENTO sem operador/horário, ou com esses campos fora do status EM_PAGAMENTO | O banco recusa a gravação (Check Constraint Violation) |
| Duas transações alteram a mesma mesa ao mesmo tempo | O JPA recusa a segunda (`OptimisticLockException`), conforme a regra 4 do ADR 0003 |
