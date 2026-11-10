BEGIN;

ALTER TABLE tb_usuario ALTER COLUMN email DROP NOT NULL;

ALTER TABLE tb_usuario
    ADD COLUMN IF NOT EXISTS cpf      VARCHAR(14),
    ADD COLUMN IF NOT EXISTS telefone VARCHAR(20);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_usuario_cpf') THEN
        ALTER TABLE tb_usuario ADD CONSTRAINT uk_usuario_cpf UNIQUE (cpf);
    END IF;
END $$;

COMMIT;
