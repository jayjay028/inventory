-- ============================================================
-- Inventory System Database Schema
-- Database: inventory_db
-- Engine: MySQL 8.x
-- Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================

CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE inventory_db;

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NULL,
    role            ENUM('ADMIN','STAFF') NOT NULL,
    access_rights   BIGINT          NOT NULL DEFAULT 0,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    last_login      DATETIME        NULL,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_active ON users (active);
CREATE INDEX idx_users_role ON users (role);

-- ============================================================
-- 2. CATEGORIES
-- ============================================================
CREATE TABLE categories (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255)    NULL,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_categories_active ON categories (active);

-- ============================================================
-- 3. ITEMS
-- ============================================================
CREATE TABLE items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    item_code       VARCHAR(50)     NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    description     TEXT            NULL,
    category_id     BIGINT          NOT NULL,
    unit            VARCHAR(30)     NOT NULL,
    price           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    cost_price      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    reorder_level   INT             NOT NULL DEFAULT 0,
    taxable         TINYINT(1)      NOT NULL DEFAULT 1,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_items_item_code UNIQUE (item_code),
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_items_name ON items (name);
CREATE INDEX idx_items_category_id ON items (category_id);
CREATE INDEX idx_items_active ON items (active);

-- ============================================================
-- 4. CUSTOMERS
-- ============================================================
CREATE TABLE customers (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    tin             VARCHAR(20)     NULL,
    address         VARCHAR(500)    NULL,
    contact_person  VARCHAR(150)    NULL,
    contact_number  VARCHAR(20)     NULL,
    email           VARCHAR(150)    NULL,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_customers_name ON customers (name);
CREATE INDEX idx_customers_tin ON customers (tin);
CREATE INDEX idx_customers_active ON customers (active);

-- ============================================================
-- 5. SUPPLIERS
-- ============================================================
CREATE TABLE suppliers (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    tin             VARCHAR(20)     NULL,
    address         VARCHAR(500)    NULL,
    contact_person  VARCHAR(150)    NULL,
    contact_number  VARCHAR(20)     NULL,
    email           VARCHAR(150)    NULL,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_suppliers_name ON suppliers (name);
CREATE INDEX idx_suppliers_tin ON suppliers (tin);
CREATE INDEX idx_suppliers_active ON suppliers (active);

-- ============================================================
-- 6. STOCK
-- ============================================================
CREATE TABLE stock (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    item_id         BIGINT          NOT NULL,
    quantity_on_hand INT            NOT NULL DEFAULT 0,
    last_updated    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_stock_item_id UNIQUE (item_id),
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. STOCK_TRANSACTIONS
-- ============================================================
CREATE TABLE stock_transactions (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    item_id             BIGINT          NOT NULL,
    transaction_type    ENUM('IN','OUT','ADJUSTMENT') NOT NULL,
    status              ENUM('CREATED','APPROVED','CANCELLED') NOT NULL DEFAULT 'CREATED',
    quantity            INT             NOT NULL,
    unit_cost           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    unit_price          DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    discount_type       ENUM('NONE','FIXED','PERCENTAGE') NOT NULL DEFAULT 'NONE',
    discount_value      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    discount_amount     DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    subtotal            DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    net_amount          DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    tax_enabled         TINYINT(1)      NOT NULL DEFAULT 0,
    tax_type            ENUM('VAT','NON_VAT','EXEMPT','ZERO_RATED') NULL,
    tax_rate            DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
    tax_amount          DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    vatable_amount      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    total_amount        DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    customer_id         BIGINT          NULL,
    supplier_id         BIGINT          NULL,
    document_type       ENUM('OR','SI','DR','PO','RR','NONE') NOT NULL DEFAULT 'NONE',
    document_no         VARCHAR(50)     NULL,
    reference_no        VARCHAR(50)     NULL,
    remarks             VARCHAR(500)    NULL,
    transaction_date    DATETIME        NOT NULL,
    created_by          VARCHAR(50)     NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by         VARCHAR(50)     NULL,
    approved_at         DATETIME        NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_st_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_st_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_st_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_st_item_id ON stock_transactions (item_id);
CREATE INDEX idx_st_transaction_type ON stock_transactions (transaction_type);
CREATE INDEX idx_st_status ON stock_transactions (status);
CREATE INDEX idx_st_transaction_date ON stock_transactions (transaction_date);
CREATE INDEX idx_st_reference_no ON stock_transactions (reference_no);
CREATE INDEX idx_st_document_no ON stock_transactions (document_no);
CREATE INDEX idx_st_customer_id ON stock_transactions (customer_id);
CREATE INDEX idx_st_supplier_id ON stock_transactions (supplier_id);
CREATE INDEX idx_st_created_by ON stock_transactions (created_by);

-- ============================================================
-- 8. TRANSACTION_ADDONS
-- ============================================================
CREATE TABLE transaction_addons (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    transaction_id  BIGINT          NOT NULL,
    addon_name      VARCHAR(100)    NOT NULL,
    amount          DECIMAL(12,2)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ta_transaction FOREIGN KEY (transaction_id) REFERENCES stock_transactions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ta_transaction_id ON transaction_addons (transaction_id);

-- ============================================================
-- 9. ADDON_MASTER
-- ============================================================
CREATE TABLE addon_master (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    default_amount  DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_addon_master_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. AUDIT_TRAIL
-- ============================================================
CREATE TABLE audit_trail (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    entity_name     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT          NOT NULL,
    action          ENUM('CREATE','UPDATE','DELETE') NOT NULL,
    field_name      VARCHAR(100)    NULL,
    old_value       TEXT            NULL,
    new_value       TEXT            NULL,
    performed_by    VARCHAR(50)     NOT NULL,
    performed_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address      VARCHAR(45)     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_entity ON audit_trail (entity_name, entity_id);
CREATE INDEX idx_audit_performed_by ON audit_trail (performed_by);
CREATE INDEX idx_audit_performed_at ON audit_trail (performed_at);
CREATE INDEX idx_audit_action ON audit_trail (action);

-- ============================================================
-- 11. APP_SETTINGS
-- ============================================================
CREATE TABLE app_settings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    setting_key     VARCHAR(100)    NOT NULL,
    setting_value   TEXT            NULL,
    description     VARCHAR(255)    NULL,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      VARCHAR(50)     NOT NULL,
    updated_by      VARCHAR(50)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_settings_key UNIQUE (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_app_settings_active ON app_settings (active);

-- ============================================================
-- 12. SHIFTS
-- ============================================================
CREATE TABLE shifts (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    cashier             VARCHAR(50)     NOT NULL,
    opening_amount      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    closing_amount      DECIMAL(12,2)   NULL,
    expected_amount     DECIMAL(12,2)   NULL,
    difference          DECIMAL(12,2)   NULL,
    total_sales         DECIMAL(12,2)   NULL,
    total_transactions  INT             NULL,
    total_voided        DECIMAL(12,2)   NULL,
    total_returns       DECIMAL(12,2)   NULL,
    status              ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
    opened_at           DATETIME        NOT NULL,
    closed_at           DATETIME        NULL,
    remarks             VARCHAR(500)    NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_shifts_cashier ON shifts (cashier);
CREATE INDEX idx_shifts_status ON shifts (status);
CREATE INDEX idx_shifts_opened_at ON shifts (opened_at);

-- ============================================================
-- 13. SALES
-- ============================================================
CREATE TABLE sales (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    sale_no         VARCHAR(50)     NOT NULL,
    customer_id     BIGINT          NULL,
    shift_id        BIGINT          NULL,
    subtotal        DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    discount_type   ENUM('NONE','FIXED','PERCENTAGE','SENIOR_PWD') NOT NULL DEFAULT 'NONE',
    discount_value  DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    addons_total    DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    net_amount      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    tax_enabled     TINYINT(1)      NOT NULL DEFAULT 0,
    tax_type        ENUM('VAT','NON_VAT','EXEMPT','ZERO_RATED') NULL,
    tax_rate        DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    vatable_amount  DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    amount_tendered DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    change_amount   DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    payment_method  ENUM('CASH','GCASH','BANK_TRANSFER','CREDIT','MULTIPLE') NOT NULL DEFAULT 'CASH',
    status          ENUM('OPEN','PAID','CLOSED','VOIDED') NOT NULL DEFAULT 'OPEN',
    void_reason     VARCHAR(500)    NULL,
    voided_by       VARCHAR(50)     NULL,
    voided_at       DATETIME        NULL,
    document_type   ENUM('OR','SI','NONE') NOT NULL DEFAULT 'OR',
    document_no     VARCHAR(50)     NULL,
    remarks         VARCHAR(500)    NULL,
    sale_date       DATETIME        NOT NULL,
    created_by      VARCHAR(50)     NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_sales_sale_no UNIQUE (sale_no),
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_sales_shift FOREIGN KEY (shift_id) REFERENCES shifts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sales_customer_id ON sales (customer_id);
CREATE INDEX idx_sales_shift_id ON sales (shift_id);
CREATE INDEX idx_sales_sale_date ON sales (sale_date);
CREATE INDEX idx_sales_status ON sales (status);
CREATE INDEX idx_sales_payment_method ON sales (payment_method);
CREATE INDEX idx_sales_created_by ON sales (created_by);
CREATE INDEX idx_sales_document_no ON sales (document_no);

-- ============================================================
-- 14. SALE_ITEMS
-- ============================================================
CREATE TABLE sale_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    sale_id         BIGINT          NOT NULL,
    item_id         BIGINT          NOT NULL,
    item_name       VARCHAR(200)    NOT NULL,
    item_code       VARCHAR(50)     NOT NULL,
    quantity        INT             NOT NULL,
    unit_price      DECIMAL(12,2)   NOT NULL,
    unit_cost       DECIMAL(12,2)   NOT NULL,
    discount_type   ENUM('NONE','FIXED','PERCENTAGE') NOT NULL DEFAULT 'NONE',
    discount_value  DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    line_total      DECIMAL(12,2)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_si_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
    CONSTRAINT fk_si_item FOREIGN KEY (item_id) REFERENCES items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_si_sale_id ON sale_items (sale_id);
CREATE INDEX idx_si_item_id ON sale_items (item_id);

-- ============================================================
-- 15. SALE_ADDONS
-- ============================================================
CREATE TABLE sale_addons (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    sale_id         BIGINT          NOT NULL,
    addon_name      VARCHAR(100)    NOT NULL,
    amount          DECIMAL(12,2)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sa_sale FOREIGN KEY (sale_id) REFERENCES sales (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sa_sale_id ON sale_addons (sale_id);

-- ============================================================
-- 16. SALE_PAYMENTS
-- ============================================================
CREATE TABLE sale_payments (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    sale_id         BIGINT          NOT NULL,
    payment_method  ENUM('CASH','GCASH','BANK_TRANSFER','CREDIT') NOT NULL,
    amount          DECIMAL(12,2)   NOT NULL,
    reference_no    VARCHAR(100)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sp_sale FOREIGN KEY (sale_id) REFERENCES sales (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sp_sale_id ON sale_payments (sale_id);

-- ============================================================
-- END OF SCHEMA
-- ============================================================
