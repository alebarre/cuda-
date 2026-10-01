package br.com.cuidamais.shared.testsupport;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Teste de contrato (T-006, Constitution §6): base reutilizável que valida requisição e resposta
 * do MockMvc contra o {@code openapi.yaml} real da feature 000 — sem duplicar o contrato no
 * classpath de teste, apontando direto para o arquivo em {@code specs/000-fundacao/contracts/}.
 *
 * <p>Biblioteca: {@code com.atlassian.oai:openapi-request-validator-mockmvc} (sucessor do antigo
 * "swagger-request-validator"). Internamente usa {@code io.swagger.parser.v3} 2.1.x, que lê
 * OpenAPI 3.1 com suporte parcial (o modelo interno ainda é o do OAS 3.0, mas isso basta para
 * detectar rotas e corpos fora do contrato — ver relatório da tarefa).
 */
public final class ContractValidation {

    private static final String CONTRACT_RELATIVE_PATH = "specs/000-fundacao/contracts/openapi.yaml";

    private static final OpenApiInteractionValidator VALIDATOR =
            OpenApiInteractionValidator.createFor(resolveContractPath()).build();

    private ContractValidation() {
    }

    /**
     * {@link ResultMatcher} que lança {@code OpenApiMatchers.OpenApiValidationException} se a
     * requisição ou a resposta do {@code MockMvc} divergir do contrato.
     */
    public static ResultMatcher matchesContract() {
        return OpenApiValidationMatchers.openApi().isValid(VALIDATOR);
    }

    public static OpenApiInteractionValidator validator() {
        return VALIDATOR;
    }

    private static String resolveContractPath() {
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 6; i++) {
            Path candidate = dir.resolve(CONTRACT_RELATIVE_PATH);
            if (Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
            Path parent = dir.getParent();
            if (parent == null) {
                break;
            }
            dir = parent;
        }
        throw new IllegalStateException(
                "Não encontrei " + CONTRACT_RELATIVE_PATH + " subindo a partir de "
                        + Paths.get("").toAbsolutePath());
    }
}
