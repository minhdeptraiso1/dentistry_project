CREATE TABLE medical_records
(
    id          UUID PRIMARY KEY,

    record_code VARCHAR(20) NOT NULL UNIQUE, -- HS000001
    patient_id  UUID        NOT NULL,
    doctor_id   UUID        NOT NULL,

    visit_date  TIMESTAMP   NOT NULL,        -- ngày khám
    symptom     TEXT,                        -- triệu chứng
    diagnosis   TEXT,                        -- chẩn đoán
    note        TEXT,                        -- ghi chú

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),

    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),

    CONSTRAINT fk_medical_records_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),

    CONSTRAINT fk_medical_records_doctor
        FOREIGN KEY (doctor_id) REFERENCES users (id)
);

-- =========================
-- INDEX FOR SEARCH
-- =========================
CREATE INDEX idx_medical_records_code ON medical_records (record_code);
CREATE INDEX idx_medical_records_patient ON medical_records (patient_id);
CREATE INDEX idx_medical_records_doctor ON medical_records (doctor_id);
CREATE INDEX idx_medical_records_visit_date ON medical_records (visit_date);
