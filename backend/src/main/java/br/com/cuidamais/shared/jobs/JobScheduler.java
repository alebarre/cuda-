package br.com.cuidamais.shared.jobs;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * O único ponto {@code @Scheduled} da aplicação (plan D-09, R-06): a cada {@code
 * app.jobs.interval} (intervalo fixo, contado a partir do fim da rodada anterior, para que duas
 * rodadas da mesma instância nunca se sobreponham) chama {@link JobRunner#runAll()}, que executa
 * cada {@link ScheduledRoutine} na própria transação.
 *
 * <p>Registrado só por {@link JobsSchedulingConfig}, isto é, só quando {@code app.jobs.enabled}
 * é {@code true}. <strong>R-06:</strong> pensado para uma instância só no MVP; sem lock
 * distribuído, mais de uma réplica dispararia rodadas simultâneas.
 */
class JobScheduler {

    private final JobRunner runner;

    JobScheduler(JobRunner runner) {
        this.runner = runner;
    }

    @Scheduled(fixedDelayString = "${app.jobs.interval}")
    void tick() {
        runner.runAll();
    }
}
