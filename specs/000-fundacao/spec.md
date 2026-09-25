# Spec 000 — Fundação: grupo de cuidado, convites, aprovação e acesso

- **Status:** **aprovada para o plan** (sem `[DÚVIDA]` em aberto; três rodadas do `revisor-spec`
  mais uma conferência). Plan, contrato e tasks precisam ser revisados a partir desta rev.
  (Constitution §7).
- **Constitution:** v1.3
- **Última revisão:** 2026-09-25 (rev. 17: DÚVIDA-38 fechada; rev. 15: bloqueio do código de
  convite por tempo, com reenvio automático; sai o status `BLOQUEADO`)

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

Só o Responsável cria grupo e convida; Cuidadores e Familiares entram **apenas por convite**.

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
- **US-012** Como Responsável, quero excluir o grupo de cuidado quando ele não for mais necessário,
  e que todas as pessoas envolvidas sejam avisadas.

## 4. Critérios de aceite

### AC-001 Criação do grupo de cuidado
- **AC-001.1** Dado um visitante, quando ele informa nome, e-mail, telefone, **endereço** (AC-011) e senha válidos na
  opção "Criar grupo de cuidado", então a conta é criada com status `AGUARDANDO_CONFIRMACAO_EMAIL`,
  um código é enviado ao e-mail e a tela avisa: **"Confirme seu e-mail em até 24 horas. Depois
  desse prazo o cadastro é descartado e será preciso começar de novo."**
- **AC-001.2** Quando o criador confirma o e-mail (regras de AC-005), ele se torna `ATIVO` e é
  levado ao cadastro do idoso (AC-002). O **grupo é criado no momento em que o idoso é
  cadastrado**, com ele como **Responsável**. Não passa por aprovação.
- **AC-001.3** A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número.
- **AC-001.4** Dado um e-mail já cadastrado (em qualquer status, inclusive membro de um grupo),
  quando alguém tenta criar conta com ele, então a tela mostra **a mesma mensagem de sucesso** de
  AC-001.1 (P5) e o dono do e-mail recebe um e-mail avisando que houve uma tentativa de criar
  conta com o endereço dele, orientando a entrar com a senha ou redefini-la (AC-010) se foi ele.
- **AC-001.5** Uma conta `AGUARDANDO_CONFIRMACAO_EMAIL` que não é confirmada em **24 horas** é
  **descartada**, sem e-mail de aviso: o e-mail fica livre para um novo "Criar grupo de cuidado".
- **AC-001.6** Na tela de confirmação há a ação "reenviar código", que segue AC-005.3 e responde
  sempre com a mesma mensagem, exista ou não a conta (P5).
- **AC-001.7** Um criador `ATIVO` que ainda não cadastrou o idoso é levado ao cadastro do idoso
  toda vez que entra; não há outra tela disponível para ele até concluir, **exceto "Sair"**
  (AC-008.5).

### AC-002 Idoso
- **AC-002.1** Cada grupo de cuidado tem **exatamente um idoso** e **exatamente um Responsável**.
  **Não existe grupo sem idoso**: o grupo nasce junto com o cadastro do idoso (AC-001.2).
- **AC-002.2** O Responsável cadastra do idoso: nome, data de nascimento e **endereço** (AC-011),
  obrigatórios; e, opcionalmente, foto, alergias, condições de saúde e contatos de emergência.
- **AC-002.3** Todos os membros ativos veem esses dados; só o Responsável edita.
- **AC-002.4** Enquanto o idoso não for cadastrado, não há grupo e o criador não consegue enviar
  convites.
- **AC-002.5** Cada contato de emergência tem nome, parentesco/relação, telefone e **endereço**
  (AC-011), todos obrigatórios. O idoso pode ter zero ou mais contatos.
- **AC-002.6** O idoso tem **no máximo uma foto**, nos formatos JPEG ou PNG, de até **5 MB**;
  enviar uma nova substitui a anterior, e a foto pode ser removida.
- **AC-002.7** A data de nascimento é informada como **dd/mm/aaaa**, deve ser uma data válida do
  calendário e **não pode ser futura**.
- **AC-002.8** Alergias e condições de saúde são **texto livre**, cada campo com até **500
  caracteres**.
- **AC-002.9** Edições do cadastro do idoso **não geram histórico**: o cadastro não é registro de
  cuidado (P3 vale para registros de rotina, não para dados cadastrais).

### AC-003 Convite
- **AC-003.1** Quando o Responsável informa o e-mail de uma pessoa, então ela recebe um e-mail com
  um **código de convite de 6 dígitos**, o **nome do Responsável** que convidou, o **primeiro
  nome do idoso** e as instruções para usá-lo na tela de login. Nada além disso sobre o idoso
  (P5).
- **AC-003.2** O código é de **uso único**, vale **somente para o e-mail convidado** e **vence em
  7 dias**.
