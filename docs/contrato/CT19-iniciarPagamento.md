# CT19: iniciarPagamento

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend. Ao concluir, publica CT21 `statusMesaAtualizado` |
| **Atende** | E3, E7 |
| **Como chamar** | POST `/api/mesas/{mesaId}/pagamento/iniciar` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| status | um de: disponivel, ocupada, reservada, em_pagamento | sim | em_pagamento |
| iniciadoPor | inteiro | sim | 3 |
| iniciadoEm | data e hora | sim | 2026-10-13T14:40:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A mesa informada não existe | Erro MESA_NAO_ENCONTRADA |
| A mesa não está ocupada, ou seja, não há pedido em aberto para pagar | Erro MESA_SEM_PEDIDO_ABERTO |
| A mesa já está em pagamento aberto por outro operador de caixa | Erro MESA_JA_EM_PAGAMENTO |
| O usuário que chama a API não é do Caixa nem Administrador | Erro ACESSO_NEGADO |
