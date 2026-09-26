package br.com.cuidamais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Cuida+ backend. Pacotes por domínio (plan D-01): {@code shared}, {@code auth}, {@code group},
 * {@code membership}, {@code notification}.
 */
@SpringBootApplication
public class CuidaMaisApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuidaMaisApplication.class, args);
    }
}
