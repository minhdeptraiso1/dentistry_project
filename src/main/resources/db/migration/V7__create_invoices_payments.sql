-- =========================
-- INVOICES
-- =========================
CREATE TABLE invoices
(
    id                UUID PRIMARY KEY,

    invoice_code      VARCHAR(20)    NOT NULL UNIQUE, -- HD000001
    patient_id        UUID           NOT NULL,
    cashier_id        UUID,                           -- user(CASHIER/ADMIN)
    treatment_plan_id UUID,                           -- optional

    status            VARCHAR(20)    NOT NULL,        -- DRAFT/ISSUED/PARTIALLY_PAID/PAID/CANCELLED
    note              TEXT,

    subtotal          NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    paid_amount       NUMERIC(12, 2) NOT NULL DEFAULT 0,

    issued_at         TIMESTAMP,                      -- lúc chốt hóa đơn
    paid_at           TIMESTAMP,                      -- lúc thanh toán đủ

    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(100),

    CONSTRAINT fk_invoice_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),

    CONSTRAINT fk_invoice_cashier
        FOREIGN KEY (cashier_id) REFERENCES users (id),

    CONSTRAINT fk_invoice_treatment_plan
        FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plans (id)
);

CREATE INDEX idx_invoice_code ON invoices (invoice_code);
CREATE INDEX idx_invoice_patient ON invoices (patient_id);
CREATE INDEX idx_invoice_status ON invoices (status);

-- =========================
-- INVOICE ITEMS
-- =========================
CREATE TABLE invoice_items
(
    id              UUID PRIMARY KEY,

    invoice_id      UUID           NOT NULL,

    service_id      UUID, -- optional: service catalog (snapshot)
    item_name       VARCHAR(255)   NOT NULL,
    service_code    VARCHAR(30),
    service_type    VARCHAR(20),

    quantity        INT            NOT NULL DEFAULT 1,
    unit_price      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    line_total      NUMERIC(12, 2) NOT NULL DEFAULT 0,

    note            TEXT,

    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),

    CONSTRAINT fk_invoice_item_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (id),

    CONSTRAINT fk_invoice_item_service
        FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE INDEX idx_invoice_items_invoice ON invoice_items (invoice_id);

-- =========================
-- PAYMENTS
-- =========================
CREATE TABLE payments
(
    id         UUID PRIMARY KEY,

    invoice_id UUID           NOT NULL,
    method     VARCHAR(20)    NOT NULL, -- CASH/TRANSFER/CARD
    amount     NUMERIC(12, 2) NOT NULL,
    paid_at    TIMESTAMP      NOT NULL,
    reference  VARCHAR(100),            -- mã giao dịch/chuyển khoản (optional)
    note       TEXT,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (id)
);

CREATE INDEX idx_payments_invoice ON payments (invoice_id);
CREATE INDEX idx_payments_paid_at ON payments (paid_at);
