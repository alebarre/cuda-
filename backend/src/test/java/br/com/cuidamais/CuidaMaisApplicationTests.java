package br.com.cuidamais;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import br.com.cuidamais.shared.testsupport.IntegrationTest;

/**
 * T-002 "Pronto quando": o contexto sobe com Web, JPA, Security, Validation, Flyway e Mail
 * configurados, contra um PostgreSQL 17 e um Mailpit reais (base de teste do T-003).
 */
class CuidaMaisApplicationTests extends IntegrationTest {

    @Test
    void contextLoads() {
        // D-10: o relógio injetável existe e está em UTC
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
