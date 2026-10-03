package br.com.cuidamais.shared.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import br.com.cuidamais.shared.testsupport.IntegrationTest;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * T-019 (plan D-09, D-10): o {@link JobRunner} executa <strong>todas</strong> as rotinas
 * registradas, cada uma na <strong>própria transação</strong>, com o {@code Clock} do contexto;
 * uma rotina que falha não impede as demais nem derruba o agendador, e o log da falha não carrega
 * dado pessoal (Constitution P5).
 *
 * <p>As três rotinas vêm de {@link JobsTestRoutines}; cada teste programa só o que precisa.
 */
@Import(JobsTestRoutines.class)
@ExtendWith(OutputCaptureExtension.class)
class JobRunnerTest extends IntegrationTest {

    private static final String EMAIL_PESSOAL = "ana.souza@example.com";

    @Autowired
    JobRunner runner;

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

    @AfterEach
    void limpa() {
        rotina1.reset();
        rotina2.reset();
        rotina3.reset();
    }

    // ------------------------------------------------------------------ D-09: isolamento entre rotinas

    @Test
    @DisplayName("D-09 rotina que lança exceção não impede as demais nem sai do runAll()")
    void d_09_rotina_com_falha_nao_impede_as_demais_nem_derruba_o_agendador() {
        AtomicBoolean rodou1 = new AtomicBoolean();
        AtomicBoolean rodou3 = new AtomicBoolean();
        rotina1.setBody(clock -> rodou1.set(true));
        rotina2.setBody(clock -> {
            throw new IllegalStateException("falha simulada da rotina 2");
        });
        rotina3.setBody(clock -> rodou3.set(true));

        assertThatCode(runner::runAll).as("a exceção da rotina 2 não sai do runAll()").doesNotThrowAnyException();

        assertThat(rodou1).as("rotina-1 executou").isTrue();
        assertThat(rodou3).as("rotina-3 executou mesmo depois da falha da rotina-2").isTrue();
    }

    @Test
    @DisplayName("D-09/P5 log da falha diz o nome da rotina e o tipo da exceção, sem dado pessoal")
    void d_09_log_da_falha_tem_nome_da_rotina_e_tipo_da_excecao_sem_dado_pessoal(CapturedOutput output) {
        rotina2.setBody(clock -> {
            throw new IllegalStateException("falha ao processar " + EMAIL_PESSOAL);
        });

        runner.runAll();

        String log = output.getAll();
        assertThat(log).contains("rotina-2").contains("IllegalStateException");
        assertThat(log).as("P5: o log não carrega o e-mail da mensagem da exceção").doesNotContain(EMAIL_PESSOAL);
    }

    // ------------------------------------------------------------------ D-09/D-42: transação própria por rotina

    @Test
    @DisplayName("D-09 cada rotina roda na própria transação: a escrita da que falhou é desfeita, a da seguinte persiste")
    void d_09_cada_rotina_roda_na_propria_transacao() {
        String batch = "tx-" + UUID.randomUUID();
        UUID linhaDaRotinaQueFalha = UUID.randomUUID();
        UUID linhaDaRotinaQueCompleta = UUID.randomUUID();
        rotina1.setBody(clock -> {
            entityManager.persist(new JobsTestQueueRow(linhaDaRotinaQueFalha, batch));
            entityManager.flush();
            throw new IllegalStateException("falha depois de escrever");
        });
        rotina2.setBody(clock -> entityManager.persist(new JobsTestQueueRow(linhaDaRotinaQueCompleta, batch)));

        try {
            runner.runAll();

            assertThat(existe(linhaDaRotinaQueFalha)).as("escrita da rotina que falhou foi desfeita (rollback)").isFalse();
            assertThat(existe(linhaDaRotinaQueCompleta)).as("escrita da rotina seguinte persistiu (commit)").isTrue();
        } finally {
            jdbc.update("DELETE FROM jobs_test_queue WHERE test_batch = ?", batch);
        }
    }

    // ------------------------------------------------------------------ D-10: Clock injetado

    @Test
    @DisplayName("D-10 a rotina recebe o Clock do contexto: avançar o relógio muda o agora visto pela rotina")
    void d_10_rotina_recebe_o_clock_do_contexto_e_ve_o_relogio_avancar() {
        AtomicReference<Instant> agoraVisto = new AtomicReference<>();
        rotina1.setBody(c -> agoraVisto.set(c.instant()));

        Instant antes = clock.instant();
        runner.runAll();
        assertThat(agoraVisto.get()).as("o agora da rotina é o do MutableClock, não o do sistema").isEqualTo(antes);

        clock.advance(Duration.ofHours(24));
        runner.runAll();

        assertThat(agoraVisto.get()).isEqualTo(antes.plus(Duration.ofHours(24)));
    }

    private boolean existe(UUID id) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM jobs_test_queue WHERE id = ?", Integer.class, id);
        return n != null && n > 0;
    }
}
