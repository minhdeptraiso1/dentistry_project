-- =========================
-- MEDICINE SALE PRICE (current)
-- =========================
ALTER TABLE medicines
    ADD COLUMN sale_price NUMERIC(12, 2);

-- optional: chặn âm
ALTER TABLE medicines
    ADD CONSTRAINT chk_medicine_sale_price_non_negative
        CHECK (sale_price IS NULL OR sale_price >= 0);

CREATE INDEX idx_medicines_sale_price ON medicines (sale_price);

-- =========================
-- MEDICINE PRICE HISTORY
-- =========================
CREATE TABLE medicine_price_histories
(
    id          UUID PRIMARY KEY,

    medicine_id UUID           NOT NULL,

    old_price   NUMERIC(12, 2),
    new_price   NUMERIC(12, 2) NOT NULL,

    reason      TEXT,

    changed_at  TIMESTAMP      NOT NULL,
    changed_by  VARCHAR(100),

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),

    CONSTRAINT fk_price_history_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE INDEX idx_price_history_medicine ON medicine_price_histories (medicine_id);
CREATE INDEX idx_price_history_changed_at ON medicine_price_histories (changed_at);
