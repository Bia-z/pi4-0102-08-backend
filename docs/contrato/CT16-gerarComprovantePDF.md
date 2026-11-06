# CT16: gerarComprovantePDF

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E8 |
| **Como chamar** | GET `/api/pedidos/{pedidoId}/comprovante-pdf` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| pedidoId | inteiro | sim | 101 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| documentoPdf | texto | sim | %PDF-1.4 ... (fluxo de bytes do PDF / application/pdf) |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O pedido informado não foi encontrado | Erro PEDIDO_NAO_ENCONTRADO (404) |
| O pedido ainda não foi pago ou cancelado | Erro CONTA_NAO_PAGA (409) |
| Ocorreu uma falha na renderização do PDF | Erro ERRO_GERACAO_PDF (500) |

O comprovante reúne os pedidos da mesa pagos na mesma transação, informa o horário
do pagamento, os itens e quantidades com preços, subtotal, taxa de serviço, total
efetivamente pago e formas de pagamento. Itens cancelados não são incluídos.
