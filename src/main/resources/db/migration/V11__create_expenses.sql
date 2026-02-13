CREATE TABLE expenses
(
    id           UUID PRIMARY KEY,

    expense_code VARCHAR(20)    NOT NULL UNIQUE, -- CP000001
    category     VARCHAR(50)    NOT NULL,        -- RENT, SALARY, ELECTRIC, WATER, SUPPLIES, OTHER
    name         VARCHAR(255)   NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    expense_date DATE           NOT NULL,
    note         TEXT,

    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100)
);

CREATE INDEX idx_expenses_date ON expenses (expense_date);
CREATE INDEX idx_expenses_category ON expenses (category);
