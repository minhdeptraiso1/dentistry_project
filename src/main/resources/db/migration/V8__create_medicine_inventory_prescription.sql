-- =========================
-- MEDICINES (catalog)
-- =========================
CREATE TABLE medicines
(
    id          UUID PRIMARY KEY,

    code        VARCHAR(20)  NOT NULL UNIQUE, -- TH000001
    name        VARCHAR(255) NOT NULL,
    ingredient  TEXT,                         -- hoạt chất/thành phần
    unit        VARCHAR(30),                  -- viên / vỉ / chai / ống
    usage_guide TEXT,                         -- hướng dẫn sử dụng

    active      BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100)
);

CREATE INDEX idx_medicines_code ON medicines (code);
CREATE INDEX idx_medicines_name ON medicines (name);
CREATE INDEX idx_medicines_active ON medicines (active);

-- =========================
-- MEDICINE BATCHES (import lots)
-- =========================
CREATE TABLE medicine_batches
(
    id                 UUID PRIMARY KEY,

    medicine_id        UUID           NOT NULL,
    batch_no           VARCHAR(50), -- số lô (optional)
    import_date        DATE           NOT NULL,
    expiry_date        DATE,
    import_price       NUMERIC(12, 2) NOT NULL DEFAULT 0,

    quantity_in        INT            NOT NULL,
    quantity_remaining INT            NOT NULL,

    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    deleted_at         TIMESTAMP,
    deleted_by         VARCHAR(100),

    CONSTRAINT fk_batch_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE INDEX idx_batches_medicine ON medicine_batches (medicine_id);
CREATE INDEX idx_batches_expiry ON medicine_batches (expiry_date);
CREATE INDEX idx_batches_remaining ON medicine_batches (quantity_remaining);

-- =========================
-- PRESCRIPTIONS
-- =========================
CREATE TABLE prescriptions
(
    id                UUID PRIMARY KEY,

    prescription_code VARCHAR(20) NOT NULL UNIQUE, -- DT000001
    medical_record_id UUID        NOT NULL,
    patient_id        UUID        NOT NULL,
    doctor_id         UUID        NOT NULL,

    status            VARCHAR(20) NOT NULL,        -- DRAFT/ISSUED/DISPENSED/CANCELLED
    note              TEXT,

    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(100),

    CONSTRAINT fk_rx_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_records (id),
    CONSTRAINT fk_rx_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_rx_doctor
        FOREIGN KEY (doctor_id) REFERENCES users (id)
);

CREATE INDEX idx_rx_code ON prescriptions (prescription_code);
CREATE INDEX idx_rx_patient ON prescriptions (patient_id);
CREATE INDEX idx_rx_status ON prescriptions (status);

-- =========================
-- PRESCRIPTION ITEMS
-- =========================
CREATE TABLE prescription_items
(
    id              UUID PRIMARY KEY,

    prescription_id UUID         NOT NULL,
    medicine_id     UUID         NOT NULL,

    medicine_code   VARCHAR(20),
    medicine_name   VARCHAR(255) NOT NULL, -- snapshot
    unit            VARCHAR(30),

    dosage          TEXT,                  -- liều dùng: 2v/ngày, sáng tối...
    quantity        INT          NOT NULL,

    note            TEXT,

    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),

    CONSTRAINT fk_rx_item_rx
        FOREIGN KEY (prescription_id) REFERENCES prescriptions (id),
    CONSTRAINT fk_rx_item_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE INDEX idx_rx_items_rx ON prescription_items (prescription_id);
CREATE INDEX idx_rx_items_medicine ON prescription_items (medicine_id);

-- =========================
-- DISPENSE LOG (trace batch deduction)
-- =========================
CREATE TABLE dispense_logs
(
    id                   UUID PRIMARY KEY,

    prescription_id      UUID      NOT NULL,
    prescription_item_id UUID      NOT NULL,

    medicine_id          UUID      NOT NULL,
    batch_id             UUID      NOT NULL,

    quantity             INT       NOT NULL,
    dispensed_at         TIMESTAMP NOT NULL,

    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    deleted_at           TIMESTAMP,
    deleted_by           VARCHAR(100),

    CONSTRAINT fk_dispense_rx
        FOREIGN KEY (prescription_id) REFERENCES prescriptions (id),
    CONSTRAINT fk_dispense_rx_item
        FOREIGN KEY (prescription_item_id) REFERENCES prescription_items (id),
    CONSTRAINT fk_dispense_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_dispense_batch
        FOREIGN KEY (batch_id) REFERENCES medicine_batches (id)
);

CREATE INDEX idx_dispense_rx ON dispense_logs (prescription_id);
CREATE INDEX idx_dispense_batch ON dispense_logs (batch_id);
