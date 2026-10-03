package br.com.cuidamais.shared.jobs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registra três {@link ProgrammableRoutine} como beans {@link ScheduledRoutine} (T-019). Os
 * testes importam esta configuração com {@code @Import(JobsTestRoutines.class)} e programam o
 * corpo de cada rotina conforme o comportamento que provam.
 */
@TestConfiguration(proxyBeanMethods = false)
public class JobsTestRoutines {

    @Bean
    public ProgrammableRoutine rotina1() {
        return new ProgrammableRoutine("rotina-1");
    }

    @Bean
    public ProgrammableRoutine rotina2() {
        return new ProgrammableRoutine("rotina-2");
    }

    @Bean
    public ProgrammableRoutine rotina3() {
        return new ProgrammableRoutine("rotina-3");
    }
}
