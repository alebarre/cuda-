# Cuida+ — instruções para o Claude Code

Este projeto segue **Spec-Driven Development**. A fonte da verdade são os documentos em `specs/`.

## Ordem de leitura

1. `specs/constitution.md` — princípios que valem sempre
2. `specs/roadmap.md` — em que feature estamos
3. `specs/<feature>/spec.md` → `plan.md` → `contracts/openapi.yaml` → `tasks.md`
4. `JORNADA-SDD.md` — mapa do aprendizado e status

## Regras de ouro

- **Nunca** implemente algo que não esteja em uma tarefa do `tasks.md`.
- Se o código precisar divergir da spec, do plan ou do contrato: **pare**, proponha a mudança no
  documento e só continue depois de aprovada (Constitution §7).
- Testes primeiro. Todo teste cita o AC que prova (`AC-xxx.y`).
- IDs (US, AC, D, R, T) nunca são renumerados.
- NUNCA commite, faça merges ou pushes no GIT de ABSOLUTAMENTE NADA sem minha permissão/pedido expresso.
- NUNCA insira mensagem nenhuma (co-autoria, co-participação, revisão, nada) no GIT sem minha expressa permissão/pedido.

## Agentes (`.claude/agents/`)

Fluxo por tarefa: **autor-testes → implementador-backend / implementador-frontend → verificador**.

- `revisor-spec`: antes de uma spec avançar para o plan.
- Tarefas marcadas `[P]` podem rodar em paralelo, cada uma com seu trio.
- Só marque a tarefa como `[x]` no `tasks.md` depois do veredito "tarefa aprovada" do verificador.
- O humano (Alexandre) é o dono da spec e dá a palavra final.
