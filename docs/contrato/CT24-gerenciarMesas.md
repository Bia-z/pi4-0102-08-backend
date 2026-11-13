# CT24: gerenciarMesas (listar, editar e excluir)

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend (tela "Mesas" do administrador) |
| **Quem responde** | Backend |
| **Atende** | E2 |
| **Como chamar** | GET `/api/mesas` · PUT `/api/mesas/{id}` · DELETE `/api/mesas/{id}` |

Complementa o CT5 (cadastrarMesa). Só o perfil `admin` pode usar estas operações; os demais recebem `403 ACESSO_NEGADO`.

## GET `/api/mesas`: listar

Devolve as mesas ativas em ordem de número. Mesas excluídas não aparecem.

| Campo | Tipo | Exemplo |
| :--- | :--- | :--- |
| id | inteiro | 3 |
| numero | inteiro | 5 |
| capacidade | inteiro | 4 |
| status | um de: disponivel, ocupada, reservada, em_pagamento | disponivel |

## PUT `/api/mesas/{id}`: editar

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro (path) | sim | 3 |
| numero | inteiro positivo | sim | 15 |
| capacidade | inteiro, mínimo 1 | sim | 6 |

Saída: a mesa atualizada, no mesmo formato da listagem. Editar não muda o status da mesa.

### Erros

| Quando | O que volta |
| :--- | :--- |
| A mesa não existe ou foi excluída | Erro MESA_NAO_ENCONTRADA |
| Outro registro já usa o número informado | Erro MESA_DUPLICADA |
| Capacidade menor que 1 | Erro CAPACIDADE_INVALIDA |

## DELETE `/api/mesas/{id}`: excluir

Responde `204` sem corpo. A exclusão é lógica (`tb_mesa.ativo = false`): os pedidos antigos continuam ligados à mesa, mas ela some do mapa, do cadastro de pedidos e da transferência.

| Quando | O que volta |
| :--- | :--- |
| A mesa não existe ou já foi excluída | Erro MESA_NAO_ENCONTRADA |
| A mesa não está `disponivel` (ocupada, reservada ou em pagamento) | Erro MESA_EM_USO |

## Cadastrar de novo uma mesa excluída

Se o administrador cadastrar (CT5) um número que pertence a uma mesa excluída, ela é reativada com a nova capacidade e status `disponivel`, em vez de recusar com MESA_DUPLICADA.
