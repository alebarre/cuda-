package br.com.cuidamais.shared.jobs;

import java.time.Clock;

/**
 * Rotina executada pelo agendador de jobs (plan D-09, D-42): as quatro rotinas de D-09
 * (expiração de pedidos, descarte de contas não confirmadas, retenção e reenvio automático —
 * T-046, T-028, T-034/T-049, T-048) são beans que implementam esta interface e o {@link
 * JobRunner} as executa a cada rodada, cada uma na própria transação, com o {@link Clock} do
 * contexto (D-10).
 *
 * <p>Toda rotina é <strong>idempotente</strong> e reivindica as linhas que processa com {@code
 * SELECT ... FOR UPDATE SKIP LOCKED} (D-42), via {@link SkipLockedClaimer}, para que uma rodada
 * atrasada ou repetida nunca processe a mesma linha duas vezes.
 */
public interface ScheduledRoutine {

    /** Nome curto da rotina, usado só em log (P5: nunca contém dado pessoal). */
    String name();

    /**
     * Executa uma rodada da rotina dentro da transação aberta pelo {@link JobRunner}. O "agora"
     * vem sempre de {@code clock} (D-10).
     */
    void run(Clock clock);
}
