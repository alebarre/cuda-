package br.com.cuidamais;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * T-002 "Pronto quando": o contexto sobe com Web, JPA, Security, Validation, Flyway e Mail
 * configurados, contra um PostgreSQL 17 real (Testcontainers). T-003 extrai daqui a base
 * reutilizável com Mailpit e relógio ajustável.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CuidaMaisApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    Clock clock;

    @Test
    void contextLoads() {
        // D-10: o relógio injetável existe e está em UTC
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
