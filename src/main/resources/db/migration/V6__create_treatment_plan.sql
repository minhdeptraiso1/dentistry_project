-- =========================
-- TREATMENT PLANS
-- =========================
CREATE TABLE treatment_plans
(
    id                UUID PRIMARY KEY,

    plan_code         VARCHAR(20)    NOT NULL UNIQUE, -- KH000001
    medical_record_id UUID           NOT NULL,
    patient_id        UUID           NOT NULL,
    doctor_id         UUID           NOT NULL,

    status            VARCHAR(20)    NOT NULL,        -- DRAFT/APPROVED/IN_PROGRESS/DONE/CANCELLED
    note              TEXT,

    total_amount      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    final_amount      NUMERIC(12, 2) NOT NULL DEFAULT 0,

    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    deleted_at        TIMESTAMP,
    deleted_by        VARCHAR(100),

    CONSTRAINT fk_tp_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_records (id),
    CONSTRAINT fk_tp_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_tp_doctor
        FOREIGN KEY (doctor_id) REFERENCES users (id)
);

CREATE INDEX idx_tp_code ON treatment_plans (plan_code);
CREATE INDEX idx_tp_medical_record ON treatment_plans (medical_record_id);
CREATE INDEX idx_tp_patient ON treatment_plans (patient_id);
CREATE INDEX idx_tp_doctor ON treatment_plans (doctor_id);
CREATE INDEX idx_tp_status ON treatment_plans (status);

-- =========================
-- TREATMENT ITEMS
-- =========================
CREATE TABLE treatment_items
(
    id              UUID PRIMARY KEY,

    plan_id         UUID           NOT NULL,
    service_id      UUID           NOT NULL,

    item_name       VARCHAR(255)   NOT NULL, -- snapshot name
    service_code    VARCHAR(30),             -- snapshot code
    service_type    VARCHAR(20),             -- snapshot type SINGLE/PACKAGE

    quantity        INT            NOT NULL DEFAULT 1,
    unit_price      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    line_total      NUMERIC(12, 2) NOT NULL DEFAULT 0,

    tooth_no        VARCHAR(10),             -- răng số mấy (vd: 18, 11, 24...)
    tooth_surface   VARCHAR(20),             -- mặt nhai/xa/gần... (tuỳ)
    status          VARCHAR(20)    NOT NULL, -- PLANNED/DONE/CANCELLED
    note            TEXT,

    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),

    CONSTRAINT fk_ti_plan
        FOREIGN KEY (plan_id) REFERENCES treatment_plans (id),
    CONSTRAINT fk_ti_service
        FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE INDEX idx_ti_plan ON treatment_items (plan_id);
CREATE INDEX idx_ti_service ON treatment_items (service_id);
CREATE INDEX idx_ti_status ON treatment_items (status);
