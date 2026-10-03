package br.com.cuidamais.shared.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cuidamais.shared.testsupport.IntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TransactionRequiredException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;

/**
 * T-019 "Pronto quando" (plan D-42): duas execuções concorrentes da mesma rotina, reivindicando
 * linhas com {@code SELECT ... FOR UPDATE SKIP LOCKED} pelo {@link SkipLockedClaimer}, não
 * processam a mesma linha duas vezes; e rodar de novo depois de tudo processado não reprocessa
 * nada (idempotência).
 *
 * <p>Usa a tabela só de teste {@code jobs_test_queue} ({@code R__jobs_test_queue.sql}) e a
 * entidade {@link JobsTestQueueRow}. Cada teste insere as próprias linhas com um {@code
 * test_batch} único, então os testes não dependem de ordem nem de limpeza entre si.
 *
 * <p>API esperada (pacote {@code br.com.cuidamais.shared.jobs}):
 *
 * <ul>
 *   <li>{@code SkipLockedClaimer#claim(Class<T>, String jpql, Map<String,Object>, int maxRows)}:
 *       dentro da transação corrente, devolve até {@code maxRows} entidades que satisfazem a JPQL,
 *       bloqueadas com {@code FOR UPDATE SKIP LOCKED}; fora de transação, recusa.
 *   <li>{@code JobRunner#runAll()}: executa cada {@link ScheduledRoutine} registrada na própria
 *       transação, passando o {@code Clock} do contexto.
 * </ul>
 */
@Import(JobsTestRoutines.class)
class SkipLockedClaimTest extends IntegrationTest {

    private static final int LINHAS = 20;
    private static final int LOTE = 4;
    private static final Duration TRABALHO_POR_LOTE = Duration.ofMillis(80);
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(30);

    private static final String JPQL_PENDENTES =
            "select r from JobsTestQueueRow r where r.testBatch = :batch and r.processedBy is null";

    @Autowired
    JobRunner runner;

    @Autowired
    SkipLockedClaimer claimer;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ProgrammableRoutine rotina1;

    @Autowired
    ProgrammableRoutine rotina2;

    @Autowired
    ProgrammableRoutine rotina3;

    private String batch;

    @BeforeEach
    void preparaLote() {
        batch = "batch-" + UUID.randomUUID();
        for (int i = 0; i < LINHAS; i++) {
            jdbc.update("INSERT INTO jobs_test_queue (id, test_batch) VALUES (?, ?)", UUID.randomUUID(), batch);
        }
    }

    @AfterEach
    void limpa() {
        rotina1.reset();
        rotina2.reset();
        rotina3.reset();
        jdbc.update("DELETE FROM jobs_test_queue WHERE test_batch = ?", batch);
    }

    // ------------------------------------------------------------------ D-42: concorrência

    @Test
    @DisplayName("D-42 duas execuções concorrentes da mesma rotina processam cada linha exatamente uma vez (SKIP LOCKED)")
    void d_42_duas_execucoes_concorrentes_nao_processam_a_mesma_linha_duas_vezes() throws Exception {
        CountDownLatch largada = new CountDownLatch(2);
        Map<String, Integer> processadasPorExecucao = new ConcurrentHashMap<>();
        AtomicReference<Throwable> falhaNaRotina = new AtomicReference<>();
        rotina1.setBody(processadora(largada, processadasPorExecucao, falhaNaRotina));

        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<?> execucaoA = threads.submit(runner::runAll);
            Future<?> execucaoB = threads.submit(runner::runAll);
            execucaoA.get(ESPERA_MAXIMA.toSeconds(), TimeUnit.SECONDS);
            execucaoB.get(ESPERA_MAXIMA.toSeconds(), TimeUnit.SECONDS);
        } finally {
            threads.shutdownNow();
        }

