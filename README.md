# RestManager

O RestManager é uma plataforma web de gestão de restaurante que substitui as comandas de papel, integrando em tempo real o lançamento de pedidos pelo garçom, o acompanhamento da cozinha e o fechamento do caixa — com perfis de acesso distintos para garçom, cozinha, caixa e administrador.

## Integrantes

- Beatriz Caroline Moreno Tavares — @Bia-z
- João Victor Teles Carneiro — @jvictortcarneiro-max
- Heitor Barros Silvério — @Heitorbs97
- Lucas Ormenese Altieri — @la-eds’s
- Nathalia Lopes Da Silva Lima — @NathyLopes
- Vitória Aparecida Brigo Mattos — @Vitoria-Mattos

## Tecnologias

- Backend: Java 17, Spring Boot 3.5 (Web, Data JPA, Security, Validation, WebSocket/STOMP), JWT e BCrypt
- Banco: PostgreSQL 16
- Frontend: HTML, CSS (Bootstrap 5) e JavaScript ES6 servidos pelo nginx, no repositório [pi4-0102-08-frontend](https://github.com/Bia-z/pi4-0102-08-frontend)
- Execução: Docker Compose; CI no GitHub Actions com `mvn verify`

## Como rodar

Os dois repositórios precisam estar na mesma pasta:

```
git clone https://github.com/Bia-z/pi4-0102-08-backend.git
git clone https://github.com/Bia-z/pi4-0102-08-frontend.git
cd pi4-0102-08-backend
cp .env.example .env
docker compose up -d --build
```

No PowerShell, troque `cp` por `Copy-Item`. A aplicação fica em http://localhost:3000 e a API em http://localhost:8080.

Usuários de teste, todos com a senha `restmanager123`: `admin`, `garcom1`, `garcom2`, `cozinha` e `caixa`.

Para atualizar um banco criado antes de alguma mudança no esquema, veja [docker/db/migracoes](docker/db/migracoes/README.md).

## Testes

```
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-17 mvn verify
```

Com Java 17 e Maven instalados, basta `mvn verify`.

## Documentação

| Pasta | Conteúdo |
| :--- | :--- |
| `docs/contrato` | Contratos da API, das tabelas e das mensagens em tempo real (CT1 a CT24) |
| `docs/banco` | Modelo do banco e convenções |
| `docs/adr` | Decisões de arquitetura |
| `docs/testes` | Plano de testes e roteiro de demonstração |
| `docs/escopo.pdf` | Escopo do projeto |

## Segurança da API

O login `POST /api/auth/login` é público. As demais operações exigem um JWT
válido no cabeçalho `Authorization: Bearer <token>`. As permissões por método,
rota e perfil ficam declaradas em um único lugar, `PerfilAccessRules`; rotas sem
regra são recusadas.

Sem token, a API responde `401` com `{"erro":"TOKEN_AUSENTE"}`; token inválido
responde `401` com `{"erro":"TOKEN_INVALIDO"}`; perfil sem permissão responde
`403` com `{"erro":"ACESSO_NEGADO"}`.
