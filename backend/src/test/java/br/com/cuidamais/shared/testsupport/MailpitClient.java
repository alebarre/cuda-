package br.com.cuidamais.shared.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

/**
 * Cliente mínimo da API HTTP do Mailpit (v1.29) para os testes de integração lerem e apagarem as
 * mensagens recebidas. Reutilizável por todas as tarefas que enviam e-mail (T-016 em diante).
 *
 * <p>Endpoints usados: {@code GET /api/v1/messages} (lista), {@code GET /api/v1/message/{ID}}
 * (corpo) e {@code DELETE /api/v1/messages} (apaga tudo).
 */
public final class MailpitClient {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() {};

    private final RestClient http;

    public MailpitClient(String host, int apiPort) {
        this.http = RestClient.create("http://" + host + ":" + apiPort);
    }

    /** Resumo de uma mensagem na listagem. */
    public record Summary(String id, String fromAddress, List<String> to, String subject, String snippet) {}

    /** Mensagem completa. */
    public record Detail(
            String id,
            String fromName,
            String fromAddress,
            List<String> to,
            String subject,
            String text,
            String html) {}

    /** Apaga todas as mensagens guardadas. */
    public void deleteAll() {
        http.delete().uri("/api/v1/messages").retrieve().toBodilessEntity();
    }

    /** Total de mensagens guardadas (campo {@code total} da listagem). */
    public int count() {
        Map<String, Object> body = http.get().uri("/api/v1/messages").retrieve().body(JSON_OBJECT);
        return ((Number) body.get("total")).intValue();
    }

    /** Todas as mensagens (mais recente primeiro, como o Mailpit devolve). */
    @SuppressWarnings("unchecked")
    public List<Summary> messages() {
        Map<String, Object> body = http.get().uri("/api/v1/messages").retrieve().body(JSON_OBJECT);
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.getOrDefault("messages", List.of());
        List<Summary> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            result.add(new Summary(
                    (String) item.get("ID"),
                    address((Map<String, Object>) item.get("From")),
                    addresses((List<Map<String, Object>>) item.get("To")),
                    (String) item.get("Subject"),
                    (String) item.get("Snippet")));
        }
        return result;
    }

    /** Mensagens cujo destinatário inclui {@code address}. */
    public List<Summary> messagesTo(String address) {
        return messages().stream().filter(m -> m.to().contains(address)).toList();
    }

    /** Mensagem completa, com corpo em texto e HTML. */
    @SuppressWarnings("unchecked")
    public Detail message(String id) {
        Map<String, Object> body = http.get().uri("/api/v1/message/{id}", id).retrieve().body(JSON_OBJECT);
        Map<String, Object> from = (Map<String, Object>) body.get("From");
        return new Detail(
                (String) body.get("ID"),
                from == null ? null : (String) from.get("Name"),
                address(from),
                addresses((List<Map<String, Object>>) body.get("To")),
                (String) body.get("Subject"),
                (String) body.get("Text"),
                (String) body.get("HTML"));
    }

    private static String address(Map<String, Object> mailbox) {
        return mailbox == null ? null : (String) mailbox.get("Address");
    }

    private static List<String> addresses(List<Map<String, Object>> mailboxes) {
        if (mailboxes == null) {
            return List.of();
        }
        return mailboxes.stream().map(MailpitClient::address).toList();
    }
}
