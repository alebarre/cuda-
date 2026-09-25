# Spec 000 — Fundação: grupo de cuidado, convites, aprovação e acesso

- **Status:** pronta para revisão final (sem `[DÚVIDA]` em aberto)
- **Constitution:** v1.2
- **Última revisão:** 2026-09-25 (rev. 9: Responsável = Familiar administrador)

## 1. Contexto e objetivo

Antes de qualquer registro de rotina, o app precisa saber **quem é quem** e **quem pode ver os
dados de qual idoso**. Como são dados de saúde (Constitution P5), o acesso é **sempre por convite**
e depende da **aprovação do Responsável**.

Fluxo desejado:

1. Uma pessoa **cria um grupo de cuidado**, vira o **Responsável** e cadastra o idoso.
2. O Responsável **convida por e-mail** quem vai fazer parte do plantão. O convidado recebe um
   **código de convite (OTP)**, válido por **7 dias**.
3. Na **tela de login**, o convidado escolhe "Tenho um código de convite", digita e-mail e código
   e completa o cadastro. Fica automaticamente vinculado àquele idoso.
4. O pedido fica **aguardando aprovação por até 24 horas**; o Responsável é notificado no app e
   por e-mail.
5. O Responsável **aprova** (escolhendo Cuidador ou Familiar) ou **recusa**. A pessoa é avisada
   por e-mail.

## 2. Atores

- **Visitante:** ainda não tem conta.
- **Convidado:** recebeu um código de convite, mas ainda não se cadastrou.
- **Pendente:** cadastrou-se com o código; aguarda aprovação.
- **Cuidadores** e **Familiares** (vários).
- **Responsável:** o **Familiar administrador** do grupo; exatamente um, que é quem criou o grupo
  (Constitution §3).

## 3. Histórias de usuário

- **US-001** Como visitante, quero criar um grupo de cuidado para organizar a rotina de um idoso
  da minha família.
- **US-002** Como Responsável, quero cadastrar os dados básicos do idoso.
- **US-003** Como Responsável, quero convidar pessoas por e-mail para que elas façam parte do
  grupo de cuidado daquele idoso.
- **US-004** Como convidado, quero entrar com o código que recebi por e-mail e me cadastrar, sem
  precisar informar a qual idoso me refiro.
- **US-005** Como Responsável, quero ser avisado no app de que há pedidos pendentes e aprová-los
  (escolhendo o papel) ou recusá-los.
- **US-006** Como pessoa que pediu acesso, quero saber o resultado: aprovação, recusa ou expiração.
- **US-007** Como membro aprovado, quero entrar com e-mail e senha e continuar logado no celular.
- **US-008** Como Responsável, quero remover o acesso de um membro sem apagar o histórico dele.
- **US-009** Como usuário, quero redefinir minha senha se a esquecer.
- **US-010** Como membro, quero informar e manter atualizados meu telefone e meu endereço.
- **US-011** Como qualquer pessoa que preenche um endereço, quero digitar só o CEP e ter o resto
  preenchido, porque digitar endereço no celular é trabalhoso.

## 4. Critérios de aceite

### AC-001 Criação do grupo de cuidado
- **AC-001.1** Dado um visitante, quando ele informa nome, e-mail, telefone, **endereço** (AC-011) e senha válidos na
  opção "Criar grupo de cuidado", então a conta é criada com status `AGUARDANDO_CONFIRMACAO_EMAIL`
  e um código é enviado ao e-mail.
- **AC-001.2** Quando o criador confirma o e-mail (regras de AC-005), ele se torna `ATIVO` como
  **Responsável** do novo grupo e é levado ao cadastro do idoso (AC-002). Não passa por aprovação.
- **AC-001.3** A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número.
- **AC-001.4** Dado um e-mail já cadastrado, quando alguém tenta criar conta com ele, então a
  resposta **não confirma** se o e-mail existe (P5).

### AC-002 Idoso
- **AC-002.1** Cada grupo de cuidado tem **exatamente um idoso** e **exatamente um Responsável**.
- **AC-002.2** O Responsável cadastra do idoso: nome, data de nascimento e **endereço** (AC-011),
  obrigatórios; e, opcionalmente, foto, alergias, condições de saúde e contatos de emergência.
- **AC-002.5** Cada contato de emergência tem nome, parentesco/relação, telefone e **endereço**
  (AC-011), todos obrigatórios. O idoso pode ter zero ou mais contatos.
