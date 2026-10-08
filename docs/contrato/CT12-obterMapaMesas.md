# CT12: obterMapaMesas

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E6 |
| **Como chamar** | GET `/api/mesas/mapa` |

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
| status | um de: disponivel, ocupada, reservada | sim | ocupada |
| valorConsumido | decimal | sim | 143.00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| Falha temporária no banco de dados | Erro FALHA_INTERNA_SERVIDO |
