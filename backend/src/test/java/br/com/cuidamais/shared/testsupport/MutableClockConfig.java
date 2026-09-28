package br.com.cuidamais.shared.testsupport;

import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Substitui o {@code Clock} de produção (plan D-10) por um {@link MutableClock} nos testes de
 * integração, para que cada teste possa avançar o relógio em vez de esperar.
 */
@TestConfiguration
public class MutableClockConfig {

    @Bean
    @Primary
    public MutableClock testClock() {
        return new MutableClock(Instant.now(), ZoneOffset.UTC);
    }
}