        assertThat(falhaNaRotina.get()).as("a rotina de teste não lançou exceção").isNull();
        assertThat(contagem("process_count = 1"))
                .as("cada uma das %d linhas foi processada exatamente uma vez", LINHAS)
                .isEqualTo(LINHAS);
        assertThat(contagem("process_count >= 2")).as("nenhuma linha processada em dobro").isZero();
        assertThat(contagem("process_count = 0 OR processed_by IS NULL")).as("nenhuma linha ficou de fora").isZero();
        assertThat(processadasPorExecucao)
                .as("as duas execuções processaram alguma linha (não foi serialização por acaso)")
                .hasSize(2)
                .allSatisfy((execucao, processadas) -> assertThat(processadas).isPositive());
        assertThat(processadasPorExecucao.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(LINHAS);
    }

    // ------------------------------------------------------------------ D-42: idempotência

    @Test
    @DisplayName("D-42 rodar a rotina de novo depois de tudo processado não reprocessa nada")
    void d_42_segunda_rodada_depois_de_tudo_processado_nao_reprocessa() {
        Map<String, Integer> processadasPorExecucao = new ConcurrentHashMap<>();
        AtomicReference<Throwable> falhaNaRotina = new AtomicReference<>();
        rotina1.setBody(processadora(new CountDownLatch(0), processadasPorExecucao, falhaNaRotina));

        runner.runAll();
        assertThat(falhaNaRotina.get()).isNull();
        assertThat(processadasPorExecucao).containsEntry("exec-1", LINHAS);
        assertThat(contagem("process_count = 1")).isEqualTo(LINHAS);

        runner.runAll();

        assertThat(falhaNaRotina.get()).isNull();
        assertThat(processadasPorExecucao).as("a segunda execução não reivindicou linha nenhuma").containsEntry("exec-2", 0);
        assertThat(contagem("process_count = 1")).as("contador de processamentos não mudou").isEqualTo(LINHAS);
        assertThat(contagem("process_count <> 1")).isZero();
    }

    // ------------------------------------------------------------------ D-42: lock só dentro de transação

    @Test
    @DisplayName("D-42 reivindicar linhas fora de transação é recusado (o lock só vale dentro da transação da rotina)")
    void d_42_claim_fora_de_transacao_e_recusado() {
        assertThatThrownBy(() -> claimer.claim(JobsTestQueueRow.class, JPQL_PENDENTES, Map.of("batch", batch), LOTE))
                .isInstanceOfAny(
                        TransactionRequiredException.class,
                        IllegalTransactionStateException.class,
                        InvalidDataAccessApiUsageException.class);
    }

    // ------------------------------------------------------------------ apoio

    /**
     * Corpo de rotina que reivindica lotes de linhas pendentes do {@code batch} com SKIP LOCKED,
     * simula trabalho (sleep dentro da transação, para que a outra execução encontre as linhas
     * bloqueadas) e marca cada linha com o identificador da execução ({@code exec-N}).
     */
    private Consumer<Clock> processadora(
            CountDownLatch largada, Map<String, Integer> processadasPorExecucao, AtomicReference<Throwable> falha) {
        AtomicInteger execucoes = new AtomicInteger();
        return clock -> {
            String execucao = "exec-" + execucoes.incrementAndGet();
            try {
                largada.countDown();
                if (!largada.await(ESPERA_MAXIMA.toSeconds(), TimeUnit.SECONDS)) {
                    throw new IllegalStateException("a outra execução não chegou à largada");
                }
                int processadas = 0;
                while (true) {
                    List<JobsTestQueueRow> lote =
                            claimer.claim(JobsTestQueueRow.class, JPQL_PENDENTES, Map.of("batch", batch), LOTE);
                    if (lote.isEmpty()) {
                        break;
                    }
                    assertThat(lote).as("o claim respeita maxRows").hasSizeLessThanOrEqualTo(LOTE);
                    Thread.sleep(TRABALHO_POR_LOTE.toMillis());
                    for (JobsTestQueueRow linha : lote) {
                        linha.markProcessed(execucao, clock.instant());
                    }
                    entityManager.flush();
                    processadas += lote.size();
                }
                processadasPorExecucao.put(execucao, processadas);
            } catch (Throwable t) {
                falha.compareAndSet(null, t);
                throw t instanceof RuntimeException re ? re : new IllegalStateException(t);
            }
        };
    }

    private int contagem(String condicao) {
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM jobs_test_queue WHERE test_batch = ? AND (" + condicao + ")", Integer.class, batch);
        return n == null ? 0 : n;
    }
}
