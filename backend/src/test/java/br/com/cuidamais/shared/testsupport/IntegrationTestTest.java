package br.com.cuidamais.shared.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * T-003 "Pronto quando": um teste sobe os containers, roda uma migração vazia e avança o
 * relógio.
 */
class IntegrationTestTest extends IntegrationTest {

    @Autowired
    Flyway flyway;

    @Test
    void sobeContainersRodaMigracaoVaziaEAvancaRelogio() {
        assertThat(POSTGRES.isRunning()).isTrue();
        assertThat(MAILPIT.isRunning()).isTrue();

        assertThat(flyway.info().applied()).isNotEmpty();

        Instant antes = clock.instant();
        clock.advance(Duration.ofDays(30));

        assertThat(clock.instant()).isEqualTo(antes.plus(Duration.ofDays(30)));
    }
}
