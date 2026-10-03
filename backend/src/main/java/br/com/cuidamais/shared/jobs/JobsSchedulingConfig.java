package br.com.cuidamais.shared.jobs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga o agendador de jobs (plan D-09): um único {@code @Scheduled} ({@link JobScheduler}) que
 * chama {@link JobRunner#runAll()} a cada {@code app.jobs.interval} (padrão 5 min). Fica no
 * pacote de jobs, e não na classe principal, para que cada domínio ligue só o que precisa (D-01);
 * o {@code @EnableAsync} do e-mail vive em {@code notification}.
 *
 * <p>{@code app.jobs.enabled=false} (profile {@code test}) desliga tudo: nem {@code
 * @EnableScheduling} nem o bean agendado são registrados, e os testes chamam {@code runAll()}
 * diretamente.
 *
 * <p><strong>R-06:</strong> o agendador roda em <strong>uma instância só</strong> no MVP. Com
 * mais de uma réplica seria preciso um lock distribuído (ShedLock ou equivalente); o {@code
 * SKIP LOCKED} de D-42 evita processar a mesma linha duas vezes, mas não evita rodadas
 * simultâneas.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "app.jobs.enabled", havingValue = "true", matchIfMissing = true)
class JobsSchedulingConfig {

    @Bean
    JobScheduler jobScheduler(JobRunner runner) {
        return new JobScheduler(runner);
    }
}
