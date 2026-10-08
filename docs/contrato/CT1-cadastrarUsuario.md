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
| email | texto | sim | carlos@restmanager.com |
| senha | texto | sim | senhaSegura123 |
| perfil | um de: admin, garçom, cozinha, caixa | sim | garçom |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 1 |
| nome | texto | sim | Carlos Mendes |
| email | texto | sim | carlos@restmanager.com |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| ativo | booleano | sim | true |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O e-mail já está cadastrado no sistema | Erro EMAIL_DUPLICADO |
| O perfil informado não é válido | Erro PERFIL_INVALIDO |
| O usuário que chama a API não é Administrador | Erro ACESSO_NEGADO |
