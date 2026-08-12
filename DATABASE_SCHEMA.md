# Inventory System - Database Schema

## Database: `inventory_db`
## Engine: MySQL 8.0
## Character Set: utf8mb4
## Collation: utf8mb4_unicode_ci
## Locale: Philippines (₱ PHP, Asia/Manila UTC+8)

---

## Entity Relationship Diagram (Text)

```
users
  │
  ├──< audit_trail (performed_by)
  │
categories
  │
  ├──< items (category_id)
        │
        ├──< stock (item_id) [1:1]
        │
        ├──< stock_transactions (item_id)
        │     │
        │     ├──< transaction_addons (transaction_id)
        │     ├── customers (customer_id) [optional, for OUT]
        │     └── suppliers (supplier_id) [optional, for IN]
        │
        ├──< sale_items (item_id)
              │
              └── sales (sale_id)
                    │
                    ├──< sale_items (sale_id)
                    ├──< sale_addons (sale_id)
                    ├──< sale_payments (sale_id)
                    ├── customers (customer_id) [optional]
                    └── shifts (shift_id) [optional]

shifts (cashier shift management)

customers (standalone master)

suppliers (standalone master)

addon_master (standalone reference)

app_settings (standalone config)
```

---

## Tables

### Standard Audit Fields

All master data tables include these audit columns (unless noted otherwise):

| Column | Type | Description |
|--------|------|-------------|
| created_by | VARCHAR(50) | Username who created the record |
| created_at | DATETIME | When the record was created |
| updated_by | VARCHAR(50) | Username who last modified |
| updated_at | DATETIME | When the record was last modified |

These are automatically populated via a JPA `@MappedSuperclass` (BaseEntity) or `@EntityListeners`.

---

### Bitwise Access Rights (Permissions)

The `users.access_rights` column stores permissions as a bitmask (BIGINT). Each permission is a power of 2:

| Permission | Bit | Value | Description |
|-----------|-----|-------|-------------|
| VIEW_DASHBOARD | 0 | 1 | View dashboard |
| VIEW_ITEMS | 1 | 2 | View item list |
| MANAGE_ITEMS | 2 | 4 | Create/edit/deactivate items |
| VIEW_CATEGORIES | 3 | 8 | View categories |
| MANAGE_CATEGORIES | 4 | 16 | Create/edit/deactivate categories |
| VIEW_CUSTOMERS | 5 | 32 | View customers |
| MANAGE_CUSTOMERS | 6 | 64 | Create/edit/deactivate customers |
| VIEW_SUPPLIERS | 7 | 128 | View suppliers |
| MANAGE_SUPPLIERS | 8 | 256 | Create/edit/deactivate suppliers |
| VIEW_STOCK | 9 | 512 | View stock levels |
| MANAGE_STOCK_IN | 10 | 1024 | Perform stock in |
| MANAGE_STOCK_OUT | 11 | 2048 | Perform stock out |
| MANAGE_STOCK_ADJ | 12 | 4096 | Perform stock adjustment |
| VIEW_TRANSACTIONS | 13 | 8192 | View transaction history |
| USE_POS | 14 | 16384 | Use POS terminal |
| VOID_SALES | 15 | 32768 | Void POS sales |
| MANAGE_SHIFTS | 16 | 65536 | Open/close shifts |
| VIEW_REPORTS | 17 | 131072 | Generate/view reports |
| VIEW_AUDIT_TRAIL | 18 | 262144 | View audit trail |
| MANAGE_USERS | 19 | 524288 | Manage user accounts |
| MANAGE_SETTINGS | 20 | 1048576 | Modify app settings |
| MANAGE_ADDONS | 21 | 2097152 | Manage add-on master |
| APPROVE_TRANSACTIONS | 22 | 4194304 | Approve stock transactions (CREATED → APPROVED) |
| CANCEL_TRANSACTIONS | 23 | 8388608 | Cancel stock transactions (CREATED → CANCELLED) |
| REPRINT | 24 | 16777216 | Reprint transactions and receipts |

**Role Presets:**
| Role | access_rights | Permissions |
|------|--------------|-------------|
| ADMIN | 33554431 | All permissions (bits 0–24) |
| CASHIER | 82435 | Dashboard + View Items + View Stock + POS + Shifts |
| STOCK_CLERK | 12599295 | Dashboard + Items + Stock In/Out/Adj + Transactions + Approve + Cancel + Reprint |
| VIEWER | 139947 | Dashboard + View-only all modules |

Presets are applied on user creation based on role, but can be customized per user.

---

