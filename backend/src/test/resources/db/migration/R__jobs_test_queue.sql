-- T-019 (D-42): tabela SÓ de teste (vive em src/test/resources, nunca em produção) para provar
-- que duas execuções concorrentes da mesma rotina, reivindicando linhas com
-- SELECT ... FOR UPDATE SKIP LOCKED, não processam a mesma linha duas vezes.
-- Mapeada pela entidade de teste br.com.cuidamais.shared.jobs.JobsTestQueueRow.
CREATE TABLE IF NOT EXISTS jobs_test_queue (
    id            UUID PRIMARY KEY,
    test_batch    VARCHAR(64) NOT NULL,
    processed_by  VARCHAR(64) NULL,
    processed_at  TIMESTAMPTZ NULL,
    process_count INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS ix_jobs_test_queue_batch ON jobs_test_queue (test_batch);
