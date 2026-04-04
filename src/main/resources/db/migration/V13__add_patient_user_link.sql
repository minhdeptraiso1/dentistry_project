ALTER TABLE users
    ADD COLUMN patient_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id);

CREATE INDEX idx_users_patient ON users (patient_id);