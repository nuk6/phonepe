CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(64)     PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL
);

CREATE TABLE IF NOT EXISTS mutual_funds (
    id              VARCHAR(64)     PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    category        VARCHAR(100)    NOT NULL,
    current_nav     NUMERIC(18, 4)  NOT NULL
);

CREATE TABLE IF NOT EXISTS sips (
    id                      VARCHAR(64)     PRIMARY KEY,
    user_id                 VARCHAR(64)     NOT NULL REFERENCES users(id),
    fund_id                 VARCHAR(64)     NOT NULL REFERENCES mutual_funds(id),
    amount                  NUMERIC(18, 2)  NOT NULL,
    base_amount             NUMERIC(18, 2)  NOT NULL,
    mode                    VARCHAR(20)     NOT NULL,
    state                   VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    start_date              DATE            NOT NULL,
    next_execution_date     DATE            NOT NULL,
    step_up_percentage      DOUBLE PRECISION NOT NULL DEFAULT 0,
    installment_count       INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_sips_user_id ON sips(user_id);
CREATE INDEX idx_sips_state ON sips(state);
CREATE INDEX idx_sips_next_exec ON sips(state, next_execution_date);

CREATE TABLE IF NOT EXISTS sip_installments (
    id                  VARCHAR(64)     PRIMARY KEY,
    sip_id              VARCHAR(64)     NOT NULL REFERENCES sips(id),
    amount              NUMERIC(18, 2)  NOT NULL,
    nav                 NUMERIC(18, 4)  NOT NULL,
    units_allotted      NUMERIC(18, 4)  NOT NULL,
    execution_date      DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL
);

CREATE INDEX idx_installments_sip_id ON sip_installments(sip_id);

