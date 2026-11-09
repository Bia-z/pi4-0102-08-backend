# CT3: autenticarUsuario

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend |
| **Atende** | E1 |
| **Como chamar** | POST `/api/auth/login` |

### Entrada

| Campo  | Tipo | Obrigatório | Exemplo |
|:-------| :--- | :--- |:--------|
| codigo | texto | sim | 12345   |
| senha  | texto | sim | 123456  |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| token | texto | sim | eyJhbGciOiJIUzI1NiIsInR5cCI6Ik... |
| tipoToken | texto | sim | Bearer |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| usuarioId | inteiro | sim | 2 |

O `usuarioId` é o identificador do funcionário. O Frontend o guarda na sessão para enviar o `garcomId` em `criarPedido` (CT7) e para filtrar `listarPedidos` (CT22) pelo garçom logado.

### Erros

| Quando                            | O que volta |
|:----------------------------------| :--- |
| Codigo ou senha incorretos        | Erro CREDENCIAIS_INVALIDAS |
| O usuário cadastrado está inativo | Erro USUARIO_INATIVO |
