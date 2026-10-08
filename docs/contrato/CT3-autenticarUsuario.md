# CT3: autenticarUsuario

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E1 |
| **Como chamar** | POST `/api/auth/login` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| email | texto | sim | garcom@restmanager.com |
| senha | texto | sim | 123456 |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| token | texto | sim | eyJhbGciOiJIUzI1NiIsInR5cCI6Ik... |
| tipoToken | texto | sim | Bearer |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |

### Erros

| Quando | O que volta |
| :--- | :--- |
| E-mail ou senha incorretos | Erro CREDENCIAIS_INVALIDAS |
| O usuário cadastrado está inativo | Erro USUARIO_INATIVO |
