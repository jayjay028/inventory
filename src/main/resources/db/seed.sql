-- ============================================================
-- Inventory System Seed Data
-- Database: inventory_db
-- ============================================================

USE inventory_db;

-- ============================================================
-- DEFAULT ADMIN USER
-- Password: admin123 (BCrypt encoded)
-- ============================================================
INSERT INTO users (username, password, full_name, email, role, access_rights, active, created_by)
VALUES (
    'admin',
    '$2a$10$nMSFToLukmFtMWEY0FKXTeFdB7TdTrWfom8fPLzToEpSFkeK.017u',
    'System Administrator',
    NULL,
    'ADMIN',
    33554431,
    1,
    'system'
);

-- ============================================================
-- APP SETTINGS - Company Information
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('company.name', 'My Company', 'Company name displayed in reports and receipts', 1, 'system'),
('company.address', '', 'Company address', 1, 'system'),
('company.contact_number', '', 'Company contact number', 1, 'system'),
('company.email', '', 'Company email address', 1, 'system'),
('company.tin', '', 'Company TIN (Tax Identification Number)', 1, 'system'),
('company.logo_path', '', 'Path to company logo image', 1, 'system');

-- ============================================================
-- APP SETTINGS - Tax Settings
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('tax.enabled', 'false', 'Enable tax computation globally', 1, 'system'),
('tax.type', 'VAT', 'Default tax type (VAT, NON_VAT, EXEMPT, ZERO_RATED)', 1, 'system'),
('tax.rate', '12.00', 'Default tax rate percentage', 1, 'system'),
('tax.inclusive', 'true', 'Whether prices are tax-inclusive', 1, 'system');

-- ============================================================
-- APP SETTINGS - Item Settings
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('item.code_prefix', 'ITM', 'Prefix for auto-generated item codes', 1, 'system'),
('item.code_auto_generate', 'true', 'Auto-generate item codes', 1, 'system'),
('item.default_unit', 'PCS', 'Default unit of measure for new items', 1, 'system'),
('item.track_cost_price', 'true', 'Track cost price for items', 1, 'system');

-- ============================================================
-- APP SETTINGS - Document Numbering
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('document.or_prefix', 'OR', 'Official Receipt number prefix', 1, 'system'),
('document.or_next_number', '1', 'Next Official Receipt number', 1, 'system'),
('document.si_prefix', 'SI', 'Sales Invoice number prefix', 1, 'system'),
('document.si_next_number', '1', 'Next Sales Invoice number', 1, 'system'),
('document.dr_prefix', 'DR', 'Delivery Receipt number prefix', 1, 'system'),
('document.dr_next_number', '1', 'Next Delivery Receipt number', 1, 'system'),
('document.po_prefix', 'PO', 'Purchase Order number prefix', 1, 'system'),
('document.po_next_number', '1', 'Next Purchase Order number', 1, 'system'),
('document.rr_prefix', 'RR', 'Receiving Report number prefix', 1, 'system'),
('document.rr_next_number', '1', 'Next Receiving Report number', 1, 'system'),
('document.sale_prefix', 'SL', 'Sale number prefix', 1, 'system'),
('document.sale_next_number', '1', 'Next Sale number', 1, 'system'),
('document.number_padding', '6', 'Zero-padding length for document numbers', 1, 'system');

-- ============================================================
-- APP SETTINGS - Discount Settings
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('discount.senior_pwd_rate', '20.00', 'Senior Citizen / PWD discount rate percentage', 1, 'system'),
('discount.allow_line_discount', 'true', 'Allow discount per line item', 1, 'system'),
('discount.allow_transaction_discount', 'true', 'Allow discount on entire transaction', 1, 'system');

-- ============================================================
-- APP SETTINGS - General Settings
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('general.date_format', 'yyyy-MM-dd', 'System date format', 1, 'system'),
('general.datetime_format', 'yyyy-MM-dd HH:mm:ss', 'System datetime format', 1, 'system'),
('general.timezone', 'Asia/Manila', 'System timezone', 1, 'system'),
('general.currency', 'PHP', 'System currency code', 1, 'system'),
('general.currency_symbol', '₱', 'Currency symbol displayed in UI', 1, 'system'),
('general.pagination_size', '20', 'Default page size for list queries', 1, 'system');

-- ============================================================
-- APP SETTINGS - POS Settings
-- ============================================================
INSERT INTO app_settings (setting_key, setting_value, description, active, created_by) VALUES
('pos.enabled', 'true', 'Enable POS module', 1, 'system'),
('pos.require_shift', 'true', 'Require open shift before making sales', 1, 'system'),
('pos.auto_print_receipt', 'false', 'Auto-print receipt after sale', 1, 'system'),
('pos.allow_void', 'true', 'Allow voiding of sales', 1, 'system'),
('pos.void_requires_admin', 'true', 'Void operation requires admin approval', 1, 'system'),
('pos.default_payment_method', 'CASH', 'Default payment method for POS sales', 1, 'system'),
('pos.receipt_footer', 'Thank you for your purchase!', 'Text displayed at the bottom of receipts', 1, 'system');

-- ============================================================
-- END OF SEED DATA
-- ============================================================
