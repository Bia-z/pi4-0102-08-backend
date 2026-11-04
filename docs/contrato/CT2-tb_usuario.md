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
| codigo | texto (único) | sim | garcom1 |
| nome | texto | sim | Carlos Mendes |
| email | texto | sim | carlos@restmanager.com |
| senha | texto | sim | $2a$10$e839... (hash BCrypt) |
| perfil | um de: ADMIN, GARCOM, COZINHA, CAIXA | sim | GARCOM |
| ativo | booleano | sim | true |

### Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 1 |
| codigo | texto (único) | sim | garcom1 |
| nome | texto | sim | Carlos Mendes |
| email | texto | sim | carlos@restmanager.com |
| senha | texto | sim | $2a$10$e839... (hash BCrypt) |
| perfil | um de: ADMIN, GARCOM, COZINHA, CAIXA | sim | GARCOM |
| ativo | booleano | sim | true |

### Erros

| Quando | O que volta |
| :--- | :--- |
| O e-mail enviado viola a restrição UNIQUE da tabela | O banco recusa a gravação (Unique Constraint Violation) |
| O código enviado já pertence a outro usuário | O banco recusa a gravação (Unique Constraint Violation) |
| O perfil não é um dos valores permitidos | O banco recusa a gravação (Check Constraint Violation) |

> O perfil é gravado em maiúsculas, igual ao enum `Perfil` (`@Enumerated(EnumType.STRING)`). A API devolve em minúsculas. O `codigo` é o identificador usado no login (CT3).
