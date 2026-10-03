package br.com.cuidamais.shared.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.cuidamais.shared.testsupport.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * T-019 (plan D-09): no profile {@code test} o agendador está <strong>desligado</strong>
 * ({@code app.jobs.enabled=false} em {@code application-test.yml}), para que o {@code @Scheduled}
 * não dispare no meio dos testes de integração; os testes chamam {@code JobRunner.runAll()}
 * diretamente.
 *
 * <p>Este teste é uma guarda: passa antes da implementação e deve continuar passando depois.
 */
class JobSchedulerDisabledInTestProfileTest extends IntegrationTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    Environment environment;

    @Test
    @DisplayName("D-09 no profile test app.jobs.enabled é false e nenhuma tarefa fica agendada")
    void d_09_no_profile_test_o_agendador_esta_desligado() {
        assertThat(environment.getProperty("app.jobs.enabled", Boolean.class))
                .as("application-test.yml desliga o agendador")
                .isFalse();
        assertThat(ScheduledTasks.of(context)).as("nenhuma tarefa @Scheduled registrada no profile test").isEmpty();
    }
}
