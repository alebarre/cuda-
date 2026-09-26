package br.com.cuidamais.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio injetável (plan D-10): todo código que precisa de "agora" recebe este {@link Clock},
 * para que os testes avancem o tempo (15 min, 30 min, 24 h, 7 dias, 30 dias) sem esperar.
 * Armazenamento em UTC; a exibição em America/Sao_Paulo é responsabilidade da apresentação
 * (Constitution §5).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
