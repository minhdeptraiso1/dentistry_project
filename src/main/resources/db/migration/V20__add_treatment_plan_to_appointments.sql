ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS treatment_plan_id UUID;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_treatment_plan
        FOREIGN KEY (treatment_plan_id)
            REFERENCES treatment_plans(id);