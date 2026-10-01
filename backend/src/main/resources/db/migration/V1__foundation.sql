-- T-010: esquema de fundação (plan.md §3, rev. 2).
--
-- Convenções: snake_case; timestamps em TIMESTAMPTZ (Constitution §5); ids em uuid, gerados em
-- Java (DEFAULT gen_random_uuid() só como conveniência, Postgres 17 já traz a função no core).
-- Endereço (D-25) embutido com prefixo address_ em users, elders e emergency_contacts.

-- ---------------------------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------------------------
CREATE TABLE users (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                   VARCHAR(200) NOT NULL,
    email                  VARCHAR(320) NOT NULL,
    phone                  VARCHAR(20) NOT NULL,
    address_zip_code       CHAR(8) NOT NULL,
    address_street         VARCHAR(200) NOT NULL,
    address_number         VARCHAR(20) NOT NULL,
    address_complement     VARCHAR(100) NULL,
    address_district       VARCHAR(100) NOT NULL,
    address_city           VARCHAR(100) NOT NULL,
    address_state          CHAR(2) NOT NULL,
    password_hash          VARCHAR(100) NOT NULL,
    email_verified_at      TIMESTAMPTZ NULL,
    failed_logins          INTEGER NOT NULL DEFAULT 0,
    locked_until           TIMESTAMPTZ NULL,
    lock_notified_at       TIMESTAMPTZ NULL,
    elder_reminder_sent_at TIMESTAMPTZ NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- ---------------------------------------------------------------------------------------------
-- care_groups
-- ---------------------------------------------------------------------------------------------
CREATE TABLE care_groups (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------------------------
-- elders (D-02: um único idoso por grupo via group_id UNIQUE; D-36: limites de conteúdo)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE elders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id            UUID NOT NULL REFERENCES care_groups (id) ON DELETE CASCADE,
    name                VARCHAR(200) NOT NULL,
    birth_date          DATE NOT NULL,
    address_zip_code    CHAR(8) NOT NULL,
    address_street      VARCHAR(200) NOT NULL,
    address_number      VARCHAR(20) NOT NULL,
    address_complement  VARCHAR(100) NULL,
    address_district    VARCHAR(100) NOT NULL,
    address_city        VARCHAR(100) NOT NULL,
    address_state       CHAR(2) NOT NULL,
    photo_path          VARCHAR(500) NULL,
    allergies           VARCHAR(500) NULL,
    conditions          VARCHAR(500) NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_elders_group_id UNIQUE (group_id),
    CONSTRAINT ck_elders_birth_date_not_future CHECK (birth_date <= CURRENT_DATE)
);

-- ---------------------------------------------------------------------------------------------
-- emergency_contacts (D-26: tabela própria, não JSONB)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE emergency_contacts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    elder_id            UUID NOT NULL REFERENCES elders (id) ON DELETE CASCADE,
    name                VARCHAR(200) NOT NULL,
    relationship        VARCHAR(100) NOT NULL,
    phone               VARCHAR(20) NOT NULL,
    address_zip_code    CHAR(8) NOT NULL,
    address_street      VARCHAR(200) NOT NULL,
    address_number      VARCHAR(20) NOT NULL,
    address_complement  VARCHAR(100) NULL,
    address_district    VARCHAR(100) NOT NULL,
    address_city        VARCHAR(100) NOT NULL,
    address_state       CHAR(2) NOT NULL,
    position            INTEGER NOT NULL
);

-- ---------------------------------------------------------------------------------------------
-- memberships (D-02 user_id UNIQUE; D-03 um único admin por grupo; D-08 lock otimista;
-- D-09 retention_deadline; D-29 Cuidador nunca admin)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE memberships (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id           UUID NOT NULL REFERENCES care_groups (id) ON DELETE CASCADE,
    user_id            UUID NOT NULL REFERENCES users (id),
    role               VARCHAR(20) NOT NULL,
    is_admin           BOOLEAN NOT NULL DEFAULT false,
    status             VARCHAR(30) NOT NULL,
    requested_at       TIMESTAMPTZ NOT NULL,
    expires_at         TIMESTAMPTZ NULL,
    decided_by         UUID NULL REFERENCES users (id),
    decided_at         TIMESTAMPTZ NULL,
    retention_deadline TIMESTAMPTZ NULL,
    version            BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_memberships_user_id UNIQUE (user_id),
    CONSTRAINT ck_memberships_role CHECK (role IN ('CUIDADOR', 'FAMILIAR')),
    CONSTRAINT ck_memberships_status
        CHECK (status IN ('AGUARDANDO_APROVACAO', 'ATIVO', 'RECUSADO', 'EXPIRADO', 'REMOVIDO')),
    CONSTRAINT ck_memberships_cuidador_nunca_admin CHECK (NOT is_admin OR role = 'FAMILIAR')
);

-- D-03: índice único parcial garante exatamente um Responsável (is_admin) por grupo, mas permite
-- vários membros não-admin no mesmo grupo.
CREATE UNIQUE INDEX uk_memberships_one_admin_per_group
    ON memberships (group_id)
    WHERE is_admin;

-- ---------------------------------------------------------------------------------------------
-- invitations (D-34: origin MANUAL/AUTO)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE invitations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id     UUID NOT NULL REFERENCES care_groups (id) ON DELETE CASCADE,
    email        VARCHAR(320) NOT NULL,
    code_hash    VARCHAR(100) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    origin       VARCHAR(10) NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    created_by   UUID NOT NULL REFERENCES users (id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    used_at      TIMESTAMPTZ NULL,
    cancelled_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_invitations_status CHECK (status IN ('ENVIADO', 'USADO', 'VENCIDO', 'CANCELADO')),
    CONSTRAINT ck_invitations_origin CHECK (origin IN ('MANUAL', 'AUTO'))
);

-- ---------------------------------------------------------------------------------------------
-- invite_code_attempts (D-34: contador por e-mail, existe mesmo sem convite; PK = email)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE invite_code_attempts (
    email           VARCHAR(320) PRIMARY KEY,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    blocked_until   TIMESTAMPTZ NULL,
    auto_resends    INTEGER NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------------------------
-- otp_codes (D-04: só hash do código)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE otp_codes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    purpose      VARCHAR(30) NOT NULL,
    code_hash    VARCHAR(100) NOT NULL,
    attempts     INTEGER NOT NULL DEFAULT 0,
    expires_at   TIMESTAMPTZ NOT NULL,
    consumed_at  TIMESTAMPTZ NULL,
    last_sent_at TIMESTAMPTZ NULL
);

-- ---------------------------------------------------------------------------------------------
-- refresh_tokens (D-11: teto absoluto de 30 dias em absolute_expires_at)
-- ---------------------------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash          VARCHAR(100) NOT NULL,
    expires_at          TIMESTAMPTZ NOT NULL,
    absolute_expires_at TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