### 1. users

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| username | VARCHAR(50) | NOT NULL, UNIQUE | Login username |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed |
| full_name | VARCHAR(150) | NOT NULL | Display name |
| email | VARCHAR(150) | NULLABLE | Contact email |
| role | ENUM('ADMIN','STAFF') | NOT NULL | High-level role label |
| access_rights | BIGINT | NOT NULL, DEFAULT 0 | Bitwise permissions |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete flag |
| last_login | DATETIME | NULLABLE | Last successful login |
| created_by | VARCHAR(50) | NOT NULL | User who created |
| updated_by | VARCHAR(50) | NULLABLE | User who last updated |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `uk_users_username` — UNIQUE on `username`
- `idx_users_active` — on `active`
- `idx_users_role` — on `role`

---

### 2. categories

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Category name |
| description | VARCHAR(255) | NULLABLE | Optional description |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete flag |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `uk_categories_name` — UNIQUE on `name`
- `idx_categories_active` — on `active`

---

### 3. items

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| item_code | VARCHAR(50) | NOT NULL, UNIQUE | Item identification code |
| name | VARCHAR(200) | NOT NULL | Item name |
| description | TEXT | NULLABLE | Item description |
| category_id | BIGINT | FK → categories.id, NOT NULL | Item category |
| unit | VARCHAR(30) | NOT NULL | Unit of measure (pcs, kg, box, etc.) |
| price | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Selling price |
| cost_price | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Cost/purchase price |
| reorder_level | INT | NOT NULL, DEFAULT 0 | Low stock threshold |
| taxable | TINYINT(1) | NOT NULL, DEFAULT 1 | Is this item subject to tax? |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete flag |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `uk_items_item_code` — UNIQUE on `item_code`
- `idx_items_name` — on `name`
- `idx_items_category_id` — on `category_id`
- `idx_items_active` — on `active`

**Foreign Keys:**
- `fk_items_category` — `category_id` → `categories.id`

**Notes:**
- `taxable` flag determines if tax is computed for this item during transactions
- If `app_settings.tax_enabled = false`, tax is never computed regardless of this flag

---

### 4. customers

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(200) | NOT NULL | Customer / business name |
| tin | VARCHAR(20) | NULLABLE | Tax Identification Number (PH format: 123-456-789-000) |
| address | VARCHAR(500) | NULLABLE | Business / billing address |
| contact_person | VARCHAR(150) | NULLABLE | Contact name |
| contact_number | VARCHAR(20) | NULLABLE | Phone / mobile |
| email | VARCHAR(150) | NULLABLE | Email address |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete flag |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `idx_customers_name` — on `name`
- `idx_customers_tin` — on `tin`
- `idx_customers_active` — on `active`

---

### 5. suppliers

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(200) | NOT NULL | Supplier / business name |
| tin | VARCHAR(20) | NULLABLE | Tax Identification Number |
| address | VARCHAR(500) | NULLABLE | Business address |
| contact_person | VARCHAR(150) | NULLABLE | Contact name |
| contact_number | VARCHAR(20) | NULLABLE | Phone / mobile |
| email | VARCHAR(150) | NULLABLE | Email address |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete flag |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `idx_suppliers_name` — on `name`
- `idx_suppliers_tin` — on `tin`
- `idx_suppliers_active` — on `active`

---

### 6. stock

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| item_id | BIGINT | FK → items.id, UNIQUE, NOT NULL | One stock record per item |
| quantity_on_hand | INT | NOT NULL, DEFAULT 0 | Current stock quantity |
| last_updated | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last stock change |

**Indexes:**
- `uk_stock_item_id` — UNIQUE on `item_id`

**Foreign Keys:**
- `fk_stock_item` — `item_id` → `items.id`

---

