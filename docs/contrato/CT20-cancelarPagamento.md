# CT20: cancelarPagamento

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend. Ao concluir, publica CT21 `statusMesaAtualizado` |
| **Atende** | E3, E7 |
| **Como chamar** | POST `/api/mesas/{mesaId}/pagamento/cancelar` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| status | um de: disponivel, ocupada, reservada, em_pagamento | sim | ocupada |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A mesa informada não existe | Erro MESA_NAO_ENCONTRADA |
| A mesa não está em pagamento | Erro MESA_NAO_EM_PAGAMENTO |
| O usuário que chama não é o operador que iniciou o pagamento nem Administrador | Erro ACESSO_NEGADO |
