# CT4: cadastrarProduto

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E2 |
| **Como chamar** | POST `/api/produtos` |

O cadastro é permitido somente ao perfil `ADMIN`.

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | Bruschetta Italiana |
| descricao | texto | não | Pão com tomate, manjericão e azeite extra virgem |
| preco | decimal | sim | 28.00 |
| categoriaId | inteiro | sim | 1 |
| imagemUrl | texto | não | bruschetta.jpg |
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

As categorias do cardápio são: Entradas, Pratos principais, Sobremesas,
**Adicionais** (itens extras que acompanham um prato, como queijo ou bacon extra)
e Bebidas. As categorias disponíveis para o formulário vêm de `GET /api/cardapio`.

## Gerenciar itens

Todas as operações de gerenciamento abaixo exigem perfil `ADMIN`. A remoção é
lógica: o registro permanece para preservar pedidos e históricos associados.

| Recurso | Listar para gerenciamento | Editar | Remover |
| :--- | :--- | :--- | :--- |
| Itens/produtos | `GET /api/produtos` | `PUT /api/produtos/{id}` | `DELETE /api/produtos/{id}` |

O `PUT` de produto recebe os mesmos campos do cadastro.

## listarCardapio

| Campo | Valor |
| :--- | :--- |
| **Quem chama** | Frontend e equipe autenticada |
| **Como chamar** | GET `/api/cardapio` |

A resposta é uma lista de categorias ativas, ordenada pela ordem de exibição e nome.
Cada categoria contém somente os produtos ativos, em ordem alfabética:

```json
[
  {
    "categoriaId": 1,
    "nome": "Entradas",
    "produtos": [
      {
        "id": 10,
        "nome": "Bruschetta Italiana",
        "descricao": "Pão com tomate e manjericão",
        "preco": 28.00,
        "categoriaId": 1,
        "imagemUrl": "bruschetta.jpg",
        "ativo": true,
        "criadoEm": "2026-10-13T14:00:00"
      }
    ]
  }
]
```
