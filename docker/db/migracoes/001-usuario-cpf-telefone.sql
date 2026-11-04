-- =====================================================================
-- Migração 001 - CPF e telefone no cadastro de funcionário
-- Data: 04/11/2026 · Origem: cadastro de funcionários (CT1, E1)
--
-- O cadastro passa a exigir CPF e telefone pela API; o e-mail fica opcional.
-- As colunas são nullable para preservar os usuários já cadastrados.
-- O 01-schema.sql já contém esta mudança (instalações novas não precisam rodar).
--
-- Pode ser executada mais de uma vez sem erro (idempotente).
-- Atenção: a criação da regra de CPF único falha se já existirem CPFs
-- repetidos. Nesse caso, corrija os registros duplicados e rode de novo
-- (a transação desfaz tudo, o banco não fica pela metade).
-- =====================================================================
BEGIN;

ALTER TABLE tb_usuario ALTER COLUMN email DROP NOT NULL;

ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS cpf      VARCHAR(14),
    ADD COLUMN IF NOT EXISTS telefone VARCHAR(20);

-- CPF único, igual ao 01-schema.sql (o banco aceita vários NULL)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_usuario_cpf') THEN
        ALTER TABLE tb_usuario ADD CONSTRAINT uk_usuario_cpf UNIQUE (cpf);
    END IF;
END $$;

COMMIT;
