# Migrações do banco

O projeto não usa Flyway nem Liquibase. O banco é criado pelos scripts de `docker/db/init/`, que **só rodam com o volume vazio**. Quem já tem o banco criado precisa aplicar as mudanças novas com os scripts desta pasta.

Esta pasta fica **fora** de `docker/db/init/` de propósito: o Postgres não executa nada daqui sozinho.

## Como aplicar

Com o banco ligado (`docker compose up -d db`), na pasta do backend:

```powershell
# uma migração
Get-Content docker/db/migracoes/001-usuario-cpf-telefone.sql | docker compose exec -T db psql -U restmanager -d restmanager

# todas, em ordem (as migrações são idempotentes, rodar de novo não causa erro)
Get-ChildItem docker/db/migracoes/*.sql | Sort-Object Name | ForEach-Object { Get-Content $_ | docker compose exec -T db psql -U restmanager -d restmanager }
```

Mensagens `NOTICE: ... already exists, skipping` são normais: indicam que aquela parte já estava aplicada.

Linux/Mac: `cat docker/db/migracoes/001-usuario-cpf-telefone.sql | docker compose exec -T db psql -U restmanager -d restmanager`

Sem dados importantes no banco local? Também vale recriar tudo: `docker compose down -v` e `docker compose up -d db`.

## Como criar uma migração nova

1. Altere o `docker/db/init/01-schema.sql` (para instalações novas).
2. Crie aqui o próximo arquivo numerado: `002-descricao-curta.sql`, `003-...`. Nunca edite uma migração que já foi enviada.
3. Escreva o script de forma **idempotente** (`IF NOT EXISTS`, checagem em `pg_constraint` etc.) e dentro de `BEGIN; ... COMMIT;`.
4. Teste duas vezes no seu banco: a segunda execução não pode dar erro.
5. Registre na tabela abaixo e avise o grupo.

## Histórico

| Nº | Arquivo | O que muda | Data | Autor |
| :--- | :--- | :--- | :--- | :--- |
| 001 | `001-usuario-cpf-telefone.sql` | `tb_usuario`: e-mail opcional, colunas `cpf` (único) e `telefone` | 04/11/2026 | Beatriz (cadastro) · Lucas (script) |
