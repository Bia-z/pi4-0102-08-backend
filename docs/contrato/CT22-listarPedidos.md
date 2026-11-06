# CT22: listarPedidos

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Como chamar** | GET `/api/pedidos` |
| **Quem pode chamar** | Administrador, garçom ou cozinha |

### Filtros opcionais

| Parâmetro | Tipo | Descrição |
| :--- | :--- | :--- |
| status | `StatusPedido` | Filtra pelo status do pedido |
| statusItem | `StatusItem` | Retorna pedidos que tenham item nesse status |
| garcomId | inteiro | Filtra pelo garçom responsável |
| mesaId | inteiro | Filtra pela mesa |

São retornados somente pedidos em aberto (diferentes de `pago` e `cancelado`),
em ordem crescente de criação. Pedidos com o mesmo horário são ordenados pelo
identificador. Para a cozinha, são retornados somente pedidos com itens
`aguardando` ou `em_preparo`, e a lista de itens contém apenas esses itens.

### Saída

Uma lista (vazia quando não há correspondências) de pedidos com `id`, `mesaId`,
`numeroMesa`, `garcomId`, `status`, `valorTotal`, `dataCriacao` e `itens`.
Cada item contém `id`, `produtoId`, `produtoNome`, `quantidade`, `observacao`
e `status`. Os status são apresentados em letras minúsculas.

### Erros

Parâmetros de filtro incompatíveis com seus tipos retornam HTTP 400. A operação
não retorna erro quando nenhum pedido corresponde aos filtros: retorna `[]`.
