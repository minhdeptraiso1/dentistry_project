CREATE TABLE doctor_schedule_requests
(
    id           UUID PRIMARY KEY,

    doctor_id    UUID        NOT NULL,
    work_date    DATE        NOT NULL,
    shift        VARCHAR(20) NOT NULL,
    max_patients INT         NOT NULL,

    status       VARCHAR(20) NOT NULL, -- PENDING / APPROVED / REJECTED

    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100),

    CONSTRAINT fk_dsr_doctor
        FOREIGN KEY (doctor_id) REFERENCES users (id)
);

CREATE INDEX idx_dsr_doctor ON doctor_schedule_requests (doctor_id);
CREATE INDEX idx_dsr_date_shift ON doctor_schedule_requests (work_date, shift);