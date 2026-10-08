# CT4: cadastrarProduto

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E2 |
| **Como chamar** | POST `/api/produtos` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | Bruschetta Italiana |
| descricao | texto | não | Pão com tomate, manjericão e azeite extra virgem |
| preco | decimal | sim | 28.00 |
| categoriaId | inteiro | sim | 1 |
| imagemUrl | texto | não | bruschetta.jpg |
| ingredientes | lista de inteiro | não | [1, 2, 5] |
| ativo | booleano | sim | true |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 10 |
| nome | texto | sim | Bruschetta Italiana |
| descricao | texto | não | Pão com tomate, manjericão e azeite extra virgem |
| preco | decimal | sim | 28.00 |
| categoriaId | inteiro | sim | 1 |
| imagemUrl | texto | não | bruschetta.jpg |
| ativo | booleano | sim | true |
| criadoEm | data e hora | sim | 2026-10-13T14:00:00 |

### Erros

| Quando | O que volta |
| :--- | :--- |
| A categoria informada não existe | Erro CATEGORIA_NAO_ENCONTRADA |
| O valor do preço é menor ou igual a zero | Erro PRECO_INVALIDO |
| Já existe um prato cadastrado com o mesmo nome | Erro PRODUTO_DUPLICADO |
