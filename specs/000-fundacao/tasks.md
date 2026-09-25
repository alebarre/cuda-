# Tasks 000 — Fundação

- **Spec:** rev. 9 · **Plan:** D-01…D-32 · **Contrato:** `contracts/openapi.yaml` 0.1.0
- **Status:** pronto para implementação

## Como ler este arquivo

- Cada tarefa é **pequena** (algumas horas), tem **uma** entrega verificável e diz quais **ACs** e
  **decisões** atende.
- **Pronto quando:** o teste que prova que a tarefa acabou. **Escreva o teste primeiro**, veja-o
  falhar e então implemente (Constitution §6).
- `[P]` = pode ser feita em paralelo com as outras `[P]` da mesma fase.
- `Depende de:` só aparece quando a dependência não é óbvia pela ordem.
- Ao final de cada fase há um **checkpoint**: não avance com checkpoint vermelho.
- Se durante a implementação algo não couber na spec ou no plan, **pare e atualize o documento
  primeiro** (Constitution §7).

---

## Fase 0 — Esqueleto do projeto

- [ ] **T-001** `docker-compose.yml` na raiz com PostgreSQL 17 e Mailpit.
  *Pronto quando:* `docker compose up` sobe os dois e a UI do Mailpit abre em `localhost:8025`.
- [ ] **T-002** Backend Spring Boot 4.1 / Java 25 (Maven): Web, Data JPA, Security, Validation,
  Flyway, Mail, PostgreSQL. Profiles `dev`, `test`, `prod`; bean `Clock` (D-10); pacotes por
  domínio (D-01). *Pronto quando:* `mvn verify` passa com um teste de contexto.
- [ ] **T-003** Base de teste de integração com Testcontainers (PostgreSQL + Mailpit) reutilizável
  por todas as classes de teste. *Pronto quando:* um teste sobe os containers e roda uma migração vazia.
- [ ] **T-004** `[P]` Frontend Angular 22 standalone + signals, `@angular/pwa` só para o *app shell*
  (D-22), fonte base 16px e alvos ≥ 44px (P4). *Pronto quando:* `ng build` e `ng test` passam; o
  Lighthouse reconhece o app como instalável.
- [ ] **T-005** `[P]` Geração do cliente TypeScript a partir do `openapi.yaml` com script npm
  (D-19). *Pronto quando:* `npm run api:generate` gera o cliente sem erros e ele compila.
- [ ] **T-006** Teste de contrato: validar requisições e respostas dos testes de API contra o
  `openapi.yaml` (ex.: `swagger-request-validator` no MockMvc). *Pronto quando:* uma rota
  propositalmente fora do contrato faz o teste falhar.
- [ ] **T-007** Tratamento global de erros em Problem Details (RFC 9457) com `code` estável e sem
  dados de saúde (Constitution §5, P5). *Pronto quando:* teste de validação retorna
  `ValidationProblem` conforme o contrato.

**Checkpoint 0:** tudo compila, containers sobem, teste de contrato está ativo.

## Fase 1 — Domínio e dados (sem HTTP)

- [ ] **T-010** Migração `V1__foundation.sql` com todas as tabelas do plan §3, `user_id` UNIQUE
  (D-02), índice único parcial `WHERE is_admin` (D-03), `CHECK (NOT is_admin OR role='FAMILIAR')`
  (D-29), `emergency_contacts` (D-26). *Pronto quando:* testes de repositório provam que o banco
  **recusa** dois admins no mesmo grupo e um Cuidador admin.
- [ ] **T-011** `[P]` `Endereco` como `record` `@Embeddable` com validação (D-25).
  ACs: 011.1, 011.2. *Pronto quando:* testes cobrem CEP com 7/8/9 dígitos, UF inválida, número
  "S/N", complemento vazio.
- [ ] **T-012** `[P]` Entidade `Membership` com a máquina de estados (D-07): `confirmEmail`,
  `approve(role)`, `reject`, `expire`, `remove`, `changeRole`, com `Clock`.
  ACs: 001.2, 004.3, 006.3, 006.4, 006.5, 009.1, 009.2, 009.4.
  *Pronto quando:* um teste por transição válida **e** por transição inválida (ex.: aprovar
  expirado, remover admin, tornar admin um Cuidador).
