# CT2: tb_usuario

| Campo | Valor |
| :--- | :--- |
| **Fronteira** | Banco |
| **Quem chama** | Backend |
| **Quem responde** | Banco |
| **Atende** | E1 |
| **Como chamar** | Tabela `tb_usuario`, chave `id` |

### Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | Carlos Mendes |
| email | texto | sim | carlos@restmanager.com |
| senha | texto | sim | $2a$10$e839... (hash BCrypt) |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| ativo | booleano | sim | true |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 1 |
| nome | texto | sim | Carlos Mendes |
| email | texto | sim | carlos@restmanager.com |
| senha | texto | sim | $2a$10$e839... (hash BCrypt) |
| perfil | um de: admin, garcom, cozinha, caixa | sim | garcom |
| ativo | booleano | sim | true |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O e-mail enviado viola a restrição UNIQUE da tabela | O banco recusa a gravação (Unique Constraint Violation) |
