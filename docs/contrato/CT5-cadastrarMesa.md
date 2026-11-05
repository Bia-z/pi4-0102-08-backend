# CT5: cadastrarMesa

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E2 |
| **Como chamar** | POST `/api/mesas` |

Operação permitida somente ao perfil `ADMIN`. A mesa é criada com status `disponivel`.

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| numero | inteiro | sim | 10 |
| capacidade | inteiro | sim | 4 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| capacidade | inteiro | sim | 4 |
| status | um de: disponivel, ocupada, reservada, em_pagamento | sim | disponivel |

### Erros

| Quando | O que volta |
| :--- | :--- |
| Já existe uma mesa cadastrada com esse número | Erro MESA_DUPLICADA |
| A capacidade é menor que 1 | Erro CAPACIDADE_INVALIDA |
