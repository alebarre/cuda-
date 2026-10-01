package br.com.cuidamais.shared.testsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlassian.oai.validator.mockmvc.OpenApiMatchers;
import com.atlassian.oai.validator.report.ValidationReport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * T-006 "Pronto quando": uma rota propositalmente fora do contrato faz o teste falhar.
 *
 * <p>{@link RotaForaDoContratoController} existe só aqui (nunca em {@code src/main}): é um
 * controller real, registrado via {@code MockMvcBuilders.standaloneSetup} (sem contexto Spring
 * completo, por não ser necessário para esta prova), que responde {@code 200 OK} para uma rota
 * que não está no {@code openapi.yaml} da feature 000.
 *
 * <p>A asserção central é sobre a violação ser reportada pelo validador de contrato (Constitution
 * §6), não sobre o status HTTP da chamada: o teste primeiro confirma que a rota responde
 * normalmente (200) e só então prova, com {@code assertThrows}, que validar essa mesma troca
 * contra o contrato lança {@code OpenApiMatchers.OpenApiValidationException}. Se alguém remover
 * {@link ContractValidation#matchesContract()} da chamada, nenhuma exceção é lançada e o
 * {@code assertThrows} falha — é essa linha que sustenta o teste, não o 200 do passo anterior.
 */
class ContractValidationTest {

    private final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(new RotaForaDoContratoController()).build();

    @Test
    void rotaForaDoContratoRespondeNormalmenteForaDaValidacaoDeContrato() throws Exception {
        mockMvc.perform(get("/rota-fora-do-contrato")).andExpect(status().isOk());
    }

    @Test
    void rotaForaDoContratoFalhaNaValidacaoDeContrato() {
        OpenApiMatchers.OpenApiValidationException violacao = assertThrows(
                OpenApiMatchers.OpenApiValidationException.class,
                () -> mockMvc.perform(get("/rota-fora-do-contrato"))
                        .andExpect(ContractValidation.matchesContract()));

        ValidationReport report = violacao.getValidationReport();
        assertThat(report.hasErrors()).isTrue();
    }

    @RestController
    static class RotaForaDoContratoController {

        @GetMapping(path = "/rota-fora-do-contrato", produces = MediaType.APPLICATION_JSON_VALUE)
        Map<String, Object> rota() {
            return Map.of("ok", true);
        }
    }
}