- [ ] **T-013** `[P]` Serviço de OTP: gera 6 dígitos, guarda hash (D-04), 15 min, 5 tentativas,
  reenvio 1/min. ACs: 005.1, 005.2, 005.3. *Pronto quando:* testes com `Clock` fixo cobrem
  expiração no minuto 15, 5ª tentativa errada e reenvio antes de 60 s.
- [ ] **T-014** `[P]` Entidade `Invitation`: código hash, 7 dias, 5 tentativas, uso único,
  reenvio invalida o anterior, convite repetido substitui o `ENVIADO` (D-31).
  ACs: 003.2, 003.3, 003.4. *Pronto quando:* testes de cada transição de `InvitationStatus`.
- [ ] **T-015** `[P]` `AddressVisibilityPolicy` (D-28). AC: 011.5.
  *Pronto quando:* `@ParameterizedTest` com **cada célula** da matriz da spec.
- [ ] **T-016** Infra de e-mail: eventos de domínio + `@TransactionalEventListener(AFTER_COMMIT)`
  assíncrono (D-23); templates pt-BR sem dados de saúde (P5); profile `dev`→Mailpit, `prod`→Gmail
  via variáveis de ambiente (D-24). *Pronto quando:* teste prova que (a) o e-mail chega no Mailpit
  após o commit e (b) **nenhum** e-mail sai se a transação faz rollback.

**Checkpoint 1:** todas as regras de negócio testadas sem nenhum controller.

## Fase 2 — API de autenticação

- [ ] **T-020** `POST /auth/signup` (resposta neutra 202, D-15). ACs: 001.1, 001.3, 001.4.
  *Pronto quando:* testes para cadastro novo, e-mail repetido (mesma resposta, nada criado),
  senha fraca e endereço inválido.
- [ ] **T-021** JWT de 15 min + refresh token opaco de 7 dias com rotação e detecção de reuso;
  cookie `HttpOnly; Secure; SameSite=Strict` (D-11, D-12). AC: 008.3.
  *Pronto quando:* testes de renovação, expiração após 7 dias sem uso e revogação da família no reuso.
- [ ] **T-022** `POST /auth/verify-email` abre sessão e cria o grupo + membership admin (D-05).
  ACs: 001.2, 005.1, 005.2. Depende de: T-021.
- [ ] **T-023** `POST /auth/login` + bloqueio 5 erros / 15 min + respostas por status (D-14).
  ACs: 008.1, 008.2, 008.4. *Pronto quando:* um teste para cada `code` de
  `MembershipBlockedProblem` e para o 423.
- [ ] **T-024** Filtro que confere `membership ATIVO` a cada requisição (D-13). AC: 009.2.
  *Pronto quando:* token válido de membro removido recebe 401 na requisição seguinte.
- [ ] **T-025** `[P]` `POST /auth/refresh` e `POST /auth/logout`.
- [ ] **T-026** `[P]` `POST /auth/resend-code` e redefinição de senha (`request`/`confirm`),
  encerrando as sessões. ACs: 005.3, 010.1, 010.2.

**Checkpoint 2:** criador consegue se cadastrar, confirmar e entrar; contrato verde.

## Fase 3 — Idoso e dados pessoais

- [ ] **T-030** `POST/GET/PUT /elders` com contatos de emergência; 409 se o grupo já tem idoso;
  404 para outro grupo (D-32). ACs: 002.1, 002.2, 002.3, 002.5, 011.7.
- [ ] **T-031** `[P]` `PUT/DELETE /elders/{id}/photo`, JPEG/PNG até 5 MB (D-30). AC: 002.2.
- [ ] **T-032** `[P]` `GET/PUT /me`. AC: 011.6.

## Fase 4 — Convites e aprovação

- [ ] **T-040** Convites do Responsável: criar, listar, reenviar, cancelar; bloqueado sem idoso;
  409 se já é membro ativo. ACs: 002.4, 003.1, 003.4, 003.5.
- [ ] **T-041** `POST /invitations/redeem` (token de resgate de 15 min, D-16) + rate limit por IP
  (R-03). ACs: 003.2, 003.3, 004.1, 004.5.
