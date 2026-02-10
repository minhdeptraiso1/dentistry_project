CREATE TABLE patients
(
    id           UUID PRIMARY KEY,

    patient_code VARCHAR(20)  NOT NULL UNIQUE,
    full_name    VARCHAR(255) NOT NULL,
    gender       VARCHAR(10),
    phone        VARCHAR(20),
    dob          DATE,
    address      VARCHAR(500),
    note         TEXT,

    created_at   TIMESTAMP,
    created_by   VARCHAR(100),

    updated_at   TIMESTAMP,
    updated_by   VARCHAR(100),

    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100)
);


CREATE INDEX idx_patients_code ON patients (patient_code);
CREATE INDEX idx_patients_phone ON patients (phone);
CREATE INDEX idx_patients_name ON patients (full_name);
