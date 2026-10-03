package br.com.cuidamais.shared.jobs;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Executa todas as {@link ScheduledRoutine} registradas no contexto, cada uma na própria
 * transação, com o {@code Clock} injetado (plan D-09, D-10, D-42). Uma rotina que lança exceção
 * tem só a sua transação desfeita, não impede as demais nem derruba o agendador; o log registra
 * apenas o nome da rotina e o tipo da exceção, nunca a mensagem nem o stack trace, que poderiam
 * carregar dado pessoal (Constitution P5).
 *
 * <p>Cada rotina roda em {@code PROPAGATION_REQUIRES_NEW}, de modo que uma chamada a {@link
 * #runAll()} feita de dentro de uma transação (caso de alguns testes) ainda isola o commit e o
 * rollback de cada rotina.
 */
@Component
public class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);

    private final List<ScheduledRoutine> routines;
    private final Clock clock;
    private final TransactionTemplate transactionPerRoutine;

    public JobRunner(List<ScheduledRoutine> routines, Clock clock, PlatformTransactionManager transactionManager) {
        this.routines = List.copyOf(routines);
        this.clock = clock;
        this.transactionPerRoutine = new TransactionTemplate(transactionManager);
        this.transactionPerRoutine.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Uma rodada: executa todas as rotinas registradas, cada uma na própria transação. */
    public void runAll() {
        for (ScheduledRoutine routine : routines) {
            runIsolated(routine);
        }
    }

    private void runIsolated(ScheduledRoutine routine) {
        try {
            transactionPerRoutine.executeWithoutResult(status -> routine.run(clock));
        } catch (RuntimeException e) {
            // P5: só o nome da rotina e o tipo da exceção; sem mensagem e sem stack trace.
            log.error("Rotina agendada '{}' falhou com {}; transação desfeita, as demais seguem",
                    routine.name(), e.getClass().getSimpleName());
        }
    }
}
