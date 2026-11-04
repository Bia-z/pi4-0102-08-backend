# CT1: cadastrarUsuario

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E1 |
| **Como chamar** | POST `/api/usuarios` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | Carlos Mendes |
| cpf | texto | sim | 123.456.789-00 |
| telefone | texto | sim | 11999999999 |
| codigo | texto | não | FUNC001 |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| senha | texto | sim | senhaSegura123 |

Se `codigo` não for enviado, o backend gera um código de funcionário único.

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 1 |
| codigo | texto | sim | FUNC001 |
| nome | texto | sim | Carlos Mendes |
| cpf | texto | sim | 123.456.789-00 |
| telefone | texto | sim | 11999999999 |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| ativo | booleano | sim | true |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O código informado já está cadastrado no sistema | Erro CODIGO_DUPLICADO |
| O perfil informado não é válido | Erro PERFIL_INVALIDO |
| O usuário que chama a API não é Administrador | Erro ACESSO_NEGADO |

A senha é armazenada como hash BCrypt e não é devolvida na resposta.
