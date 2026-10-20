# CT21: statusMesaAtualizado

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | WebSocket |
| **Quem chama** | Backend, sempre que o status de uma mesa é alterado |
| **Quem responde** | Frontend, nos painéis abertos do Garçom e do Caixa. Não há resposta. |
| **Atende** | E3, E6, E7, E9 |
| **Como chamar** | Canal `/topic/mesas`, mensagem do tipo `statusMesaAtualizado` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| mesaId | inteiro | sim | 5 |
| numero | inteiro | sim | 10 |
| status | um de: disponivel, ocupada, reservada, em_pagamento | sim | em_pagamento |
| atualizadoEm | data e hora | sim | 2026-10-13T14:40:00 |

### Saída

não tem

### Erros

| Quando | O que volta |
| :--- | :--- |
| O dispositivo do garçom ou do caixa estava desconectado na hora do envio | Nada. Ao reconectar, o frontend busca o estado das mesas via HTTP (CT12) |
