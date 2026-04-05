-- =========================
-- FOLLOW-UP APPOINTMENT
-- =========================

ALTER TABLE appointments
    ADD COLUMN parent_id UUID NULL,
    ADD COLUMN sequence_no INT NULL,
    ADD COLUMN actual_date DATE NULL;

UPDATE appointments
SET sequence_no = 1
WHERE sequence_no IS NULL;

ALTER TABLE appointments
    ALTER COLUMN sequence_no SET NOT NULL;

ALTER TABLE appointments
    ALTER COLUMN sequence_no SET DEFAULT 1;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appt_parent
        FOREIGN KEY (parent_id) REFERENCES appointments (id);

CREATE INDEX idx_appt_parent ON appointments (parent_id);
CREATE INDEX idx_appt_sequence ON appointments (sequence_no);
CREATE INDEX idx_appt_actual_date ON appointments (actual_date);