- [ ] **T-042** `POST /invitations/register` cria usuário + membership `AGUARDANDO_APROVACAO` com
  prazo de 24 h e dispara o e-mail ao Responsável. ACs: 004.2, 004.3, 004.4, 006.7.
- [ ] **T-043** `GET /members` com filtro de status e endereço conforme a política; pendentes só
  para o admin. ACs: 006.2, 011.5. *Pronto quando:* teste de API da matriz para Cuidador e Familiar.
- [ ] **T-044** `[P]` `GET /members/pending-count`. AC: 006.1.
- [ ] **T-045** `approve` / `reject` com lock otimista (`version`, D-08) e e-mail do resultado.
  ACs: 006.3, 006.4, 006.6, 007.1, 007.2. *Pronto quando:* teste com duas decisões simultâneas
  prova que só a primeira vale (a outra recebe 409).
- [ ] **T-046** Job de expiração a cada 5 min + checagem na leitura (D-09). ACs: 006.5, 007.1.
  *Pronto quando:* com `Clock` avançado 24 h, aprovar retorna 410 **mesmo antes** do job rodar,
  e depois do job o status é `EXPIRADO` e o e-mail foi enviado.
- [ ] **T-047** `PATCH` (papel) e `DELETE` de membro; admin intocável. ACs: 009.1–009.4.

**Checkpoint 4:** backend completo; todo AC de backend tem teste verde; contrato 100% coberto.

## Fase 5 — Frontend (mobile-first)

- [ ] **T-050** Core: interceptor que injeta o access token e renova via `/auth/refresh` no 401;
  token só em memória (D-12); guards por status e `isAdmin` (D-20).
- [ ] **T-051** `[P]` `<app-endereco-form>` com ViaCEP (timeout 3 s) e máscara de CEP (D-27).
  ACs: 011.3, 011.4. *Pronto quando:* testes de componente com ViaCEP simulado para sucesso,
  `{"erro": true}` e timeout.
- [ ] **T-052** Criar grupo + verificar e-mail + reenvio. ACs: 001.1–001.3, 005.
- [ ] **T-053** Login com as mensagens de cada status e bloqueio. ACs: 008.1, 008.2, 008.4.
- [ ] **T-054** "Tenho um código de convite" → cadastro com e-mail travado → tela de conclusão com o
  aviso de 24 h. ACs: 004.1, 004.2, 004.4, 004.5.
- [ ] **T-055** `[P]` Esqueci a senha. AC: 010.
- [ ] **T-056** Cadastro/edição/visualização do idoso, contatos e foto. ACs: 002.
- [ ] **T-057** Convites (lista, novo, reenviar, cancelar). AC: 003.4.
- [ ] **T-058** Pedidos pendentes com badge (polling de 60 s em primeiro plano, D-17, D-21) e
  aprovação escolhendo o papel. ACs: 006.1, 006.2, 006.3, 006.4.
- [ ] **T-059** `[P]` Lista de membros (endereço só quando vier da API), alterar papel, remover.
  ACs: 009, 011.5.
- [ ] **T-060** `[P]` Meus dados (telefone e endereço). AC: 011.6.

## Fase 6 — Validação da feature

- [ ] **T-070** E2E Playwright em viewport de celular: criar grupo → cadastrar idoso → convidar →
  resgatar código (e-mail lido no Mailpit) → cadastrar → aprovar → convidado entra.
- [ ] **T-071** Script de rastreabilidade: lista todo `AC-xxx.y` da spec e falha se algum não
  aparecer em nenhum teste. *Pronto quando:* roda no `mvn verify`/CI e está verde.
- [ ] **T-072** Revisão de acessibilidade (P1: ações frequentes em ≤ 3 toques; P4: contraste AA,
  alvos ≥ 44px) nas telas da 000.
- [ ] **T-073** Retrospectiva: o que a spec previu, o que escapou, o que mudou nos documentos
  durante a implementação. Registrar em `JORNADA-SDD.md`.

**Checkpoint final (Definition of Done da feature):** 48 ACs com teste verde, contrato conferido,
E2E verde, nenhuma `[DÚVIDA]` nova sem resposta, documentos coerentes com o código.
