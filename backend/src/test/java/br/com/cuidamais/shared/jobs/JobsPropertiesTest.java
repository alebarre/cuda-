package br.com.cuidamais.shared.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

/**
 * T-019 (plan D-09): o agendador roda <strong>a cada 5 minutos</strong> e vem
 * <strong>ligado</strong> por padrão ({@code application.yml}); o profile {@code test} o desliga
 * ({@code application-test.yml}). Lê os YAML sem subir o contexto, como {@code MailProfilesTest}.
 *
 * <p>Aceita tanto o literal ({@code PT5M}, {@code true}) quanto o padrão de variável de ambiente
 * no estilo do projeto (D-43), por exemplo {@code ${JOBS_INTERVAL:PT5M}}.
 */
class JobsPropertiesTest {

    private static Properties yaml(String arquivo) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(arquivo));
        Properties properties = factory.getObject();
        assertThat(properties).as("%s existe e é YAML válido", arquivo).isNotNull();
        return properties;
    }

    /** Casa {@code valor} ou {@code ${QUALQUER_VARIAVEL:valor}}. */
    private static String literalOuVariavelComPadrao(String valor) {
        String escapado = java.util.regex.Pattern.quote(valor);
        return "^(" + escapado + "|\\$\\{[A-Za-z_][A-Za-z0-9_.]*:" + escapado + "\\})$";
    }

    @Test
    @DisplayName("D-09 application.yml: app.jobs.interval padrão é PT5M (a cada 5 minutos)")
    void d_09_intervalo_padrao_e_cinco_minutos() {
        Properties base = yaml("application.yml");

        assertThat(base.getProperty("app.jobs.interval"))
                .as("app.jobs.interval declarado em application.yml")
                .isNotNull()
                .matches(literalOuVariavelComPadrao("PT5M"));
    }

    @Test
    @DisplayName("D-09 application.yml: app.jobs.enabled padrão é true")
    void d_09_agendador_ligado_por_padrao() {
        Properties base = yaml("application.yml");

        assertThat(base.getProperty("app.jobs.enabled"))
                .as("app.jobs.enabled declarado em application.yml")
                .isNotNull()
                .matches(literalOuVariavelComPadrao("true"));
    }

    @Test
    @DisplayName("D-09 application-test.yml: app.jobs.enabled é false (agendador não dispara nos testes)")
    void d_09_profile_test_desliga_o_agendador() {
        Properties test = yaml("application-test.yml");

        assertThat(test.getProperty("app.jobs.enabled")).isEqualTo("false");
    }
}
