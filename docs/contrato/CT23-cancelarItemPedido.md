# CT23: cancelarItemPedido

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend (tela "Acompanhar pedidos" do garçom) |
| **Quem responde** | Backend |
| **Atende** | E3 e E5 |
| **Como chamar** | POST `/api/pedidos/{pedidoId}/itens/{itemId}/cancelar` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro (path) | sim | 101 |
| itemId | inteiro (path) | sim | 201 |

Não há corpo na requisição.

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| itemId | inteiro | sim | 201 |
| status | sempre `cancelado` | sim | cancelado |
| atualizadoEm | data e hora | sim | 2026-11-13T14:35:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O pedido informado não existe | Erro PEDIDO_NAO_ENCONTRADO |
| O item não pertence ao pedido informado | Erro ITEM_PEDIDO_NAO_ENCONTRADO |
| O pedido já foi pago ou cancelado | Erro PEDIDO_NAO_EM_ABERTO |
| A mesa do pedido está em pagamento | Erro MESA_EM_PAGAMENTO |
| O item não está mais `aguardando` (a cozinha já começou ou terminou) | Erro CANCELAMENTO_NAO_PERMITIDO |

### Regras

- O perfil `garcom` ou `admin` pode executar a operação. A cozinha e o caixa recebem `403 ACESSO_NEGADO`.
- Só é possível cancelar item com status `aguardando`. Depois que a cozinha inicia o preparo, o cancelamento é recusado.
- O `valor_total` do pedido é recalculado sem os itens cancelados, com a taxa de serviço vigente. O mapa de mesas, o pagamento e o comprovante já desconsideram itens cancelados.
- Se todos os itens do pedido forem cancelados, o pedido passa para `CANCELADO`. Se a mesa não tiver outro pedido em aberto, ela volta para `DISPONIVEL`.
- Após o commit é publicada a mensagem `statusPedidoAtualizado` (CT11) com `status: "cancelado"` em `/topic/pedidos`. Quando a mesa é liberada, também sai `statusMesaAtualizado` (CT21) com `status: "disponivel"` em `/topic/mesas`.
