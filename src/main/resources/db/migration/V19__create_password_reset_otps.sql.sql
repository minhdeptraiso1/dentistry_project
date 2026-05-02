CREATE TABLE password_reset_otps (
                                     id UUID PRIMARY KEY,
                                     user_id UUID NOT NULL REFERENCES users(id),
                                     otp_code VARCHAR(10) NOT NULL,
                                     purpose VARCHAR(30) NOT NULL,
                                     expired_at TIMESTAMP NOT NULL,
                                     used BOOLEAN NOT NULL DEFAULT FALSE,
                                     attempt_count INT NOT NULL DEFAULT 0,
                                     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMP NULL,
                                     created_by VARCHAR(255) NULL,
                                     updated_by VARCHAR(255) NULL,
                                     deleted_at TIMESTAMP NULL,
                                     deleted_by VARCHAR(255) NULL
);

CREATE INDEX idx_password_reset_otps_user_id
    ON password_reset_otps(user_id);

CREATE INDEX idx_password_reset_otps_expired_at
    ON password_reset_otps(expired_at);

CREATE INDEX idx_password_reset_otps_user_purpose_used
    ON password_reset_otps(user_id, purpose, used);