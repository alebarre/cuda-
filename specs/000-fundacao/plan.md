# Plan 000 — Fundação

- **Spec:** `spec.md` rev. 9
- **Constitution:** v1.2
- **Status:** rascunho para revisão

> Aqui entram as decisões técnicas. Cada decisão importante tem um ID (`D-xx`) e indica
> quais critérios de aceite (`AC-xxx`) atende.

## 1. Versões fixadas (vale para o projeto inteiro)

| Item | Versão |
|---|---|
| Java | 25 (LTS) |
| Spring Boot | 4.1.x |
| PostgreSQL | 17 (imagem Docker) |
| Angular | 22.x |
| Node | LTS atual exigida pelo Angular 22 |
| Build backend | Maven |
| E-mail real | **Gmail SMTP** (`smtp.gmail.com:587`, STARTTLS) com senha de app |
| E-mail em dev/testes | Mailpit (Docker): captura os e-mails, nada sai para a internet |

## 2. Estrutura do backend (pacotes por domínio)

```
backend/src/main/java/.../cuida/
├── shared/        (erros Problem Details, relógio, config)
├── auth/          (login, tokens, OTP, redefinição de senha)
├── group/         (grupo de cuidado, idoso)
├── membership/    (membros, convites, aprovação)
└── notification/  (envio de e-mails)
```

**D-01** Organização por domínio (e não por camada `controller/service/repository`), para que cada
feature futura (001–008) seja um pacote novo e isolado.

## 3. Modelo de dados

```
users            (id, name, email UNIQUE, phone, <endereço>, password_hash,
                  email_verified_at, failed_logins, locked_until, created_at)
care_groups      (id, created_by → users, created_at)
elders           (id, group_id UNIQUE → care_groups, name, birth_date, <endereço>,
                  photo_url, allergies, conditions, updated_at)
emergency_contacts (id, elder_id → elders, name, relationship, phone, <endereço>,
                  position)
memberships      (id, group_id → care_groups, user_id UNIQUE → users, role, is_admin, status,
                  requested_at, expires_at, decided_by → users, decided_at, version)
invitations      (id, group_id, email, code_hash, status, attempts, expires_at,
                  created_by, created_at, used_at)
otp_codes        (id, user_id, purpose, code_hash, attempts, expires_at, consumed_at,
                  last_sent_at)
refresh_tokens   (id, user_id, token_hash, expires_at, revoked_at, created_at)

<endereço> = address_zip_code CHAR(8), address_street, address_number, address_complement NULL,
             address_district, address_city, address_state CHAR(2)
```

- **D-02** `memberships.user_id` é UNIQUE: uma pessoa participa de **um único grupo** (spec §5).
- **D-29** `role` tem só dois valores (`CUIDADOR`, `FAMILIAR`) e o Responsável é
  `is_admin = true`. Uma `CHECK (NOT is_admin OR role = 'FAMILIAR')` impede Cuidador administrador.
  Assim, "Familiar" nas regras de permissão já inclui o Responsável sem caso especial.
- **D-03** Índice único parcial `memberships(group_id) WHERE is_admin`: o banco garante
  **exatamente um Responsável** por grupo (AC-002.1, AC-009.4).
- **D-04** Códigos (OTP e convite) são guardados **somente como hash** (P5). O código em texto
  existe apenas no e-mail.
- **D-05** A pessoa que criou o grupo também tem uma linha em `memberships` com papel
  `FAMILIAR` e `is_admin = true`, então as permissões são checadas de um só jeito para todos.
- **D-06** Migrações com Flyway: `V1__foundation.sql`.
- **D-25** `Endereco` é um **objeto de valor** (`@Embeddable`, imutável, um `record` Java) em
  `shared/`, embutido em `User`, `Elder` e `EmergencyContact` com colunas prefixadas `address_`.
  Não é uma tabela própria porque um endereço não tem identidade nem vida fora da pessoa; a
  validação (AC-011.1, AC-011.2) fica **num lugar só**, no construtor do `record` e em
  anotações Bean Validation reaproveitadas pelo DTO. As features futuras (ex.: 007 Visitas,
  endereço do profissional) reutilizam o mesmo tipo.
- **D-26** Contatos de emergência saem do JSONB e viram **tabela própria**
  (`emergency_contacts`): agora têm estrutura e validação obrigatória (AC-002.5), e JSONB
  esconderia isso do banco.

## 4. Máquinas de estado

**Membership**

```
(criador)   AGUARDANDO_CONFIRMACAO_EMAIL ──código ok──► ATIVO (FAMILIAR, admin)
(convidado) AGUARDANDO_APROVACAO ──aprovar──► ATIVO (CUIDADOR | FAMILIAR)
                                 ──recusar──► RECUSADO
                                 ──24h─────► EXPIRADO
            ATIVO ──remover──► REMOVIDO   (proibido se admin)
```

**Invitation:** `ENVIADO → USADO | EXPIRADO | CANCELADO`

