package br.com.cuidamais.shared.jobs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade <strong>só de teste</strong> (T-019, D-42) sobre a tabela {@code jobs_test_queue}
 * criada por {@code R__jobs_test_queue.sql} (src/test/resources). Faz o papel de "linha a
 * processar" para provar o {@link SkipLockedClaimer} sem depender das entidades de domínio.
 *
 * <p>{@code processCount} conta quantas vezes a linha foi processada: exatamente 1 é o esperado;
 * 0 é "ficou de fora"; 2 ou mais é "processada em dobro" (o que D-42 proíbe).
 */
@Entity
@Table(name = "jobs_test_queue")
public class JobsTestQueueRow {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "test_batch", nullable = false, updatable = false, length = 64)
    private String testBatch;

    @Column(name = "processed_by", length = 64)
    private String processedBy;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "process_count", nullable = false)
    private int processCount;

    protected JobsTestQueueRow() {}

    public JobsTestQueueRow(UUID id, String testBatch) {
        this.id = id;
        this.testBatch = testBatch;
    }

    /** Simula o "processamento" da linha por uma execução identificada por {@code executionId}. */
    public void markProcessed(String executionId, Instant now) {
        this.processedBy = executionId;
        this.processedAt = now;
        this.processCount++;
    }

    public UUID id() {
        return id;
    }

    public String processedBy() {
        return processedBy;
    }

    public int processCount() {
        return processCount;
    }
}
