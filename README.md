# Cuida+ (specdriven)

Projeto de aprendizado de Spec-Driven Development: app mobile-first para rotina de cuidadores
de idosos. A fonte da verdade são os documentos em `specs/` (leia `CLAUDE.md` e
`JORNADA-SDD.md`).

- `specs/constitution.md`: princípios do projeto
- `specs/roadmap.md`: ordem das features
- `specs/NNN-feature/`: spec → plan → contracts → tasks
- `backend/`: Spring Boot 4.1 / Java 25 (Maven)
- `frontend/`: Angular 22 (a criar)
- `docker-compose.yml`: PostgreSQL 17 e Mailpit para desenvolvimento

## Requisitos

| Ferramenta | Versão | Observação |
|---|---|---|
| Docker Desktop | qualquer recente | precisa estar **rodando** para o compose e para os testes (Testcontainers) |
| JDK | **25** (Temurin) | o Maven precisa de `JAVA_HOME` apontando para ele |
| Maven | 3.9+ | |
| Node | LTS exigida pelo Angular 22 | só para o frontend |

## Variáveis de ambiente: um único `.env` na raiz

Todas as variáveis de ambiente ficam em **`.env` na raiz** do repositório (plan 000, D-43).
O arquivo **não é versionado**; o modelo versionado é [`.env.example`](.env.example).

```
cp .env.example .env    # depois ajuste o que precisar
```

Quem lê o `.env`:

- **Docker Compose** lê automaticamente (banco, portas).
- **Backend Spring** importa o arquivo como properties (`spring.config.import` em
  `backend/src/main/resources/application.yml`), tanto rodando de `backend/` quanto da raiz.
  Variáveis exportadas no shell têm precedência sobre o arquivo.
- `SPRING_PROFILES_ACTIVE` e `JAVA_HOME` valem para o shell e ferramentas, não para o Spring
  (o profile padrão do backend já é `dev`).

Nunca coloque segredo real no `.env.example`. Em produção, `MAIL_USERNAME` e `MAIL_PASSWORD`
(senha de app do Gmail, D-24) vêm do ambiente do servidor.

## Subindo o ambiente local

```
docker compose up -d --wait        # PostgreSQL em :5432, Mailpit UI em http://localhost:8025
```

## Backend

```
cd backend
mvn verify                         # testes; sobem containers próprios via Testcontainers
mvn spring-boot:run                # profile dev: usa o PostgreSQL e o Mailpit do compose
```

Profiles: `dev` (padrão, compose), `test` (Testcontainers), `prod` (Gmail SMTP).