- **AC-002.3** Todos os membros ativos veem esses dados; só o Responsável edita.
- **AC-002.4** Enquanto o idoso não for cadastrado, o Responsável não consegue enviar convites.

### AC-003 Convite
- **AC-003.1** Quando o Responsável informa o e-mail de uma pessoa, então ela recebe um e-mail com
  um **código de convite de 6 dígitos**, o nome do idoso e as instruções para usá-lo na tela de login.
- **AC-003.2** O código é de **uso único**, vale **somente para o e-mail convidado** e **expira em
  7 dias**.
- **AC-003.3** Depois de 5 tentativas erradas para aquele e-mail, o código é invalidado e o
  Responsável precisa reenviar o convite.
- **AC-003.4** O Responsável vê a lista de convites com status (`ENVIADO`, `USADO`, `EXPIRADO`,
  `CANCELADO`) e pode **cancelar** ou **reenviar** um convite; o reenvio gera um código novo e
  invalida o anterior.
- **AC-003.5** Não é possível convidar um e-mail que já é membro ativo do grupo.

### AC-004 Cadastro com o código de convite
- **AC-004.1** Na tela de login há a opção **"Tenho um código de convite"**, que pede e-mail e código.
- **AC-004.2** Com e-mail e código válidos, o convidado vê o formulário de cadastro com o **e-mail
  já preenchido e não editável** e o nome do idoso; ele informa nome, telefone, **endereço** (AC-011) e senha
  (AC-001.3).
- **AC-004.3** Ao concluir, o vínculo com o grupo é criado **automaticamente** e o status passa
  direto para `AGUARDANDO_APROVACAO` (o código já comprovou o e-mail; não há confirmação extra).
  O prazo de 24 horas começa a contar.
- **AC-004.4** A tela de conclusão avisa: **"Seu pedido de acesso será analisado pelo responsável
  em até 24 horas. Depois desse prazo ele expira e será preciso pedir um novo convite."**
- **AC-004.5** Com um código expirado, cancelado ou já usado, a pessoa vê uma mensagem explicando
  o motivo e orientando a pedir um novo convite.

### AC-005 Confirmação de e-mail (criador do grupo e redefinição de senha)
- **AC-005.1** O código tem 6 dígitos e **expira em 15 minutos**.
- **AC-005.2** Depois de 5 tentativas erradas, o código é invalidado e é preciso pedir um novo.
- **AC-005.3** O reenvio do código é permitido no máximo 1 vez por minuto.

### AC-006 Aprovação
- **AC-006.1** Ao abrir o app, o Responsável vê um indicador com a quantidade de pedidos pendentes.
- **AC-006.2** A lista de pedidos mostra nome, e-mail, telefone, data do pedido e tempo restante
  até a expiração.
- **AC-006.3** Para aprovar, o Responsável **deve** escolher o papel (**Cuidador** ou **Familiar**);
  o status passa para `ATIVO`.
- **AC-006.4** Ao recusar, o status passa para `RECUSADO`.
- **AC-006.5** Se o Responsável não decidir em **24 horas**, o status passa para `EXPIRADO`.
- **AC-006.6** Cuidadores e Familiares não podem aprovar nem recusar.
- **AC-006.7** Quando um pedido entra em `AGUARDANDO_APROVACAO`, o Responsável recebe um
  **e-mail** com o nome da pessoa e o prazo para decidir. O e-mail **não** contém dados de saúde
  do idoso (P5).

### AC-007 Aviso do resultado
- **AC-007.1** A pessoa recebe um **e-mail** quando o pedido é **aprovado**, **recusado** ou
  **expira**.
- **AC-007.2** O e-mail de recusa ou expiração orienta a pedir um novo convite ao Responsável; a
  pessoa pode ser convidada novamente.

### AC-008 Login e sessão
- **AC-008.1** Só usuários `ATIVO` conseguem entrar.
- **AC-008.2** Um usuário que tenta entrar e está `AGUARDANDO_APROVACAO` vê
  "Seu cadastro está aguardando aprovação do responsável"; `RECUSADO` ou `EXPIRADO` vê orientação
  para pedir um novo convite.
- **AC-008.3** A sessão permanece ativa no celular por até **7 dias sem uso**; depois disso é
  preciso entrar de novo com e-mail e senha.
- **AC-008.4** Depois de 5 senhas erradas seguidas, o login fica bloqueado por 15 minutos.

