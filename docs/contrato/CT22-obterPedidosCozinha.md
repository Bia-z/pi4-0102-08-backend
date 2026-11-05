# CT22: obterPedidosCozinha

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend, ao abrir ou reconectar o painel da Cozinha |
| **Quem responde** | Backend |
| **Atende** | E4 |
| **Como chamar** | GET `/api/cozinha/pedidos` |

### Entrada

não tem

### Saída

Lista de pedidos com itens aguardando ou em preparo, em ordem de chegada. Cada pedido tem o mesmo formato da mensagem `pedidoCriado` (CT9).

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidos | lista de objeto PedidoCriado (CT9) | sim | [ { "pedidoId": 101, "numeroMesa": 10, "horario": "2026-10-13T14:32:00", "itens": [ { "itemId": 501, "nome": "Bruschetta Italiana", "quantidade": 2, "observacao": "Sem cebola", "status": "aguardando" } ] } ] |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O usuário não é da Cozinha nem Administrador | Erro ACESSO_NEGADO |
| Não há pedidos em aberto | Lista vazia |
