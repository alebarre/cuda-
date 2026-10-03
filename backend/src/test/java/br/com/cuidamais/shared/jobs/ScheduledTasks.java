package br.com.cuidamais.shared.jobs;

import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

/**
 * Apoio de teste (T-019): lista as tarefas {@code @Scheduled} registradas no contexto, lendo todos
 * os {@link ScheduledTaskHolder} (o pós-processador de {@code @EnableScheduling} é um deles). Sem
 * {@code @EnableScheduling} não há holder nenhum e a lista é vazia.
 */
final class ScheduledTasks {

    private ScheduledTasks() {}

    static List<ScheduledTask> of(ApplicationContext context) {
        return context.getBeansOfType(ScheduledTaskHolder.class).values().stream()
                .flatMap(holder -> holder.getScheduledTasks().stream())
                .toList();
    }
}
