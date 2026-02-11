-- =========================
-- SERVICE CATALOG (single or package)
-- =========================
CREATE TABLE services (
    id UUID PRIMARY KEY,

    code        VARCHAR(30)  NOT NULL UNIQUE,  -- DV000001 / PK000001
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(20)  NOT NULL,         -- SINGLE / PACKAGE
    category    VARCHAR(50),                   -- TRAM_RANG / NHO_RANG / ...
    description TEXT,

    base_price  NUMERIC(12,2) NOT NULL DEFAULT 0, -- giá niêm yết
    unit        VARCHAR(30),                      -- lần / răng / hàm / ca
    duration_min INT,                             -- phút dự kiến

    active      BOOLEAN NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100)
);

CREATE INDEX idx_services_code ON services(code);
CREATE INDEX idx_services_name ON services(name);
CREATE INDEX idx_services_type ON services(type);
CREATE INDEX idx_services_active ON services(active);

-- =========================
-- PACKAGE STEPS (lộ trình)
-- =========================
CREATE TABLE service_package_steps (
    id UUID PRIMARY KEY,

    service_id  UUID NOT NULL,                 -- trỏ tới services(type=PACKAGE)
    step_no     INT  NOT NULL,                 -- thứ tự bước
    step_name   VARCHAR(255) NOT NULL,
    step_desc   TEXT,

    price       NUMERIC(12,2) NOT NULL DEFAULT 0,
    quantity    INT NOT NULL DEFAULT 1,        -- số lần/số buổi

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),

    CONSTRAINT fk_pkg_steps_service
        FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE INDEX idx_pkg_steps_service ON service_package_steps(service_id);
CREATE INDEX idx_pkg_steps_step_no ON service_package_steps(step_no);
