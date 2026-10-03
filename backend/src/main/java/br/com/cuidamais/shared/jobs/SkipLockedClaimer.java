package br.com.cuidamais.shared.jobs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import org.hibernate.Timeouts;
import org.hibernate.jpa.SpecHints;
import org.springframework.stereotype.Component;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Apoio das rotinas (plan D-42) para <strong>reivindicar</strong> linhas com {@code SELECT ...
 * FOR UPDATE SKIP LOCKED} dentro da transação da rotina: duas execuções concorrentes da mesma
 * rotina nunca recebem a mesma linha. Trabalha sobre entidades JPA ({@code Membership}, {@code
 * Invitation}, {@code InviteCodeAttempts}, {@code User}), que é o que as rotinas de D-09 tocam.
 *
 * <p>O lock é {@link LockModeType#PESSIMISTIC_WRITE} com o timeout especial {@link
 * Timeouts#SKIP_LOCKED_MILLI} do Hibernate (o hint JPA só aceita inteiro), que no PostgreSQL
 * vira {@code FOR UPDATE SKIP LOCKED}; o
 * limite de linhas vira {@code FETCH FIRST n ROWS ONLY} na mesma consulta, então o lote é
 * bloqueado atomicamente.
 */
@Component
public class SkipLockedClaimer {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Executa a consulta JPQL com bloqueio pessimista de escrita e {@code SKIP LOCKED}, devolvendo
     * até {@code maxRows} entidades gerenciadas, já bloqueadas até o fim da transação corrente.
     *
     * @param entityType tipo da entidade selecionada
     * @param jpql consulta JPQL de seleção da entidade (ex.: {@code "select m from Membership m
     *     where m.status = :status and m.expiresAt <= :now"}), sem cláusula de lock
     * @param parameters parâmetros nomeados da consulta
     * @param maxRows limite de linhas por reivindicação
     * @throws IllegalTransactionStateException se não houver transação ativa — o lock só faz
     *     sentido dentro da transação da rotina
     */
    public <T> List<T> claim(Class<T> entityType, String jpql, Map<String, Object> parameters, int maxRows) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalTransactionStateException(
                    "SkipLockedClaimer.claim exige transação ativa: o lock só vale dentro da transação da rotina");
        }
        TypedQuery<T> query = entityManager
                .createQuery(jpql, entityType)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint(SpecHints.HINT_SPEC_LOCK_TIMEOUT, Timeouts.SKIP_LOCKED_MILLI)
                .setMaxResults(maxRows);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }
}
