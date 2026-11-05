# CT12: obterMapaMesas

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E6 |
| **Como chamar** | GET `/api/mesas/mapa` |
| **Acesso** | ADMIN, GARCOM ou CAIXA |

### Entrada

não tem

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesas | lista de objeto MesaResumo | sim | [ { "mesaId": 5, "numero": 10, "status": "ocupada", "valorConsumido": 143.00 } ] |

#### objeto MesaResumo

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| status | um de: disponivel, ocupada, reservada, em_pagamento | sim | ocupada |
| valorConsumido | decimal | sim | 143.00 |

`valorConsumido` soma o subtotal dos itens não cancelados em pedidos não pagos ou cancelados,
acrescido da taxa de serviço percentual cadastrada em `tb_configuracao`. Mesas inativas não
são incluídas.

### Erros

| Quando | O que volta |
| :--- | :--- |
| Falha temporária no banco de dados | Erro FALHA_INTERNA_SERVIDO |
