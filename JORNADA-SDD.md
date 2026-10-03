# Jornada Spec-Driven Development: Cuida+

> O mapa do aprendizado: onde estamos no ciclo, o que cada artefato ensinou e o que vem depois.
> Atualizado a cada etapa. Última atualização: 2026-09-25.

## 1. Onde estamos

```
 PROJETO (uma vez)                    POR FEATURE (repete para 000, 001, 002…)
 ┌──────────────┐  ┌──────────┐      ┌──────┐   ┌──────┐   ┌──────────┐   ┌───────┐   ┌───────────┐   ┌─────────┐
 │ Constitution │→ │ Roadmap  │  →   │ Spec │ → │ Plan │ → │ Contrato │ → │ Tasks │ → │ Implement │ → │ Validar │
 └──────────────┘  └──────────┘      └──────┘   └──────┘   └──────────┘   └───────┘   └───────────┘   └─────────┘
       ✔ v1.3          ✔                ✔ r17      ✔ r2       ✔ v0.2.0     ✔ 58 tarefas    ▶ AQUI          ↻ 27 ops     ↻ 47 tarefas    aguarda
                                      └──────────── o QUÊ ───────┘└──────── o COMO ────────┘└──── o CÓDIGO ─────┘
```

**Feature atual:** 000 Fundação. **Próxima etapa:** Fases 0 e 1 concluídas (T-001 a T-019, **Checkpoint 1 verde**: 481 testes,
regras de negócio provadas sem nenhum controller). Próxima é a Fase 2, começando pela T-020
(`POST /auth/signup`).

| Artefato | Arquivo | Responde | Status |
|---|---|---|---|
| Constitution | `specs/constitution.md` | Quais regras valem para tudo? | ✔ v1.3 |
| Roadmap | `specs/roadmap.md` | Em que ordem, e o que depende do quê? | ✔ 9 features |
| Spec | `specs/000-fundacao/spec.md` | **O quê** e **por quê**? | ✔ rev. 17, 12 histórias, 85 ACs, 0 dúvidas (`revisor-spec`: 3 rodadas + conferência, 31 dúvidas, todas respondidas) |
| Plan | `specs/000-fundacao/plan.md` | **Como**? | ✔ rev. 2, 42 decisões (D-01…D-42), aprovado |
| Contrato | `specs/000-fundacao/contracts/openapi.yaml` | Qual é o acordo exato entre front e back? | ✔ v0.2.0, 29 operações, validado com Redocly, 85/85 ACs rastreados |
| Tasks | `specs/000-fundacao/tasks.md` | Em que passos pequenos e verificáveis? | ✔ rev. 2, 58 tarefas em 7 fases, aprovado |
| Código + testes | `backend/`, `frontend/` | Funciona conforme a spec? | ▶ Fases 0 e 1 prontas (19/58 tarefas, 481 testes); Fase 2 a seguir |

## 2. O que cada etapa ensinou (com exemplos deste projeto)

**L1. A constitution evita rediscutir o básico.** Stack, papéis, LGPD e o formato de erro foram
decididos uma vez. Toda spec só *referencia* esses pontos (P3, P5, §5).

**L2. Specs pequenas, fatiadas por dependência.** O MVP tinha 7 áreas; virou 9 features com uma
fundação (000) primeiro. Nada de rotina existe sem idoso e membros.

**L3. A spec fala do quê, nunca do como.** "A sessão dura 7 dias sem uso" é spec. "Refresh token
de 7 dias em cookie HttpOnly" é plan. Por isso, quando você mudou a sessão para 7 dias, a spec mudou
primeiro; quando mudou para Gmail, só o plan mudou.

**L4. Escrever a spec revela as perguntas.** Surgiram 7 `[DÚVIDA]`s; a mais importante foi
"a qual idoso o cadastro se refere?". Cada uma custou minutos aqui e custaria dias no código.
Regra: a spec só avança com zero dúvidas.

**L5. Critérios de aceite testáveis e com ID estável.** `AC-006.7` entrou no fim da lista, sem
renumerar os outros, porque plan, contrato e testes apontam para os IDs.

**L6. Permissões em matriz, não em frases.** A tabela de visibilidade de endereço (AC-011.5)
expôs uma lacuna (DÚVIDA-7) que as frases escondiam. Cada célula vira um teste.

**L7. Uma boa decisão de modelagem apaga casos especiais.** Tratar o "Responsável" como um
*Familiar com permissão de admin*, e não como um terceiro papel, resolveu a DÚVIDA-7 sozinho e
simplificou banco, política e testes (D-29).

**L8. Mudança de princípio atualiza a constitution de forma explícita.** Um único Responsável e o
novo modelo de papéis viraram as versões v1.1 e v1.2, com histórico. Não fica uma spec
contradizendo a constitution em silêncio.

**L9. O dado vem da spec.** O endereço faltava no modelo porque nenhum AC pedia. O caminho foi
spec (AC-011), depois plan (D-25 a D-28), nunca direto no modelo.

**L10. Riscos do plan podem voltar para a spec.** R-01 (o Responsável perder o prazo sem push)
virou AC-006.7 (e-mail ao Responsável). Plan e spec conversam nos dois sentidos.

**L11. O contrato é o ponto de encontro e também revela lacunas.** Ao escrever o OpenAPI
apareceram decisões que o plan não tinha: upload de foto (D-30), convite repetido (D-31), 404 em
vez de 403 (D-32). Elas voltaram para o plan.

