DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS borrow_records CASCADE;
DROP TABLE IF EXISTS readers CASCADE;
DROP TABLE IF EXISTS books CASCADE;

CREATE TABLE IF NOT EXISTS books (
    isbn             VARCHAR(20)   PRIMARY KEY,
    title            VARCHAR(200)  NOT NULL,
    author           VARCHAR(100)  NOT NULL,
    total_stock      INT           NOT NULL CHECK (total_stock >= 0),
    available_stock  INT           NOT NULL CHECK (available_stock >= 0),
    shelf_location   VARCHAR(50),
    description      TEXT,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_books_title  ON books (title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books (author);
CREATE INDEX IF NOT EXISTS idx_books_isbn   ON books (isbn);

CREATE TABLE IF NOT EXISTS readers (
    reader_id            VARCHAR(20)   PRIMARY KEY,
    name                 VARCHAR(50)   NOT NULL,
    phone                VARCHAR(11)   NOT NULL,
    password_hash        VARCHAR(255)  NOT NULL,
    current_borrow_count INT           NOT NULL DEFAULT 0,
    max_borrow_count     INT           NOT NULL DEFAULT 3,
    role                 VARCHAR(20)   NOT NULL DEFAULT 'ROLE_READER',
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_role CHECK (role IN ('ROLE_READER', 'ROLE_CIRCULATION', 'ROLE_ADMIN'))
);

CREATE TABLE IF NOT EXISTS borrow_records (
    id           BIGSERIAL    PRIMARY KEY,
    reader_id    VARCHAR(20)  NOT NULL,
    isbn         VARCHAR(20)  NOT NULL,
    borrow_date  DATE         NOT NULL,
    due_date     DATE         NOT NULL,
    return_date  DATE,
    status       VARCHAR(10)  NOT NULL DEFAULT 'BORROWED',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_status CHECK (status IN ('BORROWED', 'RETURNED', 'OVERDUE'))
);

CREATE INDEX IF NOT EXISTS idx_br_reader_id ON borrow_records (reader_id);
CREATE INDEX IF NOT EXISTS idx_br_isbn      ON borrow_records (isbn);
CREATE INDEX IF NOT EXISTS idx_br_status    ON borrow_records (status);

CREATE TABLE IF NOT EXISTS audit_logs (
    id           BIGSERIAL    PRIMARY KEY,
    operator_id  VARCHAR(20)  NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    target       VARCHAR(50),
    detail       TEXT,
    ip_address   VARCHAR(45),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_created  ON audit_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_audit_operator ON audit_logs (operator_id);