ALTER TABLE invoices
    ADD COLUMN prescription_id UUID;

ALTER TABLE invoices
    ADD CONSTRAINT fk_invoice_prescription
        FOREIGN KEY (prescription_id) REFERENCES prescriptions (id);

CREATE INDEX idx_invoice_prescription ON invoices (prescription_id);
