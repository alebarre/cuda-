package br.com.cuidamais.shared.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.cuidamais.shared.testsupport.IntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.test.context.TestPropertySource;

/**
 * T-019 (plan D-09, R-06): com {@code app.jobs.enabled=true} existe <strong>um único</strong>
 * agendamento {@code @Scheduled}, em intervalo fixo lido de {@code app.jobs.interval}, apontando
 * para o pacote de jobs (que chama {@code JobRunner.runAll()}). O intervalo aqui é 1 h só para o
 * agendador não disparar de novo durante o teste; o padrão de 5 min é provado por {@link
 * JobsPropertiesTest} lendo o {@code application.yml}.
 *
 * <p>Contexto próprio (propriedades diferentes), por isso não compartilha o cache com os outros
 * testes de integração.
 */
@TestPropertySource(properties = {"app.jobs.enabled=true", "app.jobs.interval=PT1H"})
class JobSchedulerEnabledTest extends IntegrationTest {

    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("D-09 com app.jobs.enabled=true há exatamente um agendamento, em intervalo fixo igual a app.jobs.interval")
    void d_09_agendador_ligado_registra_um_unico_agendamento_com_o_intervalo_configurado() {
        List<ScheduledTask> tarefas = ScheduledTasks.of(context);

        assertThat(tarefas).as("um único agendador (D-09, R-06)").hasSize(1);
        assertThat(tarefas.get(0).getTask())
                .as("agendamento em intervalo fixo (fixedDelay/fixedRate), não cron")
                .isInstanceOfSatisfying(IntervalTask.class, tarefa ->
                        assertThat(tarefa.getIntervalDuration()).isEqualTo(Duration.ofHours(1)));
        // Task#toString() delega ao ScheduledMethodRunnable e devolve "classe.metodo"; o wrapper
        // interno do Spring 7 (Task$OutcomeTrackingRunnable) não expõe o delegado de outro jeito.
        assertThat(tarefas.get(0).getTask().toString())
                .as("o método agendado pertence ao pacote de jobs (D-01)")
                .startsWith("br.com.cuidamais.shared.jobs.");
    }
}
