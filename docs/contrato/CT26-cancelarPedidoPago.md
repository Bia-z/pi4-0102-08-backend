# CT26: cancelarPedidoPago

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend (tela "Consulta de comanda" do caixa) |
| **Quem responde** | Backend |
| **Como chamar** | POST `/api/pedidos/{pedidoId}/cancelar` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro (path) | sim | 101 |
| codigoUsuario | texto | sim | admin |
| senha | texto | sim | restmanager123 |

`codigoUsuario` e `senha` são do administrador que autoriza o cancelamento.

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |
| statusPedido | sempre `cancelado` | sim | cancelado |

### Erros

| Quando | O que volta |
| :--- | :--- |
| Código ou senha ausentes, incorretos, de usuário inativo ou que não seja `admin` | Erro CREDENCIAIS_INVALIDAS |
| O pedido informado não existe | Erro PEDIDO_NAO_ENCONTRADO |
| O pedido ainda não foi pago | Erro PEDIDO_NAO_FECHADO |
| O pedido já está cancelado | Erro PEDIDO_JA_CANCELADO |

### Regras

- O perfil `caixa` ou `admin` pode chamar a rota; as demais recebem `403 ACESSO_NEGADO`. Mesmo assim, a operação só é feita se as credenciais do corpo forem de um administrador ativo.
- As credenciais são validadas antes de consultar o pedido.
- Só pedidos com status `PAGO` podem ser cancelados. O pedido passa para `CANCELADO`.
- Os registros de `tb_pagamento` são mantidos, mas o valor esperado do caixa, o resumo do turno e o relatório do dia desconsideram pagamentos de pedidos cancelados.
- O comprovante (CT16) só é emitido para pedido `PAGO`, então um pedido cancelado deixa de ter comprovante.
- O cancelamento fica registrado no log do servidor com o código do administrador.
