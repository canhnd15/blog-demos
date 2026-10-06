CREATE TABLE IF NOT EXISTS orders (
    id              UUID PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    cart_id         VARCHAR(64) NOT NULL,
    sku             VARCHAR(64) NOT NULL,
    qty             INT         NOT NULL,
    amount          BIGINT      NOT NULL,
    status          VARCHAR(16) NOT NULL,
    -- Nullable: cách 2 (naive) không dùng key. Postgres cho phép nhiều NULL trong UNIQUE.
    idempotency_key VARCHAR(64) UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS idempotency_record (
    idempotency_key VARCHAR(64) PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    request_hash    VARCHAR(64) NOT NULL,
    order_id        UUID,
    created_at      TIMESTAMPTZ NOT NULL
);
