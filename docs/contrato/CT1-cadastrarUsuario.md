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
| email | email | não | carlos@restmanager.com |
| cpf | texto | sim | 123.456.789-00 |
| telefone | texto | sim | 11999999999 |
| codigo | texto | não | FUNC001 |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| senha | texto | sim | senhaSegura123 |

Se `codigo` não for enviado, o backend gera um código de funcionário único.
O CPF pode ser enviado com pontuação ou apenas dígitos; o backend valida os
dígitos verificadores e armazena somente os dígitos. O telefone deve ser um
número brasileiro válido com DDD (10 ou 11 dígitos, com `+55` opcional).

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 1 |
| codigo | texto | sim | FUNC001 |
| nome | texto | sim | Carlos Mendes |
| email | email ou nulo | não | carlos@restmanager.com |
| cpf | texto | sim | 123.456.789-00 |
| telefone | texto | sim | 11999999999 |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| ativo | booleano | sim | true |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O código informado já está cadastrado no sistema | Erro CODIGO_DUPLICADO |
| O CPF tem formato/dígitos verificadores inválidos | Erro CPF_INVALIDO |
| O CPF já está cadastrado no sistema | Erro CPF_DUPLICADO |
| O telefone não tem formato brasileiro válido | Erro TELEFONE_INVALIDO |
| O email informado é inválido | Erro EMAIL_INVALIDO |
| O email informado já está cadastrado | Erro EMAIL_DUPLICADO |
| O perfil informado não é válido | Erro PERFIL_INVALIDO |
| O usuário que chama a API não é Administrador | Erro ACESSO_NEGADO |

A senha é armazenada como hash BCrypt e não é devolvida na resposta.
