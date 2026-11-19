1. VISÃO GERAL

2. OBJETIVOS

3. REQUISITOS

4. ARQUITETURA

5. BANCO DE DADOS

6. CONTRATOS E DECISÕES

### 6.1 Contratos

| ID | Nome | Fronteira | Onde está |
| :--- | :--- | :--- | :--- |
| CT1 | cadastrarUsuario | HTTP | [backend/docs/contrato/CT1-cadastrarUsuario.md](contrato/CT1-cadastrarUsuario.md) |
| CT2 | tb_usuario | Banco | [backend/docs/contrato/CT2-tb_usuario.md](contrato/CT2-tb_usuario.md) |
| CT3 | autenticarUsuario | HTTP | [backend/docs/contrato/CT3-autenticarUsuario.md](contrato/CT3-autenticarUsuario.md) |
| CT4 | cadastrarProduto | HTTP | [backend/docs/contrato/CT4-cadastrarProduto.md](contrato/CT4-cadastrarProduto.md) |
| CT5 | cadastrarMesa | HTTP | [backend/docs/contrato/CT5-cadastrarMesa.md](contrato/CT5-cadastrarMesa.md) |
| CT6 | tb_mesa | Banco | [backend/docs/contrato/CT6-tb_mesa.md](contrato/CT6-tb_mesa.md) |
| CT7 | criarPedido | HTTP | [backend/docs/contrato/CT7-criarPedido.md](contrato/CT7-criarPedido.md) |
| CT8 | tb_pedido | Banco | [backend/docs/contrato/CT8-tb_pedido.md](contrato/CT8-tb_pedido.md) |
| CT9 | pedidoCriado | WebSocket (STOMP) | [backend/docs/contrato/CT9-pedidoCriado.md](contrato/CT9-pedidoCriado.md) |
| CT10 | atualizarStatusPedido | HTTP | [backend/docs/contrato/CT10-atualizarStatusPedido.md](contrato/CT10-atualizarStatusPedido.md) |
| CT11 | statusPedidoAtualizado | WebSocket (STOMP) | [backend/docs/contrato/CT11-statusPedidoAtualizado.md](contrato/CT11-statusPedidoAtualizado.md) |
| CT12 | obterMapaMesas | HTTP | [backend/docs/contrato/CT12-obterMapaMesas.md](contrato/CT12-obterMapaMesas.md) |
| CT13 | tb_pedido_extrato | Banco | [backend/docs/contrato/CT13-tb_pedido_extrato.md](contrato/CT13-tb_pedido_extrato.md) |
| CT14 | processarPagamento | HTTP | [backend/docs/contrato/CT14-processarPagamento.md](contrato/CT14-processarPagamento.md) |
| CT15 | tb_pagamento | Banco | [backend/docs/contrato/CT15-tb_pagamento.md](contrato/CT15-tb_pagamento.md) |
| CT16 | gerarComprovantePDF | HTTP | [backend/docs/contrato/CT16-gerarComprovantePDF.md](contrato/CT16-gerarComprovantePDF.md) |
| CT17 | tb_extrato_completo | Banco | [backend/docs/contrato/CT17-tb_extrato_completo.md](contrato/CT17-tb_extrato_completo.md) |
| CT18 | transferirMesa | HTTP | [backend/docs/contrato/CT18-transferirMesa.md](contrato/CT18-transferirMesa.md) |
| CT19 | iniciarPagamento | HTTP | [backend/docs/contrato/CT19-iniciarPagamento.md](contrato/CT19-iniciarPagamento.md) |
| CT20 | cancelarPagamento | HTTP | [backend/docs/contrato/CT20-cancelarPagamento.md](contrato/CT20-cancelarPagamento.md) |
| CT21 | statusMesaAtualizado | WebSocket (STOMP) | [backend/docs/contrato/CT21-statusMesaAtualizado.md](contrato/CT21-statusMesaAtualizado.md) |
| CT22 | listarPedidos | HTTP | [backend/docs/contrato/CT22-listarPedidos.md](contrato/CT22-listarPedidos.md) |
| CT23 | cancelarItemPedido | HTTP | [backend/docs/contrato/CT23-cancelarItemPedido.md](contrato/CT23-cancelarItemPedido.md) |
| CT24 | gerenciarMesas | HTTP | [backend/docs/contrato/CT24-gerenciarMesas.md](contrato/CT24-gerenciarMesas.md) |
| CT25 | gerenciarCategorias | HTTP | [backend/docs/contrato/CT25-gerenciarCategorias.md](contrato/CT25-gerenciarCategorias.md) |

### 6.2 Decisões

| ADR | Título | Status | Onde está |
| :--- | :--- | :--- | :--- |
| 0001 | Usar PostgreSQL como banco de dados relacional | Aceito | [backend/docs/adr/0001-usar-postgresql-como-banco-de-dados-relacional.md](adr/0001-usar-postgresql-como-banco-de-dados-relacional.md) |
| 0002 | Adotar interface web responsiva sem aplicativo mobile | Aceito | [frontend/docs/adr/0002-adotar-interface-web-responsiva-sem-aplicativo-mobile.md](https://github.com/Bia-z/pi4-0102-08-frontend/blob/main/docs/adr/0002-adotar-interface-web-responsiva-sem-aplicativo-mobile.md) |
| 0003 | Utilizar WebSocket para notificar atualizações de pedidos e de status das mesas | Aceito | [backend/docs/adr/0003-utilizar-websocket-para-notificar-pedidos.md](adr/0003-utilizar-websocket-para-notificar-pedidos.md) |

7. TESTES

8. PRINCIPAIS TELAS

9. COMO RODAR

10. CONCLUSÃO

11. ANEXOS
