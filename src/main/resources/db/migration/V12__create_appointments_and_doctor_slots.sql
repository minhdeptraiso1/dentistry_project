-- =========================
-- DOCTOR SLOT CAPACITY (per day + shift)
-- =========================
CREATE TABLE doctor_shift_capacities
(
    id           UUID PRIMARY KEY,

    doctor_id    UUID        NOT NULL,
    work_date    DATE        NOT NULL,
    shift        VARCHAR(20) NOT NULL, -- MORNING / AFTERNOON
    max_patients INT         NOT NULL,

    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100),

    CONSTRAINT fk_dsc_doctor
        FOREIGN KEY (doctor_id) REFERENCES users (id),

    CONSTRAINT uq_dsc UNIQUE (doctor_id, work_date, shift)
);

CREATE INDEX idx_dsc_doctor_date ON doctor_shift_capacities (doctor_id, work_date);
CREATE INDEX idx_dsc_date_shift ON doctor_shift_capacities (work_date, shift);

-- =========================
-- APPOINTMENTS (phiếu khám)
-- =========================
CREATE TABLE appointments
(
    id               UUID PRIMARY KEY,

    appointment_code VARCHAR(20) NOT NULL UNIQUE,           -- PK000001
    patient_id       UUID        NOT NULL,

    doctor_id        UUID,                                  -- nullable when not assigned
    work_date        DATE        NOT NULL,
    shift            VARCHAR(20) NOT NULL,                  -- MORNING / AFTERNOON

    status           VARCHAR(20) NOT NULL,                  -- WAITING/ASSIGNED/IN_PROGRESS/DONE/CANCELLED
    priority         VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- NORMAL/URGENT
    note             TEXT,

    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    deleted_at       TIMESTAMP,
    deleted_by       VARCHAR(100),

    CONSTRAINT fk_appt_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),

    CONSTRAINT fk_appt_doctor
        FOREIGN KEY (doctor_id) REFERENCES users (id)
);

CREATE INDEX idx_appt_code ON appointments (appointment_code);
CREATE INDEX idx_appt_date_shift ON appointments (work_date, shift);
CREATE INDEX idx_appt_doctor_date ON appointments (doctor_id, work_date);
CREATE INDEX idx_appt_status ON appointments (status);