- **AC-003.3** Depois de 5 tentativas erradas seguidas para um e-mail, novas tentativas para
  aquele e-mail ficam **bloqueadas por 30 minutos** (mensagem e contador em AC-004.7). O convite,
  se existir, continua `ENVIADO`. Ao fim dos 30 minutos o contador volta a zero e, **se há convite
  `ENVIADO` para aquele e-mail**, o sistema faz um **reenvio automático** com as mesmas regras de
  AC-003.4: convite novo, código novo, novo prazo de 7 dias, anterior `CANCELADO`, e a pessoa
  recebe o e-mail de AC-003.1. Se não há convite, nada é enviado (a tela não revela isso, P5).
  O reenvio automático acontece **no máximo 3 vezes** para o mesmo e-mail sem que um código seja
  usado; a partir daí o bloqueio continua terminando em 30 minutos, mas só o Responsável reenvia.
  Esse limite de 3 é por e-mail e só volta a zero quando um código daquele e-mail é usado
  (`USADO`); reenvios manuais do Responsável não o zeram.
- **AC-003.4** **Somente o Responsável** vê a lista de convites, com status (`ENVIADO`, `USADO`,
  `VENCIDO`, `CANCELADO`). Ele pode **cancelar** um convite `ENVIADO` e **reenviar** um convite
  `ENVIADO`, `VENCIDO` ou `CANCELADO`. **Reenviar cria um convite novo**, com código novo, **novo
  prazo de 7 dias** e **zero tentativas**; o anterior, se ainda estava `ENVIADO`, passa para
  `CANCELADO`. Um convite `USADO` não pode ser cancelado nem reenviado: o que existe agora é o
  pedido de acesso (AC-006).
- **AC-003.5** Não é possível convidar um e-mail que já é **membro ativo** do grupo ou que tem
  pedido `AGUARDANDO_APROVACAO`; o Responsável vê "Esta pessoa já faz parte do grupo ou tem um
  pedido em análise."
- **AC-003.6** É possível convidar de novo um e-mail cuja conta no grupo está `RECUSADO`,
  `EXPIRADO` ou `REMOVIDO`; o que acontece ao usar o código está em AC-004.6. (`EXPIRADO` é
  sempre status de **conta**; o status de convite vencido chama-se `VENCIDO`.)
- **AC-003.7** **Não é permitido** convidar um e-mail que pertence a **outro grupo de cuidado**
  (em qualquer status) **nem** um e-mail que tem conta sem grupo (`AGUARDANDO_CONFIRMACAO_EMAIL`,
  ou `ATIVO` que ainda não cadastrou o idoso, AC-001.7). O convite não é criado e o Responsável
  vê, nos dois casos: **"Este e-mail já está vinculado a um grupo de cuidado e não pode ser
  convidado."** A mensagem não diz qual grupo nem quem é a pessoa (P5).
- **AC-003.8** Para cada convite `ENVIADO`, a lista de convites mostra ao Responsável o número de
  tentativas erradas acumuladas e, se o e-mail estiver bloqueado, até que horário. **Não há
  e-mail** ao Responsável por bloqueio (evita que um atacante encha a caixa dele a cada 30 min).
