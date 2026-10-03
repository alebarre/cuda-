package br.com.cuidamais.shared.jobs;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * {@link ScheduledRoutine} de teste (T-019) cujo corpo cada teste programa com {@link #setBody}
 * e devolve ao no-op com {@link #reset}. Três instâncias ({@code rotina-1}, {@code rotina-2},
 * {@code rotina-3}) são registradas por {@link JobsTestRoutines} para que um único contexto
 * sirva a todos os testes do {@link JobRunner}.
 */
public final class ProgrammableRoutine implements ScheduledRoutine {

    private static final Consumer<Clock> NO_OP = clock -> {};

    private final String name;
    private final AtomicReference<Consumer<Clock>> body = new AtomicReference<>(NO_OP);

    public ProgrammableRoutine(String name) {
        this.name = name;
    }

    public void setBody(Consumer<Clock> newBody) {
        body.set(newBody);
    }

    public void reset() {
        body.set(NO_OP);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void run(Clock clock) {
        body.get().accept(clock);
    }
}