### AC-009 Gestão de membros
- **AC-009.1** O Responsável pode alterar o papel de um membro entre Cuidador e Familiar.
- **AC-009.2** Ao remover um membro, o status passa para `REMOVIDO` e todas as sessões dele são
  encerradas imediatamente.
- **AC-009.3** Os registros de cuidado feitos por um membro removido continuam visíveis, com o nome
  dele (P3).
- **AC-009.4** O Responsável não pode ser removido nem ter o papel alterado; nenhum membro pode
  ser promovido a Responsável.

### AC-010 Redefinição de senha
- **AC-010.1** O usuário informa o e-mail e recebe um código com as regras de AC-005.
- **AC-010.2** A resposta é a mesma quer o e-mail exista ou não (P5).

### AC-011 Endereço (regra única para idoso, contatos de emergência e membros)
- **AC-011.1** Campos: CEP, logradouro, número, complemento, bairro, cidade e UF. **Todos
  obrigatórios, exceto complemento.** Número aceita "S/N".
- **AC-011.2** CEP com 8 dígitos (a máscara `00000-000` é só visual); UF deve ser uma das 27
  unidades da federação.
- **AC-011.3** Ao digitar um CEP válido, logradouro, bairro, cidade e UF são **preenchidos
  automaticamente** e continuam **editáveis**.
- **AC-011.4** Se o CEP não for encontrado ou o serviço de consulta estiver fora do ar, a pessoa vê
  um aviso e **preenche manualmente**; o cadastro nunca fica bloqueado por isso.
- **AC-011.5** Visibilidade do endereço de **membros** (P5). Cada célula vira um teste:

  | Quem vê ↓ / endereço de → | si mesmo | Cuidadores | Familiares (inclui o Responsável) |
  |---|---|---|---|
  | **Cuidador** | sim | **não** | não |
  | **Familiar** (inclui o Responsável) | sim | sim | sim |

  Como o Responsável é um Familiar, a regra fica: **Cuidador vê só o próprio endereço;
  Familiar vê o de todos.**

  Quando não pode ver, o endereço simplesmente **não aparece**; nome e papel continuam visíveis.
- **AC-011.7** O endereço do **idoso** e dos **contatos de emergência** é visível para todos os
  membros ativos, inclusive Cuidadores.
- **AC-011.6** O membro pode alterar o próprio telefone e endereço a qualquer momento.

## 5. Fora de escopo

Login social, autenticação em dois fatores, login sem senha (somente OTP), SMS/WhatsApp,
notificação push (o Responsável é avisado no app e por e-mail), uma pessoa participar de
mais de um grupo de cuidado, mais de um idoso por grupo, mais de um Responsável por grupo, transferência do papel de Responsável,
recuperação do grupo caso o Responsável perca o acesso definitivamente (suporte manual).

## 6. Dúvidas

### Resolvidas
- **DÚVIDA-1** Vínculo com o idoso → pelo **convite enviado por e-mail** pelo Responsável (rev. 2).
- **DÚVIDA-2** Primeiro Responsável → fluxo **"Criar grupo de cuidado"** (rev. 2).
- **DÚVIDA-3** Vários Responsáveis → rev. 2 decidiu "sim"; **rev. 3 mudou para um único
  Responsável**; **rev. 4 descartou a transferência do papel** (AC-009.4).
- **DÚVIDA-4** Recusa → a pessoa **é avisada** (por e-mail) (rev. 2).
- **DÚVIDA-5** Pedido pendente → **expira em 24 horas**; aviso exibido no ato do cadastro (rev. 2).
- **DÚVIDA-6** Convite → **código OTP de 6 dígitos por e-mail**, digitado na tela de login,
  **válido por 7 dias**; substitui o link e a confirmação de e-mail do convidado (rev. 3).
- **R-01 do plan** Responsável sem push pode perder o prazo de 24 h → **também recebe e-mail**
  quando chega um pedido (AC-006.7, rev. 5).
- **Sessão** 30 dias sem uso → **7 dias** (AC-008.3, rev. 6).
- **Endereço** → obrigatório para idoso, contatos de emergência e membros; preenchimento por CEP
  (AC-011, rev. 7).
- **Visibilidade do endereço de membros** → Cuidador vê só o próprio; Familiar vê o próprio, dos
  Cuidadores e dos Familiares; Responsável vê todos (AC-011.5, rev. 8).

- **DÚVIDA-7** Familiar vê o endereço do Responsável? → **sim**: o Responsável passa a ser um
  **Familiar com permissão de administrador**, e não um terceiro papel (rev. 9, Constitution v1.2).