- **D-07** Transições ficam em métodos da entidade (`membership.approve(role, by, now)`), que
  lançam exceção em transição inválida. Assim a regra é testável sem banco (AC-006, AC-009).
- **D-08** A concorrência entre duas ações sobre o mesmo pedido é tratada com **lock otimista**
  (`version`): a segunda recebe `409 Conflict`.
- **D-09** A expiração é verificada **na leitura** (um pedido com `expires_at` vencido nunca é
  aprovável) e um **job agendado a cada 5 minutos** muda o status e envia o e-mail de expiração
  (AC-006.5, AC-007.1). Assim a regra não depende do job rodar na hora exata.
- **D-10** Um `Clock` injetável em todo lugar que usa hora, para testar prazos (15 min, 24 h,
  7 dias) sem esperar.

## 5. Autenticação e sessão

- **D-11** **Access token JWT** de 15 minutos + **refresh token** opaco de **7 dias**, **renovado a
  cada uso** (sessão de até 7 dias sem uso, AC-008.3). O refresh token fica guardado como hash e é
  **rotacionado**: um token antigo reapresentado revoga a família inteira.
- **D-12** Refresh token em cookie `HttpOnly; Secure; SameSite=Strict`; access token só em
  memória no Angular. Protege contra roubo de token por XSS.
- **D-13** Remover um membro revoga todos os refresh tokens dele. O access token já emitido
  morre em até 15 minutos. Para cumprir o "imediatamente" de AC-009.2, o filtro de segurança
  também confere se a membership continua `ATIVO` a cada requisição (uma consulta por chave
  primária, que pode ir para cache depois).
- **D-14** Senha com BCrypt. Bloqueio após 5 erros por 15 minutos (AC-008.4).
- **D-15** Respostas "neutras" em cadastro e redefinição de senha: mesmo status e mesma
  mensagem, exista o e-mail ou não (AC-001.4, AC-010.2).

## 6. API (resumo; o detalhe fica em `contracts/openapi.yaml`)

| Método | Rota | Quem | ACs |
|---|---|---|---|
| POST | `/api/v1/auth/signup` | público (cria grupo) | 001.1, 001.3, 001.4 |
| POST | `/api/v1/auth/verify-email` | público | 001.2, 005 |
| POST | `/api/v1/auth/resend-code` | público | 005.3 |
| POST | `/api/v1/auth/login` | público | 008 |
| POST | `/api/v1/auth/refresh` | cookie | 008.3 |
| POST | `/api/v1/auth/logout` | autenticado | — |
| POST | `/api/v1/auth/password-reset/request` | público | 010 |
| POST | `/api/v1/auth/password-reset/confirm` | público | 010 |
| POST | `/api/v1/invitations/redeem` | público (e-mail + código) | 003.2, 003.3, 004.1, 004.5 |
| POST | `/api/v1/invitations/register` | público (token de resgate) | 004.2–004.4, 006.7 |
| GET/PUT | `/api/v1/me` (telefone e endereço) | autenticado | 011.6 |
| GET/PUT | `/api/v1/elders/{elderId}` | membro / Responsável | 002 |
| PUT/DELETE | `/api/v1/elders/{elderId}/photo` | Responsável | 002.2 |
| POST | `/api/v1/elders` | Responsável sem idoso | 002.2 |
| GET/POST | `/api/v1/elders/{elderId}/invitations` | Responsável | 003.1, 003.4, 003.5 |
| POST | `/api/v1/elders/{elderId}/invitations/{id}/resend` \| `/cancel` | Responsável | 003.4 |
| GET | `/api/v1/elders/{elderId}/members?status=` | membro | 006.2 |
| GET | `/api/v1/elders/{elderId}/members/pending-count` | Responsável | 006.1 |
| POST | `/api/v1/elders/{elderId}/members/{id}/approve` \| `/reject` | Responsável | 006.3, 006.4, 006.6 |
| PATCH | `/api/v1/elders/{elderId}/members/{id}` (papel) | Responsável | 009.1, 009.4 |
| DELETE | `/api/v1/elders/{elderId}/members/{id}` | Responsável | 009.2, 009.4 |

- **D-16** O resgate do convite é feito em **dois passos**: `redeem` valida e-mail + código e
  devolve um **token de resgate** de curta duração (15 min); `register` usa esse token. Assim o
  código de 6 dígitos não trafega de novo e as tentativas contam num lugar só.
- **D-17** "Notificação no app" (AC-006.1) no MVP = o Angular consulta `pending-count` ao abrir e
  a cada 60 segundos com o app em primeiro plano. Push fica fora do MVP (Constitution §8).
- **D-30** *(descoberta no contrato)* A foto do idoso tem endpoint próprio `multipart`
  (`PUT /elders/{elderId}/photo`, JPEG/PNG até 5 MB), separado do JSON do idoso. No MVP o arquivo
  fica em disco/volume local; trocar por armazenamento de objetos depois não muda o contrato.
