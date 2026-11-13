# CT25: gerenciarCategorias (listar, cadastrar, editar e desativar)

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend (tela "Categorias" do administrador) |
| **Quem responde** | Backend |
| **Atende** | E2 |
| **Como chamar** | GET `/api/categorias` · POST `/api/categorias` · PUT `/api/categorias/{id}` · DELETE `/api/categorias/{id}` |

Só o perfil `admin` pode usar estas operações; os demais recebem `403 ACESSO_NEGADO`. O cardápio do garçom (`GET /api/cardapio`) mostra apenas as categorias ativas, na ordem definida aqui.

## Formato da categoria (saída de todas as operações)

| Campo | Tipo | Exemplo |
| :--- | :--- | :--- |
| id | inteiro | 5 |
| nome | texto | Porções |
| ordem | inteiro ≥ 0 (menor aparece primeiro no cardápio) | 5 |
| ativo | booleano | true |
| itensAtivos | inteiro: quantos itens ativos estão na categoria | 3 |

## GET `/api/categorias`: listar

Devolve todas as categorias, ativas e desativadas, por `ordem` e depois por `nome`.

## POST `/api/categorias`: cadastrar

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto até 80 caracteres | sim | Porções |
| ordem | inteiro ≥ 0 | sim | 5 |
| ativo | booleano | não (padrão `true`) | true |

Responde `201` com a categoria criada. Espaços no começo e no fim do nome são removidos.

## PUT `/api/categorias/{id}`: editar

Mesma entrada do cadastro. `ativo: true` reativa uma categoria desativada; `ativo: false` desativa (com a mesma regra do DELETE); sem `ativo`, a situação não muda.

## DELETE `/api/categorias/{id}`: desativar

Responde `204` sem corpo. A desativação é lógica (`tb_categoria.ativo = false`): a categoria some do cardápio e do formulário de itens, mas os itens antigos continuam ligados a ela.

## Erros

| Quando | O que volta |
| :--- | :--- |
| A categoria não existe | Erro CATEGORIA_NAO_ENCONTRADA |
| Já existe categoria com o mesmo nome (sem diferenciar maiúsculas) | Erro CATEGORIA_DUPLICADA |
| Desativar categoria que ainda tem itens ativos | Erro CATEGORIA_COM_ITENS |
| Nome vazio ou com mais de 80 caracteres, ou ordem negativa | `400 Bad Request` |

## Efeito no cadastro de itens (CT4)

Cadastrar, editar ou reativar um item numa categoria desativada passa a responder CATEGORIA_NAO_ENCONTRADA.
