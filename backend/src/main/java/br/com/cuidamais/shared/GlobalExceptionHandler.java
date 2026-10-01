package br.com.cuidamais.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Tratamento global de erros em Problem Details (RFC 9457, T-007; plan D-32, D-40; Constitution
 * §5/P5).
 *
 * <p>Todo corpo de erro usa {@link ProblemDetail} (suporte nativo do Spring à RFC 9457); campos
 * de extensão (ex. {@code code}, {@code errors}) entram via {@link ProblemDetail#setProperty} e o
 * {@code Jackson2ObjectMapperBuilder} padrão do Spring Boot os serializa achatados no nível raiz
 * do JSON — exatamente a forma do {@code allOf} usado pelo contrato para {@code ValidationProblem}
 * (ver teste de contrato em {@code GlobalExceptionHandlerTest}, que confere isso no JSON real).
 *
 * <p><strong>Nunca</strong> inclui stack trace, mensagem bruta de exceção, valor rejeitado de
 * campo ou qualquer outro dado potencialmente sensível no corpo da resposta (P5) — só textos
 * genéricos de desenvolvedor em {@code title}/{@code detail} e um {@code code} estável. O texto
 * exato exibido ao usuário é responsabilidade do catálogo de mensagens do frontend (D-40, tarefa
 * futura).
 *
 * <p>{@link ResourceNotFoundException} cobre ao mesmo tempo "recurso de outro grupo" e "recurso
 * inexistente" (D-32, AC-015.3): como nem a exceção nem este handler guardam qual dos dois
 * motivos ocorreu, o corpo produzido aqui é idêntico nos dois casos — só {@code instance} (o path
 * da requisição) varia naturalmente entre chamadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI VALIDATION_ERROR_TYPE = URI.create("/problems/validation-error");
    private static final URI NOT_ALLOWED_TYPE = URI.create("/problems/not-allowed");
    private static final URI NOT_FOUND_TYPE = URI.create("/problems/not-found");
    private static final URI INTERNAL_ERROR_TYPE = URI.create("/problems/internal-error");

    /** AC-015.3 / D-32: validação de {@code @Valid @RequestBody}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBodyValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::fieldErrorOf)
                .toList();
        return validationProblem(errors, request);
    }

    /** Validação de parâmetros de método (ex. {@code @RequestParam @NotBlank}). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleParameterValidation(
            HandlerMethodValidationException exception, HttpServletRequest request) {
        List<Map<String, String>> errors = exception.getParameterValidationResults().stream()
                .map(result -> {
                    String field = result.getMethodParameter().getParameterName();
                    String message = result.getResolvableErrors().stream()
                            .findFirst()
                            .map(MessageSourceResolvable::getDefaultMessage)
                            .orElse("Valor inválido.");
                    return fieldError(field == null ? "parâmetro" : field, message);
                })
                .toList();
        return validationProblem(errors, request);
    }

    /** Validação de parâmetros fora de controllers (ex. serviço com {@code @Validated}). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<Map<String, String>> errors = exception.getConstraintViolations().stream()
                .map(GlobalExceptionHandler::fieldErrorOf)
                .toList();
        return validationProblem(errors, request);
    }

    /** D-32, AC-015.2: ação sobre recurso do próprio grupo vedada ao papel. */
    @ExceptionHandler(NotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleNotAllowed(
            NotAllowedException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "Esta ação não é permitida para o papel do usuário.");
        problem.setType(NOT_ALLOWED_TYPE);
        problem.setTitle("Ação não permitida");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "NOT_ALLOWED");
        return problemResponse(HttpStatus.FORBIDDEN, problem);
    }

    /** D-32, AC-015.3: recurso de outro grupo ou inexistente — corpo idêntico nos dois casos. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "O recurso solicitado não foi encontrado.");
        problem.setType(NOT_FOUND_TYPE);
        problem.setTitle("Recurso não encontrado");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problemResponse(HttpStatus.NOT_FOUND, problem);
    }

    /**
     * Fallback para qualquer exceção não tratada: garante que nenhum stack trace ou mensagem
     * crua escape em produção (P5), mesmo para erros imprevistos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado.");
        problem.setType(INTERNAL_ERROR_TYPE);
        problem.setTitle("Erro interno");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    private ResponseEntity<ProblemDetail> validationProblem(
            List<Map<String, String>> errors, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "A requisição contém campos inválidos.");
        problem.setType(VALIDATION_ERROR_TYPE);
        problem.setTitle("Dados inválidos");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", errors);
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    private ResponseEntity<ProblemDetail> problemResponse(HttpStatus status, ProblemDetail body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private static Map<String, String> fieldErrorOf(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        return fieldError(fieldError.getField(), message == null ? "Valor inválido." : message);
    }

    private static Map<String, String> fieldErrorOf(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        return fieldError(field.isBlank() ? "parâmetro" : field, violation.getMessage());
    }

    private static Map<String, String> fieldError(String field, String message) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("field", field);
        error.put("message", message);
        return error;
    }
}