### 7. stock_transactions

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| item_id | BIGINT | FK → items.id, NOT NULL | Item affected |
| transaction_type | ENUM('IN','OUT','ADJUSTMENT') | NOT NULL | Type of movement |
| status | ENUM('CREATED','APPROVED','CANCELLED') | NOT NULL, DEFAULT 'CREATED' | Transaction status |
| quantity | INT | NOT NULL | Units moved (always positive) |
| unit_cost | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Cost price at time of transaction |
| unit_price | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Selling price at time of transaction |
| discount_type | ENUM('NONE','FIXED','PERCENTAGE') | NOT NULL, DEFAULT 'NONE' | Discount type applied |
| discount_value | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Discount amount or percentage |
| discount_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Computed total discount in ₱ |
| subtotal | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | qty × unit_price (before discount/tax) |
| net_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | subtotal - discount + addons |
| tax_enabled | TINYINT(1) | NOT NULL, DEFAULT 0 | Was tax computed for this transaction? |
| tax_type | ENUM('VAT','NON_VAT','EXEMPT','ZERO_RATED') | NULLABLE | Tax classification (NULL if tax_enabled=0) |
| tax_rate | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Tax rate applied (e.g., 12.00) |
| tax_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Computed tax amount |
| vatable_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Amount subject to VAT |
| total_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Final amount (net + tax, or net if no tax) |
| customer_id | BIGINT | FK → customers.id, NULLABLE | Customer (for OUT transactions) |
| supplier_id | BIGINT | FK → suppliers.id, NULLABLE | Supplier (for IN transactions) |
| document_type | ENUM('OR','SI','DR','PO','RR','NONE') | NOT NULL, DEFAULT 'NONE' | Document type issued |
| document_no | VARCHAR(50) | NULLABLE | Document number (OR#, SI#, DR#, PO#, RR#) |
| reference_no | VARCHAR(50) | NULLABLE | External reference |
| remarks | VARCHAR(500) | NULLABLE | Notes |
| transaction_date | DATETIME | NOT NULL | When the transaction occurred |
| created_by | VARCHAR(50) | NOT NULL | User who created |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| approved_by | VARCHAR(50) | NULLABLE | User who approved |
| approved_at | DATETIME | NULLABLE | When approved |

**Indexes:**
- `idx_st_item_id` — on `item_id`
- `idx_st_transaction_type` — on `transaction_type`
- `idx_st_status` — on `status`
- `idx_st_transaction_date` — on `transaction_date`
- `idx_st_reference_no` — on `reference_no`
- `idx_st_document_no` — on `document_no`
- `idx_st_customer_id` — on `customer_id`
- `idx_st_supplier_id` — on `supplier_id`
- `idx_st_created_by` — on `created_by`

**Foreign Keys:**
- `fk_st_item` — `item_id` → `items.id`
- `fk_st_customer` — `customer_id` → `customers.id`
- `fk_st_supplier` — `supplier_id` → `suppliers.id`

**Notes:**
- Transactions are **append-only** (never updated or deleted, except status change)
- `unit_cost` and `unit_price` capture the price at the time of the transaction (historical)
- **Status workflow**: CREATED → APPROVED (or CANCELLED)
  - `CREATED`: Transaction recorded but stock NOT yet affected. Awaiting approval.
  - `APPROVED`: Transaction approved, stock quantity updated. Cannot be reverted.
  - `CANCELLED`: Transaction cancelled. No stock impact. Reason logged in remarks.
  - Only APPROVED transactions affect `stock.quantity_on_hand`
  - `approved_by` and `approved_at` populated on approval
- **Tax is optional**: `tax_enabled = 0` means no tax was computed (tax fields stay 0)
- **Tax is optional per-transaction**: Even if global tax is enabled, individual transactions can be saved without tax
- When `tax_enabled = 1`: tax fields are populated based on the item's tax type and configured rate
- For `IN` transactions: typically linked to supplier, document_type = PO or RR
- For `OUT` transactions: typically linked to customer, document_type = OR, SI, or DR
- For `ADJUSTMENT`: no customer/supplier, document_type = NONE
- **Printing**: Any transaction (CREATED or APPROVED) can be printed/reprinted anytime

---

### 8. transaction_addons

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| transaction_id | BIGINT | FK → stock_transactions.id, NOT NULL | Parent transaction |
| addon_name | VARCHAR(100) | NOT NULL | Name of the add-on charge |
| amount | DECIMAL(12,2) | NOT NULL | Add-on amount |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Indexes:**
- `idx_ta_transaction_id` — on `transaction_id`

**Foreign Keys:**
- `fk_ta_transaction` — `transaction_id` → `stock_transactions.id`

---

### 9. addon_master

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Add-on name |
| default_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Default charge amount |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft delete flag |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `uk_addon_master_name` — UNIQUE on `name`

---

### 10. audit_trail

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| entity_name | VARCHAR(100) | NOT NULL | Table/entity affected (e.g., "Item", "Customer") |
| entity_id | BIGINT | NOT NULL | PK of the affected record |
| action | ENUM('CREATE','UPDATE','DELETE') | NOT NULL | What happened |
| field_name | VARCHAR(100) | NULLABLE | Which field changed (for UPDATE) |
| old_value | TEXT | NULLABLE | Previous value |
| new_value | TEXT | NULLABLE | New value |
| performed_by | VARCHAR(50) | NOT NULL | Username who made the change |
| performed_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When |
| ip_address | VARCHAR(45) | NULLABLE | Client IP (supports IPv6) |

**Indexes:**
- `idx_audit_entity` — COMPOSITE on (`entity_name`, `entity_id`)
- `idx_audit_performed_by` — on `performed_by`
- `idx_audit_performed_at` — on `performed_at`
- `idx_audit_action` — on `action`

**Notes:**
- This table is **append-only** (never updated or deleted)
- For CREATE: one row with `field_name` = NULL, `new_value` = summary or JSON
- For UPDATE: one row per changed field (old_value → new_value)
- For DELETE (soft): treated as UPDATE on `active` field

---

### 11. app_settings

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| setting_key | VARCHAR(100) | NOT NULL, UNIQUE | Setting identifier |
| setting_value | TEXT | NULLABLE | Setting value |
| description | VARCHAR(255) | NULLABLE | Human-readable description |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Active/inactive flag |
| created_by | VARCHAR(50) | NOT NULL | User who created |
| updated_by | VARCHAR(50) | NULLABLE | User who last updated |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

**Indexes:**
- `uk_app_settings_key` — UNIQUE on `setting_key`
- `idx_app_settings_active` — on `active`

**Default Settings:**

| Key | Default Value | Description |
|-----|---------------|-------------|
| **Company Info** | | |
| business_name | (empty) | Registered business name |
| business_tin | (empty) | TIN (e.g., 123-456-789-000) |
| business_address | (empty) | Registered business address |
| business_contact | (empty) | Business phone/mobile |
| business_email | (empty) | Business email |
| **Tax Settings** | | |
| tax_enabled | false | Enable/disable tax computation globally |
| vat_registered | true | Is business VAT registered? |
| default_tax_rate | 12.00 | Default VAT rate (%) |
| pricing_method | VAT_INCLUSIVE | VAT_INCLUSIVE or VAT_EXCLUSIVE |
| **Item Settings** | | |
| item_code_auto_generate | false | Auto-generate item code on creation |
| item_code_prefix | ITM- | Prefix for auto-generated item codes |
| **Document Numbering** | | |
| or_prefix | OR- | Official Receipt prefix |
| or_next_number | 1 | Next OR sequential number |
| si_prefix | SI- | Sales Invoice prefix |
| si_next_number | 1 | Next SI sequential number |
| dr_prefix | DR- | Delivery Receipt prefix |
| dr_next_number | 1 | Next DR sequential number |
| po_prefix | PO- | Purchase Order prefix |
| po_next_number | 1 | Next PO sequential number |
| rr_prefix | RR- | Receiving Report prefix |
| rr_next_number | 1 | Next RR sequential number |
| document_number_format | {PREFIX}{YYYYMM}-{NNNNN} | Number format pattern |
| **Discount Settings** | | |
| enable_discounts | true | Enable discount feature |
| max_discount_percent_admin | 100 | Max discount % for ADMIN |
| max_discount_percent_staff | 20 | Max discount % for STAFF |
| **General** | | |
| default_page_size | 20 | Default pagination size |
| low_stock_alert_enabled | true | Show low stock alerts on dashboard |
| currency_symbol | ₱ | Display currency symbol |
| timezone | Asia/Manila | Application timezone |
| **POS Settings** | | |
| pos_enabled | true | Enable POS module |
| pos_receipt_prefix | RCT- | POS receipt/sale number prefix |
| pos_next_number | 1 | Next POS sale number |
| pos_receipt_footer | Thank you for your purchase! | Receipt footer text |
| pos_printing_enabled | false | Enable printing (system works without printer) |
| pos_auto_print_receipt | false | Auto-print receipt after payment (requires printing enabled) |
| pos_allow_negative_stock | false | Allow selling even if stock is zero |

---

### 12. sales

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| sale_no | VARCHAR(50) | NOT NULL, UNIQUE | Sale/receipt number (sequential) |
| customer_id | BIGINT | FK → customers.id, NULLABLE | Walk-in = NULL, or registered customer |
| shift_id | BIGINT | FK → shifts.id, NULLABLE | Associated cashier shift |
| subtotal | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Sum of line item totals |
| discount_type | ENUM('NONE','FIXED','PERCENTAGE','SENIOR_PWD') | NOT NULL, DEFAULT 'NONE' | Transaction-level discount |
| discount_value | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Discount amount or percentage |
| discount_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Computed total discount |
| addons_total | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Sum of sale add-ons |
| net_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | subtotal - discount + addons |
| tax_enabled | TINYINT(1) | NOT NULL, DEFAULT 0 | Was tax applied? |
| tax_type | ENUM('VAT','NON_VAT','EXEMPT','ZERO_RATED') | NULLABLE | Tax classification |
| tax_rate | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 | Tax rate applied |
| tax_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Computed tax |
| vatable_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Vatable portion |
| total_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Final amount due |
| amount_tendered | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Amount given by customer |
| change_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Change returned |
| payment_method | ENUM('CASH','GCASH','BANK_TRANSFER','CREDIT','MULTIPLE') | NOT NULL, DEFAULT 'CASH' | Primary payment method |
| status | ENUM('OPEN','PAID','CLOSED','VOIDED') | NOT NULL, DEFAULT 'OPEN' | Sale status |
| void_reason | VARCHAR(500) | NULLABLE | Reason if voided |
| voided_by | VARCHAR(50) | NULLABLE | User who voided |
| voided_at | DATETIME | NULLABLE | When voided |
| document_type | ENUM('OR','SI','NONE') | NOT NULL, DEFAULT 'OR' | Document issued |
| document_no | VARCHAR(50) | NULLABLE | OR# or SI# |
| remarks | VARCHAR(500) | NULLABLE | |
| sale_date | DATETIME | NOT NULL | Date/time of sale |
| created_by | VARCHAR(50) | NOT NULL | Cashier username |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Indexes:**
- `uk_sales_sale_no` — UNIQUE on `sale_no`
- `idx_sales_customer_id` — on `customer_id`
- `idx_sales_shift_id` — on `shift_id`
- `idx_sales_sale_date` — on `sale_date`
- `idx_sales_status` — on `status`
- `idx_sales_payment_method` — on `payment_method`
- `idx_sales_created_by` — on `created_by`
- `idx_sales_document_no` — on `document_no`

**Foreign Keys:**
- `fk_sales_customer` — `customer_id` → `customers.id`
- `fk_sales_shift` — `shift_id` → `shifts.id`

**Notes:**
- **Status workflow**: OPEN → PAID → CLOSED (or VOIDED at any point)
  - `OPEN`: Sale started, items in cart. Stock NOT yet deducted. Can still add/remove items.
  - `PAID`: Payment received, stock deducted. Receipt available for print/reprint.
  - `CLOSED`: Sale finalized and closed (end-of-day or manual). No further action possible.
  - `VOIDED`: Sale cancelled. Stock reversed (if was PAID). Requires reason + permission.
- Only `PAID` and `CLOSED` sales deduct stock
- Voiding a `PAID` or `CLOSED` sale reverses the stock deduction
- Voiding an `OPEN` sale simply cancels it (no stock impact)
- Walk-in customers have `customer_id = NULL`
- `SENIOR_PWD` discount type = 20% off + VAT exempt (PH compliance)
- **Reprinting**: Any sale in PAID or CLOSED status can be reprinted anytime

---

### 13. sale_items

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| sale_id | BIGINT | FK → sales.id, NOT NULL | Parent sale |
| item_id | BIGINT | FK → items.id, NOT NULL | Item sold |
| item_name | VARCHAR(200) | NOT NULL | Item name snapshot (at time of sale) |
| item_code | VARCHAR(50) | NOT NULL | Item code snapshot |
| quantity | INT | NOT NULL | Qty sold |
| unit_price | DECIMAL(12,2) | NOT NULL | Selling price at time of sale |
| unit_cost | DECIMAL(12,2) | NOT NULL | Cost price (for profit calculation) |
| discount_type | ENUM('NONE','FIXED','PERCENTAGE') | NOT NULL, DEFAULT 'NONE' | Line-level discount |
| discount_value | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | |
| discount_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Computed line discount |
| line_total | DECIMAL(12,2) | NOT NULL | (qty × unit_price) - discount_amount |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Indexes:**
- `idx_si_sale_id` — on `sale_id`
- `idx_si_item_id` — on `item_id`

**Foreign Keys:**
- `fk_si_sale` — `sale_id` → `sales.id`
- `fk_si_item` — `item_id` → `items.id`

**Notes:**
- `item_name` and `item_code` are snapshots — preserved even if item is later renamed or deactivated
- `unit_cost` captured for gross profit calculations
- Line-level discount is per-item (e.g., promo item), separate from transaction-level discount

---

### 14. sale_addons

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| sale_id | BIGINT | FK → sales.id, NOT NULL | Parent sale |
| addon_name | VARCHAR(100) | NOT NULL | Name of add-on charge |
| amount | DECIMAL(12,2) | NOT NULL | Add-on amount |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Indexes:**
- `idx_sa_sale_id` — on `sale_id`

**Foreign Keys:**
- `fk_sa_sale` — `sale_id` → `sales.id`

---

### 15. sale_payments

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| sale_id | BIGINT | FK → sales.id, NOT NULL | Parent sale |
| payment_method | ENUM('CASH','GCASH','BANK_TRANSFER','CREDIT') | NOT NULL | Payment method |
| amount | DECIMAL(12,2) | NOT NULL | Amount paid via this method |
| reference_no | VARCHAR(100) | NULLABLE | GCash ref#, bank ref#, etc. |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

**Indexes:**
- `idx_sp_sale_id` — on `sale_id`

**Foreign Keys:**
- `fk_sp_sale` — `sale_id` → `sales.id`

**Notes:**
- Used when `sales.payment_method = 'MULTIPLE'` (split payment)
- For single payment method, this table may have one row or be empty (amount on `sales` table is sufficient)
- `reference_no` stores GCash reference, bank transaction ID, etc.

---

### 16. shifts

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| cashier | VARCHAR(50) | NOT NULL | Username of cashier |
| opening_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Starting cash in drawer |
| closing_amount | DECIMAL(12,2) | NULLABLE | Actual cash counted at close |
| expected_amount | DECIMAL(12,2) | NULLABLE | Computed: opening + cash sales - change |
| difference | DECIMAL(12,2) | NULLABLE | closing - expected (over/short) |
| total_sales | DECIMAL(12,2) | NULLABLE | Total sales amount during shift |
| total_transactions | INT | NULLABLE | Number of completed transactions |
| total_voided | DECIMAL(12,2) | NULLABLE | Total voided amount |
| total_returns | DECIMAL(12,2) | NULLABLE | Total returned amount |
| status | ENUM('OPEN','CLOSED') | NOT NULL, DEFAULT 'OPEN' | Shift status |
| opened_at | DATETIME | NOT NULL | Shift start time |
| closed_at | DATETIME | NULLABLE | Shift end time |
| remarks | VARCHAR(500) | NULLABLE | Notes on close |

**Indexes:**
- `idx_shifts_cashier` — on `cashier`
- `idx_shifts_status` — on `status`
- `idx_shifts_opened_at` — on `opened_at`

**Notes:**
- Only ONE shift can be OPEN per cashier at a time (enforced by application)
- `expected_amount` = opening_amount + total cash received - total change given
- `difference` = closing_amount - expected_amount (positive = over, negative = short)
- Shift must be closed before generating Z-reading

---

## Table Summary

| # | Table | Purpose | Rows Expected |
|---|-------|---------|---------------|
| 1 | users | System users | Small (< 50) |
| 2 | categories | Item categories | Small (< 100) |
| 3 | items | Item master data | Medium (100–10,000) |
| 4 | customers | Customer master (for sales/OR/SI) | Medium (100–5,000) |
| 5 | suppliers | Supplier master (for purchases/PO) | Small–Medium (10–500) |
| 6 | stock | Current stock per item | Same as items (1:1) |
| 7 | stock_transactions | All stock movements (non-POS) | Large (grows over time) |
| 8 | transaction_addons | Add-on charges per stock transaction | Medium-Large |
| 9 | addon_master | Predefined add-on types | Small (< 50) |
| 10 | audit_trail | Change history | Large (grows over time) |
| 11 | app_settings | Application configuration | Small (< 40 rows) |
| 12 | sales | POS sales header | Large (grows over time) |
| 13 | sale_items | POS sale line items | Large (grows over time) |
| 14 | sale_addons | Add-on charges per POS sale | Medium |
| 15 | sale_payments | Split payment records | Medium-Large |
| 16 | shifts | Cashier shift records | Medium (grows over time) |

**Total: 16 tables**

---

## Pricing Calculation Reference

### Tax Option Behavior

```
IF app_settings.tax_enabled = false:
    → Tax fields are hidden in UI
    → tax_enabled = 0 on all transactions
    → tax_amount = 0, vatable_amount = 0
    → total_amount = net_amount

IF app_settings.tax_enabled = true:
    → User can CHOOSE per transaction: "Apply tax?" checkbox
    → If checked (tax_enabled = 1): tax is computed
    → If unchecked (tax_enabled = 0): saved without tax
```

### Stock OUT — WITHOUT Tax (tax_enabled = 0)
```
subtotal        = quantity × unit_price
discount_amount = (discount_type == 'FIXED')      ? discount_value × quantity
                : (discount_type == 'PERCENTAGE') ? subtotal × (discount_value / 100)
                : 0
addons_total    = SUM(transaction_addons.amount)
net_amount      = subtotal - discount_amount + addons_total
tax_amount      = 0
total_amount    = net_amount
gross_profit    = total_amount - (quantity × unit_cost)
```

### Stock OUT — WITH Tax, VAT-INCLUSIVE (tax_enabled = 1, pricing_method = VAT_INCLUSIVE)
```
subtotal        = quantity × unit_price                          (price already includes VAT)
discount_amount = (based on discount type)
addons_total    = SUM(transaction_addons.amount)
net_amount      = subtotal - discount_amount + addons_total
vatable_amount  = net_amount / 1.12
tax_amount      = vatable_amount × (tax_rate / 100)             (= vatable_amount × 0.12)
total_amount    = net_amount                                    (already VAT-inclusive)
gross_profit    = total_amount - tax_amount - (quantity × unit_cost)
```

### Stock OUT — WITH Tax, VAT-EXCLUSIVE (tax_enabled = 1, pricing_method = VAT_EXCLUSIVE)
```
subtotal        = quantity × unit_price                          (price is net of VAT)
discount_amount = (based on discount type)
addons_total    = SUM(transaction_addons.amount)
net_amount      = subtotal - discount_amount + addons_total
vatable_amount  = net_amount
tax_amount      = net_amount × (tax_rate / 100)                 (= net_amount × 0.12)
total_amount    = net_amount + tax_amount
gross_profit    = net_amount - (quantity × unit_cost)
```

### Stock OUT — TAX EXEMPT (tax_type = 'EXEMPT')
```
subtotal        = quantity × unit_price
discount_amount = (based on discount type)
addons_total    = SUM(transaction_addons.amount)
net_amount      = subtotal - discount_amount + addons_total
tax_amount      = 0
vatable_amount  = 0
total_amount    = net_amount
```

### Stock IN (Purchase/Receive)
```
subtotal        = quantity × unit_cost
discount_amount = (if supplier discount applied)
addons_total    = SUM(transaction_addons.amount) — e.g., shipping, handling
net_amount      = subtotal - discount_amount + addons_total
tax fields      = based on supplier invoice (optional)
total_amount    = net_amount + tax_amount (or just net_amount if no tax)
```

### Stock ADJUSTMENT
```
No financial calculation — only quantity change
unit_cost and unit_price recorded for reference only
discount fields = 0, tax fields = 0
total_amount    = 0
```

### POS Sale (Cart-based)
```
FOR EACH sale_item:
    line_total      = (quantity × unit_price) - line_discount_amount

sale.subtotal       = SUM(sale_items.line_total)
sale.discount_amount= (discount_type == 'FIXED')       ? discount_value
                    : (discount_type == 'PERCENTAGE')   ? subtotal × (discount_value / 100)
                    : (discount_type == 'SENIOR_PWD')   ? subtotal × 0.20
                    : 0
sale.addons_total   = SUM(sale_addons.amount)
sale.net_amount     = subtotal - discount_amount + addons_total

IF tax_enabled = 0:
    sale.tax_amount     = 0
    sale.total_amount   = net_amount

IF tax_enabled = 1 AND pricing_method = VAT_INCLUSIVE:
    sale.vatable_amount = net_amount / 1.12
    sale.tax_amount     = vatable_amount × 0.12
    sale.total_amount   = net_amount                    (already includes VAT)

IF tax_enabled = 1 AND pricing_method = VAT_EXCLUSIVE:
    sale.vatable_amount = net_amount
    sale.tax_amount     = net_amount × (tax_rate / 100)
    sale.total_amount   = net_amount + tax_amount

IF discount_type = 'SENIOR_PWD':
    sale.tax_type       = 'EXEMPT'                     (20% disc + VAT exempt per PH law)
    sale.tax_amount     = 0
    sale.total_amount   = net_amount

sale.change_amount  = amount_tendered - total_amount

GROSS PROFIT:
    total_cost          = SUM(sale_items.quantity × sale_items.unit_cost)
    gross_profit        = net_amount - tax_amount - total_cost   (if VAT-inclusive)
                       OR net_amount - total_cost                (if no tax / VAT-exclusive)
```

### POS Stock Deduction
```
ON sale status changed to PAID:
    FOR EACH sale_item:
        stock.quantity_on_hand -= sale_item.quantity
        (also creates stock_transactions record with type='OUT', status='APPROVED', reference = sale_no)

ON sale VOIDED (from PAID or CLOSED):
    FOR EACH sale_item:
        stock.quantity_on_hand += sale_item.quantity
        (also creates stock_transactions record with type='IN', status='APPROVED', reference = sale_no + ' [VOID]')

ON sale VOIDED (from OPEN):
    No stock impact — sale was never paid
```

### Inventory Stock Update (Approval-based)
```
ON stock_transaction status changed to APPROVED:
    IF transaction_type = 'IN':
        stock.quantity_on_hand += quantity
    IF transaction_type = 'OUT':
        stock.quantity_on_hand -= quantity
    IF transaction_type = 'ADJUSTMENT':
        stock.quantity_on_hand = new_quantity (or += adjustment)

WHILE status = 'CREATED':
    Stock is NOT affected — waiting for approval
```

---

## Document Types (Philippine Standard)

| Code | Full Name | Used For | Generated On |
|------|-----------|----------|--------------|
| OR | Official Receipt | Cash sales | Stock OUT |
| SI | Sales Invoice | Credit/terms sales | Stock OUT |
| DR | Delivery Receipt | Goods delivery | Stock OUT |
| PO | Purchase Order | Ordering from supplier | Stock IN (reference) |
| RR | Receiving Report | Confirming receipt | Stock IN |
| NONE | No document | Internal adjustments | ADJUSTMENT |

### Document Number Format
- Default: `{PREFIX}{YYYYMM}-{NNNNN}`
- Example: `OR-202608-00001`, `SI-202608-00001`, `PO-202608-00001`
- Numbers are **sequential** and **non-repeating** (BIR requirement)
- Series managed via `app_settings` (prefix + next_number)

---

## Constraints & Business Rules

### Inventory Rules
1. **Stock cannot go negative** — Stock OUT and POS validate `quantity_on_hand >= requested quantity` (unless `pos_allow_negative_stock = true`)
2. **Item code must be unique** — enforced at DB level
3. **Category name must be unique** — enforced at DB level
4. **Transactions are append-only** — no DELETE on `stock_transactions`; only status changes allowed (CREATED → APPROVED)
5. **Audit trail is immutable** — no UPDATE or DELETE on `audit_trail`
6. **Soft delete only** — items, categories, users, customers, suppliers use `active` flag
7. **Deactivated items** — cannot create new transactions or sell inactive items
8. **Discount limits** — enforced by application based on `app_settings` per role
9. **One stock record per item** — enforced by UNIQUE constraint on `stock.item_id`
10. **Document numbers are sequential** — managed by app_settings, no gaps (voided numbers are logged)
11. **Tax is optional** — transactions can be saved with or without tax computation
12. **Customer required for OR/SI** — if document_type is OR or SI, customer_id should be populated
13. **Supplier required for PO/RR** — if document_type is PO or RR, supplier_id should be populated
14. **Approval required for stock impact** — Stock In/Out/Adjust only affect quantity when status = APPROVED
15. **Approval permission** — only users with appropriate permission can approve transactions
16. **Reprint anytime** — any APPROVED transaction or PAID/CLOSED sale can be reprinted

### POS Rules
17. **One open shift per cashier** — cannot open a new shift until current one is closed
18. **Sales require open shift** — POS transactions must be associated with an open shift (or shift_id = NULL if shift management is disabled)
19. **Sale numbers are sequential** — managed by app_settings, non-repeating
20. **POS status flow**: OPEN → PAID → CLOSED (or VOIDED at any stage)
21. **Stock deducted on PAID** — not on OPEN (items in cart don't affect stock until paid)
22. **Void requires reason** — voided sales must have `void_reason` populated
23. **Void requires permission** — only users with VOID_SALES permission can void
24. **Void reverses stock** — voiding a PAID/CLOSED sale restores stock quantities
25. **CLOSED is final** — closed sales cannot be re-opened (only voided if needed)
26. **Split payment must sum to total** — SUM(sale_payments.amount) must equal sales.total_amount
27. **OPEN sales are editable** — can add/remove items while status = OPEN
28. **PAID/CLOSED sales are immutable** — cannot edit (only void and re-create)

---

## Notes

- All DATETIME columns store in Asia/Manila timezone (UTC+8)
- DECIMAL(12,2) supports values up to ₱9,999,999,999.99
- DECIMAL(5,2) for tax_rate supports rates up to 999.99%
- ENUM values can be extended later via ALTER TABLE if needed
- Consider partitioning `stock_transactions`, `sales`, and `audit_trail` by year if data grows significantly
- Refresh tokens (for JWT) can be stored in a separate `refresh_tokens` table if needed
- **Tax toggle**: System works fully without tax enabled — suitable for businesses not yet VAT-registered or those who prefer simple inventory tracking without tax computation
- **POS and Inventory are integrated**: POS sales auto-deduct stock. Manual Stock OUT is still available for non-POS releases (warehouse, delivery, etc.)
- **Senior Citizen / PWD Discount** (20% + VAT exempt): Built-in as `SENIOR_PWD` discount type in POS
- **Void vs. Return**: Void = cancel entire sale (full stock restore). Return = partial return (future enhancement for individual line-item returns)
