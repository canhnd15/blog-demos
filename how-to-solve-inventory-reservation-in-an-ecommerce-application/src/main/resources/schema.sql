CREATE TABLE IF NOT EXISTS inventory (
    sku       VARCHAR(64) PRIMARY KEY,
    available INT    NOT NULL,
    version   BIGINT NOT NULL DEFAULT 0,
    -- Chot chan cuoi cung: du code sai thi DB van khong cho available am
    CONSTRAINT chk_inventory_available CHECK (available >= 0)
);

CREATE TABLE IF NOT EXISTS stock_reservation (
    id         UUID PRIMARY KEY,
    order_id   VARCHAR(64) NOT NULL UNIQUE,
    sku        VARCHAR(64) NOT NULL,
    qty        INT         NOT NULL,
    status     VARCHAR(16) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reservation_status_expires
    ON stock_reservation (status, expires_at);