- **D-31** *(descoberta no contrato)* Convidar de novo um e-mail com convite `ENVIADO` cancela o
  anterior e cria outro, em vez de dar erro.
- **D-32** *(descoberta no contrato)* Recurso de **outro grupo** responde `404`, não `403`: não
  revela que o recurso existe (P5).
- **D-18** Rotas sob `/elders/{elderId}` seguem a Constitution §5, mesmo com um idoso por grupo:
  as features 001–008 já nascem no padrão certo.

## 7. Frontend (Angular 22)

```
frontend/src/app/
├── core/       (interceptor de auth, guards, cliente gerado do OpenAPI)
├── auth/       login, criar grupo, verificar e-mail, tenho um código, cadastro por convite,
│               aguardando aprovação, esqueci a senha
├── elder/      cadastro/edição do idoso, visualização
└── members/    convites, pedidos pendentes (com badge), lista de membros
```

- **D-19** Cliente TypeScript gerado do `openapi.yaml` (`openapi-generator`, gerador
  `typescript-angular`). Nada de digitar DTO na mão (Constitution §4).
- **D-20** Guards por papel e status: `ATIVO` para entrar na área logada, `isAdmin` para
  convites e aprovação. O backend **sempre** revalida; o guard é só experiência de uso.
- **D-21** O badge de pendentes fica num `signal` alimentado pelo polling de D-17.
- **D-22** PWA com `@angular/pwa` apenas para instalação e cache do *app shell*; nenhuma chamada
  à API vai para cache (online-only, Constitution §4).
- **D-27** Um único componente `<app-endereco-form>` (formulário reativo), usado em todos os
  cadastros. Ele consulta o **ViaCEP** (`https://viacep.com.br/ws/{cep}/json/`) **direto do
  navegador**, com timeout de 3 s; em erro ou `{"erro": true}` mostra o aviso de AC-011.4 e libera
  a digitação. O backend **não** depende do ViaCEP: só valida o formato, então o cadastro funciona
  mesmo com o serviço fora do ar.
- **D-28** A regra de AC-011.5 fica numa **única** classe de política no backend,
  `AddressVisibilityPolicy.canSee(viewer, target)` (hoje: é o próprio **ou** o viewer é
  `FAMILIAR`), testada célula
  por célula (teste parametrizado com a matriz da spec). O mapeamento para DTO consulta a política
  e **omite** o campo `address` quando o resultado é falso. O Angular apenas exibe o que vier: a
  regra nunca é reimplementada no frontend.

## 8. Estratégia de testes (ligação AC → teste)

| Camada | Ferramenta | Cobre |
|---|---|---|
| Domínio | JUnit 5 + AssertJ, `Clock` fixo | transições e prazos: 005, 006, 009.4; matriz 011.5 (`@ParameterizedTest`) |
| API | `@SpringBootTest` + Testcontainers (PostgreSQL) + MockMvc | todos os ACs de endpoint e permissão |
| Contrato | validação das respostas contra o `openapi.yaml` | conformidade do contrato |
| E-mail | Mailpit via Testcontainers | 003.1, 006.7, 007.1 |
| Frontend | testes de componente | formulários, mensagens de 004.4 e 008.2, CEP (011.3, 011.4 com ViaCEP simulado) |
| E2E | Playwright em viewport de celular | fluxo completo: criar grupo → convidar → cadastrar → aprovar → entrar |

Cada teste traz o ID do critério no nome ou comentário, por exemplo
`void ac_006_5_pedido_expira_apos_24h()`.

## 9. Riscos e pontos de atenção

- **R-01** ~~Sem push, o Responsável pode perder o prazo de 24 h.~~ **Resolvido:** spec rev. 5
  adicionou AC-006.7 (e-mail ao Responsável).
- **D-23** Os e-mails são enviados **depois do commit** da transação
  (`@TransactionalEventListener(phase = AFTER_COMMIT)`) e de forma assíncrona: uma falha no SMTP
  não desfaz o cadastro, e nunca sai e-mail de algo que não foi gravado.
- **D-24** Envio de e-mail pelo **Gmail** via SMTP (`spring-boot-starter-mail`). Exige verificação
  em duas etapas na conta Google e uma **senha de app**; usuário e senha vêm de variáveis de
  ambiente (`MAIL_USERNAME`, `MAIL_PASSWORD`) e **nunca** vão para o repositório. O profile `dev`
  aponta para o Mailpit, o profile `prod` para o Gmail; o código não muda, só a configuração.
- **R-02** Limites do Gmail: uma conta pessoal tem cota diária de envio (na casa de centenas de
  mensagens) e envios em volume podem cair em spam ou bloquear a conta. É suficiente para o MVP e
  para um grupo familiar; se crescer, trocar por um provedor transacional muda só a configuração
  SMTP.
- **R-03** O código de convite de 6 dígitos vale por 7 dias; a proteção contra força bruta é o
  limite de 5 tentativas por e-mail (AC-003.3) somado a um rate limit por IP em `redeem`.