**L12. Rastreabilidade de ponta a ponta.** Cada operação do OpenAPI tem `x-acceptance-criteria`.
Uma checagem automática mostrou que só **AC-011.3 e AC-011.4** (preenchimento pelo CEP) não
aparecem no contrato, e está certo: são regras só do frontend. A cadeia é
**história → AC → decisão → operação → teste → código**.

**L13. Tarefas são pequenas, ordenadas e terminam num teste.** Cada item do `tasks.md` diz os
ACs que atende e o teste que prova que acabou (**"Pronto quando"**). A ordem vai do **domínio
sem HTTP** (Fase 1) para a API e só então para as telas: as regras mais importantes ficam
provadas antes de existir qualquer controller. Checkpoints entre as fases impedem avançar com
coisa quebrada.

**L14. Os artefatos do SDD são o que permite usar vários agentes.** Cada agente recebe um
documento e entrega algo verificável: `revisor-spec` (spec → dúvidas), `autor-testes` (ACs →
testes falhando), `implementadores` (testes + plan + contrato → código), `verificador`
(diff + spec → veredito por AC). Separar quem testa de quem implementa e de quem verifica evita o
"eu mesmo me aprovo". O contrato-primeiro permite backend e frontend em paralelo. O humano
continua dono da spec e dá a palavra final. Arquivos em `.claude/agents/` e regras em `CLAUDE.md`.

## 3. Quando e como usar cada agente

**Modelo mental:** você conversa com a **sessão principal** do Claude Code (a orquestradora). Ela
delega a um **subagente**, que trabalha num contexto próprio e limpo, só com as ferramentas do
arquivo dele em `.claude/agents/`, e devolve apenas o resultado.

### Como invocar
1. **Pedindo explicitamente** (preferível enquanto aprende): `Use o agente autor-testes para a T-011.`
2. **Mencionando com `@`:** `@agent-verificador revise a T-011`.
3. **Delegação automática:** a sessão principal escolhe o agente pelo campo `description`.
   Cômodo, mas esconde quem fez o quê.

`/agents` lista e edita os agentes do projeto.

### Quando invocar

| Momento no ciclo | Agente | Gatilho |
|---|---|---|
| Terminei ou revisei uma `spec.md` | `revisor-spec` | **Antes** do plan. Com dúvidas: você responde, a spec muda, roda de novo |
| Plan, contrato e tasks | nenhum | Decisão técnica com o humano; nada a delegar |
| Fase 0 (esqueleto) | nenhum | Passos sequenciais e dependentes |
| Início de cada tarefa (Fase 1+) | `autor-testes` | ACs da tarefa → testes que falham |
| Testes falhando **e revisados por você** | `implementador-backend` / `-frontend` | Implementar até ficar verde |
| Implementação terminada | `verificador` | **Antes** de marcar `[x]` no `tasks.md` |
| Veredito "devolver" | volta ao implementador | Com a lista do que falta |

### Uma tarefa de ponta a ponta (ex.: T-011 Endereço)

```
Você: "Use o autor-testes na T-011."          → tabela AC → teste; testes falhando ✔
Você: lê os testes. Cobrem AC-011.1 e 011.2?  (revisão humana)
Você: "Use o implementador-backend na T-011." → mvn verify verde
Você: "Use o verificador na T-011."           → "aprovada" ou "devolver: ..."
Você: marca [x] no tasks.md e faz o commit
```

**Paralelismo:** tarefas `[P]` da mesma fase são independentes:
`Rode o autor-testes em paralelo nas tarefas T-011, T-012, T-013, T-014 e T-015.`
Depois cada uma segue o próprio trio.

### Regra prática
Chame um agente quando as **duas** condições valerem:
1. existe um **documento de entrada pronto** (spec, tarefa, testes); sem ele, o agente adivinha;
2. há ganho em **independência de julgamento** (quem testa ≠ quem implementa ≠ quem verifica)
   ou em **paralelismo**.

Se é uma decisão que depende de você, ou um passo pequeno e sequencial, faça na sessão principal.

## 4. Próximos passos

1. ~~`tasks.md` da 000~~ ✔
2. **Scaffolding (Fase 0 do tasks):** `docker-compose.yml` (PostgreSQL + Mailpit), projeto Spring Boot 4.1 / Java
   25, projeto Angular 22 com PWA e o cliente gerado do contrato.
3. **Implementar tarefa por tarefa** com os testes primeiro, citando o AC.
4. **Validar:** todos os ACs com teste verde, contrato conferido, E2E no viewport de celular.
5. **Retrospectiva da 000** (o que o processo pegou e o que escapou) e depois spec **001 Medicamentos**.

## 5. Glossário rápido

- **AC (critério de aceite):** condição verificável no formato Dado/Quando/Então; vira teste.
- **D-xx (decisão):** escolha técnica registrada no plan, com o motivo.
- **R-xx (risco):** algo que pode dar errado, com a mitigação.
- **[DÚVIDA]:** pergunta de produto sem resposta; bloqueia o avanço da spec.
- **Contrato-primeiro:** a API é definida antes do código; front e back são construídos a partir dela.
- **Subagente:** assistente especializado com contexto e ferramentas próprios, chamado pela sessão principal.
- **Rastreabilidade:** conseguir ir de qualquer linha de código até a história de usuário que a justifica.
