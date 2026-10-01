package br.com.cuidamais.shared.testsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * T-007: extrai schemas de {@code #/components/schemas/<name>} do {@code openapi.yaml} real da
 * feature 000 e os expõe como {@code com.networknt.schema.Schema} prontos para validar um
 * {@link JsonNode} de resposta — a mesma ideia do teste de contrato da T-006
 * ({@link ContractValidation}), mas validando contra um schema nomeado em vez de uma
 * rota/operação completa (útil aqui porque os endpoints de teste do {@code GlobalExceptionHandler}
 * não existem no contrato — só os schemas de erro existem).
 *
 * <p>Estratégia: o documento inteiro é relido via {@code io.swagger.parser.v3} (já usado pela
 * T-006) e serializado para {@link JsonNode} com {@code io.swagger.v3.core.util.Json}, que é o
 * {@code ObjectMapper} que o próprio swagger-parser usa internamente — preserva a estrutura
 * {@code components.schemas.*} tal como o YAML original. Para resolver o {@code $ref} relativo
 * usado no contrato (ex. {@code '#/components/schemas/Problem'} dentro de
 * {@code ValidationProblem}), o schema devolvido é um nó sintético
 * {@code {"$ref": "#/components/schemas/<name>", "components": <components do documento>}}: o
 * validador resolve o ponteiro JSON contra esse mesmo nó raiz, então o {@code $ref} aponta de
 * volta para dentro de {@code components.schemas}, exatamente como fosse o documento inteiro.
 */
public final class ContractSchemas {

    private static final String CONTRACT_RELATIVE_PATH = "specs/000-fundacao/contracts/openapi.yaml";

    private static final JsonNode DOCUMENT_NODE = parseDocumentAsJsonNode();

    private static final SchemaRegistry REGISTRY =
            SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

    private ContractSchemas() {
    }

    /** Schema de {@code #/components/schemas/<name>} do {@code openapi.yaml}, com $ref resolvido. */
    public static Schema schemaFor(String name) {
        ObjectNode entryPoint = ((ObjectNode) DOCUMENT_NODE).objectNode();
        entryPoint.put("$ref", "#/components/schemas/" + name);
        entryPoint.set("components", DOCUMENT_NODE.get("components"));
        return REGISTRY.getSchema(entryPoint);
    }

    private static JsonNode parseDocumentAsJsonNode() {
        OpenAPI openApi = new OpenAPIV3Parser().read(resolveContractPath());
        if (openApi == null) {
            throw new IllegalStateException(
                    "Não consegui ler " + CONTRACT_RELATIVE_PATH + " com OpenAPIV3Parser.");
        }
        return Json.mapper().valueToTree(openApi);
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
