package br.com.cuidamais.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Conta de uma pessoa, com apenas o que {@link AccountState#of} precisa (D-33): o id e a
 * informação de e-mail verificado ({@code users.email_verified_at}).
 *
 * <p>Mapeada como {@code @Entity} sobre a tabela {@code users} (V1__foundation.sql, T-010) para
 * preparar a persistência de tarefas futuras. A T-017 mapeia <strong>somente</strong> as duas
 * colunas que usa; as demais ({@code name}, {@code email}, {@code phone}, endereço,
 * {@code password_hash}, contadores de bloqueio, {@code created_at}) entram com as tarefas que
 * lhes dão regra: o cadastro (T-020), a confirmação do e-mail (T-022) e o login (T-023). Por isso
 * esta entidade ainda não é persistível e nenhuma tarefa deste pacote cria repositório para ela.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private UUID id;

    /** {@code null} enquanto o e-mail não foi confirmado (AC-001.2, D-05). */
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    /** Exigido pelo Hibernate. */
    protected User() {}

    /**
     * Monta uma conta com o mínimo que a T-017 usa.
     *
     * @param id identificador da conta ({@code users.id})
     * @param emailVerifiedAt quando o e-mail foi confirmado; {@code null} = ainda não confirmado
     */
    static User of(UUID id, Instant emailVerifiedAt) {
        User user = new User();
        user.id = id;
        user.emailVerifiedAt = emailVerifiedAt;
        return user;
    }

    public UUID getId() {
        return id;
    }

    /** {@code null} enquanto o e-mail não foi confirmado. */
    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }
}
