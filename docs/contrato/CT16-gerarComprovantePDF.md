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
| O pedido informado não foi encontrado | Erro PEDIDO_NAO_ENCONTRADO |
| Ocorreu uma falha na renderização do PDF | Erro ERRO_GERACAO_PDF |
