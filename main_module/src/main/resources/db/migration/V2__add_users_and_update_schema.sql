CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL
    );

ALTER TABLE carts ADD COLUMN user_id BIGINT;
ALTER TABLE orders ADD COLUMN user_id BIGINT;

UPDATE carts SET user_id = 1 WHERE user_id IS NULL;

UPDATE orders SET user_id = 1 WHERE MOD(id, 2) = 0;
UPDATE orders SET user_id = 2 WHERE MOD(id, 2) <> 0;

ALTER TABLE carts
    ADD CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS user_balances (
    id         SERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    balance     DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_user_balance_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
);