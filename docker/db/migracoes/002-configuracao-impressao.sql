BEGIN;

ALTER TABLE tb_configuracao
    ADD COLUMN IF NOT EXISTS taxa_servico_ativa             BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS comanda_unica                  BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS imprimir_comprovante_ao_fechar BOOLEAN NOT NULL DEFAULT FALSE;

COMMIT;