- **AC-003.9** **Não existe desbloqueio manual**: o bloqueio termina sozinho em 30 minutos
  (AC-003.3). Criar um convite para aquele e-mail (envio, reenvio, AC-003.4, ou "convidar de
  novo", AC-003.12) encerra o bloqueio na hora: o contador zera (AC-003.11) e o reenvio
  automático que aconteceria ao fim dos 30 minutos (AC-003.3) **não ocorre**.
- **AC-003.10** Estados do convite (tabela única). Cada linha vira um teste; qualquer outra
  transição é rejeitada. O bloqueio de 30 minutos **não é estado do convite**: é um contador por
  e-mail (AC-003.11).

  | De | Para | Quem/o que provoca | AC |
  |---|---|---|---|
  | (não existe) | `ENVIADO` | Responsável convida ou reenvia; reenvio automático | 003.1, 003.3, 003.4, 003.12 |
  | `ENVIADO` | `USADO` | e-mail e código válidos usados | 004.3, 004.6 |
  | `ENVIADO` | `VENCIDO` | 7 dias desde o envio | 003.2 |
  | `ENVIADO` | `CANCELADO` | Responsável cancela ou reenvia; reenvio automático | 003.3, 003.4, 003.12 |
  | `USADO` / `VENCIDO` / `CANCELADO` | (excluído) | conta do e-mail excluída após 30 dias | 014.1 |
  | qualquer | (excluído) | Responsável exclui o grupo | 013.2 |

- **AC-003.11** O contador de tentativas erradas é **por e-mail**, exista convite ou não. Ele
  volta a zero (a) ao fim dos 30 minutos de bloqueio, (b) quando um convite é criado para aquele
  e-mail (envio, reenvio manual ou automático) e (c) quando um código é usado com sucesso. Assim,
  um e-mail sem convite que acumulou 5 erros não deixa inutilizável o convite que vier depois.
- **AC-003.12** Convidar um e-mail que já tem convite `ENVIADO` **equivale a reenviar**
  (AC-003.4): não dá erro.

### AC-004 Cadastro com o código de convite
- **AC-004.1** Na tela de login há a opção **"Tenho um código de convite"**, que pede e-mail e código.
- **AC-004.2** Com e-mail e código válidos, e sendo um e-mail **sem conta**, o convidado vê o
  formulário de cadastro com o **e-mail já preenchido e não editável** e o primeiro nome do idoso;
  ele informa nome, telefone, **endereço** (AC-011) e senha (AC-001.3).
- **AC-004.3** Ao concluir, o vínculo com o grupo é criado **automaticamente** e o status passa
  direto para `AGUARDANDO_APROVACAO` (o código já comprovou o e-mail; não há confirmação extra).
  O prazo de 24 horas começa a contar.
- **AC-004.4** A tela de conclusão avisa: **"Seu pedido de acesso será analisado pelo responsável
  em até 24 horas. Depois desse prazo ele expira e será preciso pedir um novo convite."**
- **AC-004.5** Mensagens por situação do convite, mostradas **somente quando o código confere**
  com o convite naquele status e **fora do bloqueio de AC-004.7** (código errado cai sempre em
  AC-004.7, P5). Cada uma um teste:
  - `VENCIDO`: **"Este código venceu. Peça um novo convite ao responsável."**
  - `CANCELADO`: **"Este convite foi cancelado. Peça um novo convite ao responsável."**
  - `USADO`: **"Este código já foi usado. Se você já se cadastrou, entre com e-mail e senha."**
- **AC-004.6** Com e-mail e código válidos, e sendo um e-mail que **já tem conta no grupo** com
  status `RECUSADO`, `EXPIRADO` ou `REMOVIDO`, **não há novo cadastro**: nome, telefone, endereço e
  **senha antiga são mantidos**, o status volta para `AGUARDANDO_APROVACAO`, o prazo de 24 horas
  recomeça e a pessoa vê a mensagem de AC-004.4. O Responsável escolhe o papel de novo ao aprovar
  (AC-006.3).
- **AC-004.7** Com código errado **ou** e-mail sem convite, a resposta é a mesma (P5):
  **"E-mail ou código inválidos. Restam N tentativas."**, onde N é contado **por e-mail**, exista
  convite ou não. Na 5ª tentativa errada, e em **qualquer** tentativa nos 30 minutos seguintes,
  com código certo ou errado, a tela mostra **"Bloqueado por tentativas excessivas. Tente
  novamente em MM:SS."** com um **contador regressivo** até o fim do bloqueio, igual nos dois
  casos (P5). Quando o bloqueio termina, a tela volta a aceitar e-mail e código.

### AC-005 Confirmação de e-mail (criador do grupo e redefinição de senha)
- **AC-005.1** O código tem 6 dígitos e **expira em 15 minutos**.
- **AC-005.2** Depois de 5 tentativas erradas, o código é invalidado e é preciso pedir um novo.
- **AC-005.3** O reenvio do código é permitido no máximo 1 vez por minuto.

### AC-006 Aprovação
- **AC-006.1** Ao abrir o app, o Responsável vê um indicador com a quantidade de pedidos pendentes.
- **AC-006.2** A lista de pedidos pendentes é visível **somente ao Responsável** e mostra
  **somente** pedidos `AGUARDANDO_APROVACAO`
  **cujo prazo de 24 horas ainda não venceu** (mesmo que o status ainda não tenha sido atualizado,
  AC-006.8), com nome, e-mail, telefone, data do pedido e tempo restante até a expiração.
- **AC-006.3** Para aprovar, o Responsável **deve** escolher o papel (**Cuidador** ou **Familiar**);
  o status passa para `ATIVO`.
- **AC-006.4** Ao recusar, o status passa para `RECUSADO`.
- **AC-006.5** Se o Responsável não decidir em **24 horas**, o status passa para `EXPIRADO`.
- **AC-006.6** Cuidadores e Familiares não podem aprovar nem recusar.
- **AC-006.7** Quando um pedido entra em `AGUARDANDO_APROVACAO`, o Responsável recebe um
  **e-mail** com o nome da pessoa e o prazo para decidir. O e-mail **não** contém dados de saúde
  do idoso (P5).
- **AC-006.8** Aprovar ou recusar um pedido cujo prazo de 24 horas já venceu **falha**, mesmo que o
  status ainda não tenha sido atualizado, com a mensagem **"Este pedido expirou. Envie um novo
  convite."**
- **AC-006.9** Pedidos `RECUSADO` e `EXPIRADO` saem da lista de pendentes e ficam consultáveis
  **só pelo Responsável**, na lista de membros filtrando por status, enquanto a conta existir
  (AC-014). Cuidadores e Familiares comuns veem na lista de membros apenas membros `ATIVO`.

### AC-007 Aviso do resultado
- **AC-007.1** A pessoa recebe um **e-mail** quando o pedido é **aprovado**, **recusado** ou
  **expira**.
- **AC-007.2** O e-mail de recusa ou expiração orienta a pedir um novo convite ao Responsável; a
  pessoa pode ser convidada novamente (AC-003.6).

### AC-008 Login e sessão
- **AC-008.1** Só usuários `ATIVO` conseguem entrar.
- **AC-008.2** As mensagens por status só aparecem **depois de e-mail e senha corretos** (P5):
  - `AGUARDANDO_CONFIRMACAO_EMAIL`: é levado à tela de confirmação do e-mail (AC-001.6);
  - `AGUARDANDO_APROVACAO`: **"Seu cadastro está aguardando aprovação do responsável."**;
  - `RECUSADO` ou `EXPIRADO`: **"Seu pedido de acesso não foi aprovado. Peça um novo convite ao
    responsável."**;
  - `REMOVIDO`: **"Seu acesso foi removido pelo responsável. Peça um novo convite."**
- **AC-008.3** A sessão permanece ativa no celular por até **7 dias sem uso** e, no máximo, por
  **30 dias** desde a entrada, mesmo com uso; depois disso é preciso entrar de novo com e-mail e
  senha.
- **AC-008.4** O bloqueio por senha errada é **por conta**: depois de 5 senhas erradas seguidas, a
  conta fica bloqueada por 15 minutos. Durante o bloqueio, qualquer tentativa (inclusive com a senha
  certa) recebe **a mesma resposta de "e-mail ou senha inválidos"** (P5) e o dono da conta recebe
  **um** e-mail no momento em que o bloqueio começa (não a cada tentativa). Um login correto zera o contador, e ao fim dos 15 minutos o
  contador também volta a zero.
- **AC-008.5** Existe a ação **"Sair"**, que encerra a sessão naquele aparelho na hora.

### AC-009 Gestão de membros
- **AC-009.1** O Responsável pode alterar o papel de um membro entre Cuidador e Familiar. A
  mudança vale **imediatamente**, inclusive para sessões já abertas.
- **AC-009.2** Ao remover um membro, o status passa para `REMOVIDO` e todas as sessões dele são
  encerradas imediatamente.
- **AC-009.3** Os registros de cuidado feitos por um membro removido continuam visíveis, com o nome
  dele (P3).
- **AC-009.4** O Responsável não pode ser removido nem ter o papel alterado; nenhum membro pode
  ser promovido a Responsável.
- **AC-009.5** De um membro `REMOVIDO`, os demais membros veem **apenas o nome** nos registros
  (AC-009.3); e-mail, telefone e endereço dele ficam visíveis **somente para o Responsável**, na
  lista de membros filtrada por status.

### AC-010 Redefinição de senha
- **AC-010.1** O usuário informa o e-mail e recebe um código com as regras de AC-005.
- **AC-010.2** A resposta é a mesma quer o e-mail exista ou não (P5).
- **AC-010.3** Com o código válido, o usuário define a nova senha, que segue AC-001.3.
- **AC-010.4** Ao concluir, **todas as sessões** da conta são encerradas e é preciso entrar de novo
  com a nova senha.
- **AC-010.5** Qualquer conta existente pode redefinir a senha; a redefinição **não altera o
  status** da conta.

### AC-011 Endereço (regra única para idoso, contatos de emergência e membros)
- **AC-011.1** Campos: CEP, logradouro, número, complemento, bairro, cidade e UF. **Todos
  obrigatórios, exceto complemento.** Número aceita "S/N".
- **AC-011.2** CEP com exatamente 8 dígitos numéricos; UF deve ser uma das 27 unidades da
  federação.
- **AC-011.3** Ao digitar um CEP com 8 dígitos, logradouro, bairro, cidade e UF são **preenchidos
  automaticamente** e continuam **editáveis**.
- **AC-011.4** Se o CEP não for encontrado, ou se a consulta não responder em **15 segundos**, a
  pessoa vê, no ato da falha, o aviso **"Não conseguimos consultar o CEP. Preencha o endereço
  manualmente."** e **preenche manualmente**; o cadastro nunca fica bloqueado por isso.
- **AC-011.5** Visibilidade do endereço de **membros** (P5). Cada célula vira um teste:

  | Quem vê ↓ / endereço de → | si mesmo | Cuidadores | Familiares (inclui o Responsável) |
  |---|---|---|---|
  | **Cuidador** | sim | **não** | não |
  | **Familiar** (inclui o Responsável) | sim | sim | sim |

  Como o Responsável é um Familiar, a regra fica: **Cuidador vê só o próprio endereço;
  Familiar vê o de todos.**

  Quando não pode ver, o endereço simplesmente **não aparece**; nome e papel continuam visíveis.
  A matriz vale **entre membros `ATIVO`**; contas em outros status só o Responsável vê (AC-006.2,
  AC-006.9, AC-009.5).
- **AC-011.6** O membro pode alterar o próprio telefone e endereço a qualquer momento.
- **AC-011.7** O endereço do **idoso** e dos **contatos de emergência** é visível para todos os
  membros ativos, inclusive Cuidadores.
- **AC-011.8** **Telefone e e-mail** de membros seguem a **mesma matriz de AC-011.5**: Cuidador vê
  só os próprios; Familiar vê os de todos. Quando não pode ver, o campo não aparece.

### AC-012 Estados da conta (tabela única)
- **AC-012.1** Uma conta está sempre em exatamente um destes estados, e só muda pelas transições
  abaixo. Cada linha vira um teste; qualquer outra transição é rejeitada.

  | De | Para | Quem/o que provoca | AC |
  |---|---|---|---|
  | (não existe) | `AGUARDANDO_CONFIRMACAO_EMAIL` | visitante cria grupo | 001.1 |
  | `AGUARDANDO_CONFIRMACAO_EMAIL` | `ATIVO` (criador sem grupo; vira Responsável ao cadastrar o idoso) | código de e-mail correto | 001.2, 001.7 |
  | `AGUARDANDO_CONFIRMACAO_EMAIL` | (descartada) | 24 h sem confirmar | 001.5 |
  | (não existe) | `AGUARDANDO_APROVACAO` | convidado conclui o cadastro | 004.3 |
  | `RECUSADO` / `EXPIRADO` / `REMOVIDO` | `AGUARDANDO_APROVACAO` | novo código de convite usado | 004.6 |
  | `AGUARDANDO_APROVACAO` | `ATIVO` (Cuidador ou Familiar) | Responsável aprova | 006.3 |
  | `AGUARDANDO_APROVACAO` | `RECUSADO` | Responsável recusa | 006.4 |
  | `AGUARDANDO_APROVACAO` | `EXPIRADO` | 24 h sem decisão | 006.5 |
  | `ATIVO` (Cuidador ou Familiar sem administração) | `REMOVIDO` | Responsável remove | 009.2 |
  | `RECUSADO` / `EXPIRADO` | (excluída) | 30 dias sem novo convite | 014.1 |
  | `ATIVO` (criador sem grupo) | (excluída) | 30 dias sem cadastrar o idoso | 014.3 |
  | qualquer status **no grupo** (`ATIVO`, `AGUARDANDO_APROVACAO`, `RECUSADO`, `EXPIRADO`, `REMOVIDO`) | (excluída) | Responsável exclui o grupo | 013.2 |

  O bloqueio temporário por senha errada (AC-008.4) **não é um estado da conta**, nem o bloqueio
  do código de convite (AC-003.11): são contadores que convivem com qualquer estado.

- **AC-012.2** Como cada transição é comunicada à pessoa. Cada linha vira um teste:

  | Transição | Aviso na tela no ato | E-mail (a quem) | Ao tentar entrar (AC-008.2) |
  |---|---|---|---|
  | criação da conta (001.1) | sim, texto de 001.1 | à pessoa: código de confirmação | leva à confirmação |
  | confirmação do e-mail (001.2) | sim: é levado ao cadastro do idoso | à pessoa: não | entra normalmente (vai ao cadastro do idoso, 001.7) |
  | cadastro por convite e reconvite (004.3, 004.6) | sim, texto de 004.4 | à pessoa: não; ao Responsável: sim (006.7) | mensagem de pendente |
  | aprovação (006.3) | não (é outra pessoa) | à pessoa: sim (007.1) | entra normalmente (008.1) |
  | recusa, expiração (006.4, 006.5) | não (é outra pessoa ou o prazo) | à pessoa: sim (007.1) | mensagem do status |
  | remoção (009.2) | não | à pessoa: não | mensagem de removido |
  | descarte em 24 h sem confirmar (001.5) | não | à pessoa: **não** (silencioso) | conta não existe |
  | exclusão após 30 dias (014.1, 014.3) | não | à pessoa: sim, curto (e lembrete prévio em 014.3) | conta não existe |
  | exclusão do grupo (013.2) | sim, ao Responsável: **"O grupo de cuidado foi excluído."** e volta à tela de login | a todos os do grupo (013.3) | conta não existe |

  Quando a conta deixa de existir, a tentativa de entrar recebe a resposta comum de "e-mail ou
  senha inválidos" (P5).

### AC-013 Exclusão do grupo de cuidado
- **AC-013.1** Só o Responsável pode excluir o grupo. Antes de concluir, a tela mostra **"Excluir
  o grupo apaga o idoso, todos os registros e todas as contas, inclusive a sua. Isso não pode ser
  desfeito."** e a ação só acontece ao tocar em **"Excluir definitivamente"**.
- **AC-013.2** A exclusão apaga o idoso, os contatos de emergência, os registros de cuidado, os
  convites e **todas as contas do grupo**, inclusive a do Responsável. Depois disso os e-mails
  ficam livres para novos cadastros.
- **AC-013.3** Todas as pessoas com conta no grupo (`ATIVO`, `AGUARDANDO_APROVACAO`, `RECUSADO`,
  `EXPIRADO` e `REMOVIDO`) recebem um **e-mail** informando que o grupo foi excluído e que os
  dados delas foram apagados. O e-mail não contém dados de saúde (P5).
- **AC-013.4** Um membro **não exclui a própria conta**; quem quer sair pede a remoção ao
  Responsável (AC-009.2).
- **AC-013.5** A exclusão do grupo é a **única** situação em que registros de cuidado são
  apagados: é o Responsável encerrando o grupo inteiro, não uma correção de registro (exceção
  registrada em P3, Constitution v1.3; P3 continua valendo para tudo o mais).

### AC-014 Retenção de dados de contas não aprovadas
- **AC-014.1** Uma conta `RECUSADO` ou `EXPIRADO` é **excluída por completo** após **30 dias**,
  **junto com todos os convites daquele e-mail** (ela nunca teve acesso, então não há registro de
  cuidado a preservar). Os 30 dias contam desde a
  transição para `RECUSADO`/`EXPIRADO` e **recomeçam a cada convite criado** para aquele e-mail
  (envio, reenvio manual ou automático, AC-003.3);
  se esse convite terminar sem uso (`VENCIDO` ou `CANCELADO`), contam de novo a partir desse
  momento. Na exclusão a
  pessoa recebe um e-mail curto avisando que os dados foram apagados; o e-mail fica livre, e se for
  convidada depois disso faz o cadastro completo (AC-004.2).
- **AC-014.2** Uma conta `REMOVIDO` **não é excluída** automaticamente: os dados dela ficam
  guardados, visíveis só ao Responsável (AC-009.5), para preservar o histórico (P3) e permitir o
  reconvite (AC-004.6).
- **AC-014.3** Uma conta `ATIVO` que não cadastrou o idoso em **30 dias** desde a confirmação do
  e-mail é **excluída por completo**; **7 dias antes** a pessoa recebe um e-mail lembrando de
  concluir o cadastro, e na exclusão recebe o mesmo e-mail curto de AC-014.1.

### AC-015 Ações sem permissão (regra transversal)
- **AC-015.1** Uma ação que o papel da pessoa não permite (editar o idoso, convidar, aprovar,
  recusar, alterar papel, remover, excluir o grupo, ver listas restritas ao Responsável) **não
  aparece** na interface.
- **AC-015.2** Se a ação chegar ao servidor por outro caminho e for sobre um recurso **do próprio
  grupo** que o papel não permite, ela é rejeitada com a mensagem **"Você não tem permissão para
  esta ação."**, sem revelar dados (P5).
- **AC-015.3** Se a ação for sobre um recurso de **outro grupo de cuidado** ou que **não existe**,
  a resposta é **idêntica** nos dois casos, a de "recurso não encontrado", e não indica qual dos
  dois ocorreu (P5). Cada combinação (próprio grupo, outro grupo, inexistente) vira um teste.

## 5. Fora de escopo

Login social, autenticação em dois fatores, login sem senha (somente OTP), SMS/WhatsApp,
notificação push (o Responsável é avisado no app e por e-mail), uma pessoa participar de
mais de um grupo de cuidado, mais de um idoso por grupo, mais de um Responsável por grupo,
transferência do papel de Responsável, recuperação do grupo caso o Responsável perca o acesso
definitivamente (suporte manual), exclusão da própria conta por um membro (só o Responsável exclui,
e exclui o grupo inteiro, AC-013), histórico de alterações do cadastro do idoso (AC-002.9).

## 6. Dúvidas

### Em aberto

Nenhuma.

### Resolvidas
- **DÚVIDA-1** Vínculo com o idoso → pelo **convite enviado por e-mail** pelo Responsável (rev. 2).
- **DÚVIDA-2** Primeiro Responsável → fluxo **"Criar grupo de cuidado"** (rev. 2).
- **DÚVIDA-3** Vários Responsáveis → rev. 2 decidiu "sim"; **rev. 3 mudou para um único
  Responsável**; **rev. 4 descartou a transferência do papel** (AC-009.4).
- **DÚVIDA-4** Recusa → a pessoa **é avisada** (por e-mail) (rev. 2).
- **DÚVIDA-5** Pedido pendente → **expira em 24 horas**; aviso exibido no ato do cadastro (rev. 2).
- **DÚVIDA-6** Convite → **código OTP de 6 dígitos por e-mail**, digitado na tela de login,
  **válido por 7 dias**; substitui o link e a confirmação de e-mail do convidado (rev. 3).
- **Risco vindo do plan** Responsável sem push pode perder o prazo de 24 h → **também recebe
  e-mail** quando chega um pedido (AC-006.7, rev. 5).
- **Sessão** 30 dias sem uso → **7 dias** (AC-008.3, rev. 6).
- **Endereço** → obrigatório para idoso, contatos de emergência e membros; preenchimento por CEP
  (AC-011, rev. 7).
- **Visibilidade do endereço de membros** → Cuidador vê só o próprio; Familiar vê o próprio, dos
  Cuidadores e dos Familiares; Responsável vê todos (AC-011.5, rev. 8).
- **DÚVIDA-7** Familiar vê o endereço do Responsável? → **sim**: o Responsável passa a ser um
  **Familiar com permissão de administrador**, e não um terceiro papel (rev. 9, Constitution v1.2).

Rev. 10 e 11 (respostas de Alexandre à revisão do `revisor-spec`; as premissas da rev. 10 foram
confirmadas na rev. 11, exceto onde indicado):

- **DÚVIDA-8** Reconvite de conta `RECUSADO`/`EXPIRADO`/`REMOVIDO` → **sem novo cadastro**; volta
  para `AGUARDANDO_APROVACAO` com a **senha antiga** (AC-003.6, AC-004.6).
- **DÚVIDA-9** Só o Responsável (Familiar administrador) cria grupo e convida; os demais entram só
  por convite. Membro que tenta "Criar grupo" cai na resposta neutra de AC-001.4. E-mail de outro
  grupo: **não pode ser convidado**; o Responsável vê a mensagem de AC-003.7 (rev. 11; a rev. 10
  propunha deixar o convite sair e avisar só a pessoa convidada). Trade-off aceito: o Responsável
  fica sabendo que o e-mail já existe no sistema, sem saber em qual grupo (P5).
- **DÚVIDA-10** Um idoso → **exatamente um**; o grupo nasce com o idoso (AC-001.2, AC-002.1,
  AC-002.4, AC-001.7).
- **DÚVIDA-11** Tabela única de estados e transições (AC-012.1); estados que faltavam no login
  (AC-008.2); mensagem de status só após senha correta (P5). Aviso ao usuário em toda transição
  (AC-012.2); se é toast ou modal fica para o plan.
- **DÚVIDA-12** Reenviar **cria convite novo com prazo novo** (AC-003.4). Convite invalidado por
  tentativas → status `BLOQUEADO`; `USADO` não se reenvia nem cancela. Rev. 11: o bloqueio **avisa o
  Responsável por e-mail** (AC-003.8), que pode **desbloquear** mantendo código e prazo (AC-003.9)
  ou reenviar; a pessoa bloqueada vê que o Responsável foi avisado (AC-004.7).
- **DÚVIDA-13** Conta não confirmada expira em **24 horas**, com aviso na tela de criação
  (AC-001.1, AC-001.5). Resposta ao e-mail duplicado: mesma tela de sucesso + e-mail ao dono
  (AC-001.4). Reenvio do código de confirmação com resposta neutra (AC-001.6).
- **DÚVIDA-14** Redefinição de senha: nova senha segue AC-001.3, todas as sessões caem e qualquer
  conta pode redefinir sem mudar de status (AC-010.3 a AC-010.5, rev. 11).
- **DÚVIDA-15** Membro removido: nada visível aos demais além do nome nos registros; dados só para
  o Responsável (AC-009.5, AC-014.2). Telefone e e-mail de membros ativos seguem a matriz do
  endereço (AC-011.8).
- **DÚVIDA-16** Aprovar pedido vencido falha com mensagem (AC-006.8); recusados/expirados saem da
  lista de pendentes (AC-006.9).
- **DÚVIDA-17** Sessão: 7 dias sem uso **e** máximo de 30 dias; ação "Sair" (AC-008.3, AC-008.5).
- **DÚVIDA-18** Bloqueio por conta, resposta idêntica à de senha errada, e-mail ao dono, contador
  zera no login correto (AC-008.4).
- **DÚVIDA-19** Textos de código expirado/cancelado/usado; código errado e e-mail sem convite com a
  mesma resposta e contagem de tentativas por e-mail (AC-004.5, AC-004.7).
- **DÚVIDA-20** Troca de papel vale **imediatamente** (AC-009.1).
- **DÚVIDA-21** Uma única foto (JPEG/PNG, 5 MB, AC-002.6); data de nascimento validada como
  dd/mm/aaaa, não futura (AC-002.7); sem histórico do cadastro (AC-002.9). Alergias/condições →
  texto livre de até 500 caracteres cada (AC-002.8, rev. 11).
- **DÚVIDA-22** CEP: 8 dígitos; consulta com limite de **15 segundos** e aviso visual claro na
  falha (AC-011.3, AC-011.4).
- **DÚVIDA-23** E-mail de convite leva o nome do Responsável e só o **primeiro nome** do idoso
  (AC-003.1).
- **DÚVIDA-24** Contas `RECUSADO`/`EXPIRADO` são excluídas após **30 dias** sem novo convite;
  `REMOVIDO` fica guardada (AC-014).
- **DÚVIDA-25** Exclusão do grupo **só pelo Responsável**, com e-mail a todos os afetados;
  membro não exclui a própria conta (US-012, AC-013). A exceção a P3 foi registrada na
  Constitution v1.3 (rev. 12).

Rev. 13 (respostas de Alexandre à segunda rodada do `revisor-spec`; todas as propostas aceitas):

- **DÚVIDA-26** Descarte em 24 h é silencioso; exclusões após 30 dias mandam e-mail curto. AC-012.2
  virou tabela de comunicação por transição.
- **DÚVIDA-27** Tabela de estados do convite (AC-003.10). `BLOQUEADO` vencido vira `VENCIDO`
  sozinho (AC-003.9); reenvio nasce com zero tentativas (AC-003.4); contador por e-mail zera ao
  criar convite ou desbloquear (AC-003.11).
- **DÚVIDA-28** Mensagens de AC-004.5 só com código conferido; código errado cai em AC-004.7;
  `BLOQUEADO` repete a mensagem de bloqueio mesmo com código certo.
- **DÚVIDA-29** E-mail com conta sem grupo não pode ser convidado (AC-003.7); convidar e-mail com
  convite `ENVIADO` equivale a reenviar (AC-003.12).
- **DÚVIDA-30** Conta `ATIVO` sem idoso é excluída em 30 dias, com lembrete 7 dias antes
  (AC-014.3, AC-012.1).
- **DÚVIDA-31** Os 30 dias de AC-014.1 contam da recusa/expiração e recomeçam a cada convite.
- **DÚVIDA-32** Convites, pendentes e contas não ativas são visíveis só ao Responsável (AC-003.4,
  AC-006.2, AC-006.9); a matriz de AC-011.5/AC-011.8 vale entre membros `ATIVO`.
- **DÚVIDA-33** Ações sem permissão não aparecem; no servidor, mensagem única (AC-015).
- **DÚVIDA-34** Contador de senha errada zera ao fim dos 15 minutos (AC-008.4).
- **DÚVIDA-35** Textos do aviso de CEP (AC-011.4) e da confirmação de exclusão (AC-013.1).
- **Vocabulário** Status de convite `EXPIRADO` renomeado para `VENCIDO`; `EXPIRADO` fica só para
  conta (AC-003.4, AC-003.6, AC-003.10, AC-004.5).

Rev. 14 (correções de redação da terceira rodada, sem decisão nova): AC-003.2 "vence"; convite
`BLOQUEADO` também equivale a reenviar (AC-003.12); linha de exclusão do grupo na tabela do convite
(AC-003.10); `CANCELADO` reinicia os 30 dias (AC-014.1); um único e-mail por bloqueio (AC-008.4);
tabela de comunicação (AC-012.2) com destinatário do e-mail e texto do aviso de exclusão;
precisões de vocabulário nas tabelas de estados.
- **DÚVIDA-36** Ações sem permissão divididas: recurso do próprio grupo → mensagem de permissão
  (AC-015.2); recurso de outro grupo ou inexistente → resposta idêntica de "não encontrado"
  (AC-015.3). Sobe para a spec a decisão que o plan já tinha.

Rev. 15 (DÚVIDA-37, decisão de Alexandre; **substitui** a parte de bloqueio das DÚVIDA-12, 27 e
28):

- **DÚVIDA-37** O bloqueio do código de convite passa a ser **por tempo**: 5 erros seguidos por
  e-mail → 30 minutos bloqueado, com a mensagem "Bloqueado por tentativas excessivas. Tente
  novamente em MM:SS." e contador regressivo, igual para e-mail com ou sem convite (AC-004.7).
  **Sai** o status `BLOQUEADO`, o e-mail ao Responsável e o desbloqueio manual (AC-003.8,
  AC-003.9, AC-003.10). Ao fim dos 30 minutos o contador zera e, se há convite `ENVIADO`, é feito
  um **reenvio automático** com código novo (AC-003.3), no máximo 3 vezes por e-mail sem uso do
  código (limite proposto pela sessão para conter spam ao convidado; retirar se não quiser). A
  lista de convites mostra tentativas e horário de fim do bloqueio ao Responsável (AC-003.8).

Rev. 16 (conferência da rev. 15, redação sem decisão nova): criar convite cancela o reenvio
automático pendente (AC-003.9); o limite de 3 reenvios automáticos só zera com código usado
(AC-003.3); o reenvio automático também reinicia os 30 dias de retenção (AC-014.1); bloqueio de
convite não é estado (AC-012.1); AC-004.5 explicita "fora do bloqueio".

Rev. 17:

- **DÚVIDA-38** Convites de um e-mail são apagados junto com a conta excluída por AC-014.1
  (linha nova em AC-003.10). Com isso a spec fecha sem dúvidas e está **aprovada para o plan**.
