package br.com.cuidamais.shared.testsupport;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link Clock} de teste (plan D-10): permite avançar o "agora" sem esperar, para provar prazos
 * como 15 min, 30 min, 24 h, 7 dias e 30 dias.
 */
public final class MutableClock extends Clock {

    private final ZoneId zone;
    private final AtomicReference<Instant> instant;

    public MutableClock(Instant initial, ZoneId zone) {
        this.zone = zone;
        this.instant = new AtomicReference<>(initial);
    }

    public void advance(java.time.Duration duration) {
        instant.updateAndGet(current -> current.plus(duration));
    }

    public void set(Instant newInstant) {
        instant.set(newInstant);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return new MutableClock(instant.get(), newZone);
    }

    @Override
    public Instant instant() {
        return instant.get();
    }
}
