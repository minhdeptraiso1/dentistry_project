ALTER TABLE appointments
    ADD COLUMN reminder_today_sent BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN reminder_today_sent_at TIMESTAMP NULL,
ADD COLUMN reminder_tomorrow_sent BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN reminder_tomorrow_sent_at TIMESTAMP NULL;