# Inventory System - Philippine Standards & Localization

## Overview

This document outlines Philippine-specific business standards, tax requirements, and localization considerations for the inventory system.

---

## Currency & Formatting

| Setting | Value |
|---------|-------|
| Currency | Philippine Peso (PHP / ₱) |
| Currency Code | PHP |
| Symbol | ₱ |
| Decimal Places | 2 |
| Thousands Separator | Comma (1,000,000.00) |
| Date Format | MM/DD/YYYY (common) or DD/MM/YYYY |
| Time Zone | Asia/Manila (UTC+8) |
| Language | English (primary), Filipino (optional labels) |

---

## Tax (BIR Compliance)

### VAT (Value Added Tax)
| Rule | Detail |
|------|--------|
| Standard VAT Rate | 12% |
| VAT-Exempt | Certain agricultural goods, educational services |
| VAT Threshold | Gross sales > ₱3,000,000 annually = VAT registered |
| Non-VAT | Percentage tax (3%) if below threshold |

### Tax Fields Needed in Transactions

| Column | Type | Description |
|--------|------|-------------|
| tax_type | ENUM('VAT','NON_VAT','EXEMPT') | Tax classification |
| tax_rate | DECIMAL(5,2) | Tax rate applied (e.g., 12.00) |
| tax_amount | DECIMAL(12,2) | Computed tax |
| vatable_amount | DECIMAL(12,2) | Amount subject to tax |
| vat_exempt_amount | DECIMAL(12,2) | Exempt portion |
| zero_rated_amount | DECIMAL(12,2) | Zero-rated portion |

### BIR Reporting Requirements
- **Sales Summary** — daily/monthly/quarterly
- **Purchase Summary** — for stock-in (cost of goods)
- **VAT Relief Summary** — if applicable
- **BIR Form 2550M** — Monthly VAT declaration
- **BIR Form 2550Q** — Quarterly VAT declaration
- **BIR Form 2551Q** — Quarterly percentage tax (non-VAT)

---

## Philippine Business Document Standards

### Official Receipt (OR) / Sales Invoice (SI)
Required for all stock-out (sales) transactions:

| Field | Required |
|-------|----------|
| Business Name | Yes |
| TIN (Tax Identification Number) | Yes |
| Business Address | Yes |
| OR/SI Number (sequential) | Yes |
| Date | Yes |
| Customer Name/TIN (if ₱1,000+) | Yes (for VAT) |
| Item description | Yes |
| Quantity | Yes |
| Unit Price | Yes |
| Total Amount | Yes |
| VAT breakdown (Vatable Sales, VAT, VAT-Exempt, Zero-Rated) | Yes (for VAT registered) |

### Delivery Receipt (DR)
Required when goods are released:

| Field | Required |
|-------|----------|
| DR Number (sequential) | Yes |
| Date | Yes |
| Customer/Recipient | Yes |
| Items delivered | Yes |
| Quantity | Yes |
| Remarks | Optional |

---

## Database Schema Additions (Philippine)

### Updated: stock_transactions (add tax columns)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| tax_type | ENUM('VAT','NON_VAT','EXEMPT') | NOT NULL, DEFAULT 'VAT' | Tax classification |
| tax_rate | DECIMAL(5,2) | NOT NULL, DEFAULT 12.00 | Tax rate (%) |
| tax_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Computed tax amount |
| vatable_amount | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Vatable sales portion |

### New Table: customers (for OR/SI compliance)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(200) | NOT NULL | Customer/business name |
| tin | VARCHAR(20) | NULLABLE | Tax Identification Number |
| address | VARCHAR(500) | NULLABLE | Business/billing address |
| contact_person | VARCHAR(150) | NULLABLE | Contact name |
| contact_number | VARCHAR(20) | NULLABLE | Phone/mobile |
| email | VARCHAR(150) | NULLABLE | Email address |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

### New Table: suppliers (for purchase tracking)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(200) | NOT NULL | Supplier/business name |
| tin | VARCHAR(20) | NULLABLE | Tax Identification Number |
| address | VARCHAR(500) | NULLABLE | Business address |
| contact_person | VARCHAR(150) | NULLABLE | Contact name |
| contact_number | VARCHAR(20) | NULLABLE | Phone/mobile |
| email | VARCHAR(150) | NULLABLE | Email address |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | |
| created_by | VARCHAR(50) | NOT NULL | |
| updated_by | VARCHAR(50) | NULLABLE | |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

### Updated: stock_transactions (add customer/supplier reference)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| customer_id | BIGINT | FK → customers.id, NULLABLE | Customer (for OUT) |
| supplier_id | BIGINT | FK → suppliers.id, NULLABLE | Supplier (for IN) |
| document_type | ENUM('OR','SI','DR','PO','RR','NONE') | NOT NULL, DEFAULT 'NONE' | Document type |
| document_no | VARCHAR(50) | NULLABLE | OR#, SI#, DR#, PO# |

### Document Types
| Code | Meaning | Used For |
|------|---------|----------|
| OR | Official Receipt | Sales (cash) |
| SI | Sales Invoice | Sales (credit/terms) |
| DR | Delivery Receipt | Stock release/delivery |
| PO | Purchase Order | Stock purchase |
| RR | Receiving Report | Stock received |
| NONE | No document | Internal adjustments |

---

## Updated App Settings (Philippine)

| Key | Default Value | Description |
|-----|---------------|-------------|
| business_name | (empty) | Registered business name |
| business_tin | (empty) | TIN (e.g., 123-456-789-000) |
| business_address | (empty) | Registered business address |
| vat_registered | true | Is business VAT registered? |
| default_tax_rate | 12.00 | Default VAT rate |
| or_series_prefix | OR- | Official Receipt prefix |
| or_next_number | 1 | Next OR number |
| si_series_prefix | SI- | Sales Invoice prefix |
| si_next_number | 1 | Next SI number |
| dr_series_prefix | DR- | Delivery Receipt prefix |
| dr_next_number | 1 | Next DR number |

---

## Additional Reports (Philippine / BIR)

| Report | Purpose | Parameters |
|--------|---------|------------|
| Sales Summary | Daily/monthly sales with VAT breakdown | Date range |
| Purchase Summary | Stock-in costs for expense tracking | Date range |
| VAT Summary | Vatable, VAT-exempt, zero-rated breakdown | Month/Quarter |
| Inventory Count Sheet | Physical count form (for stock-taking) | Category filter |
| Stock Valuation | Total inventory value (cost-based) | As-of date |

---

## Philippine Pricing Calculation (with VAT)

### VAT-Inclusive Pricing (most common in PH retail)
```
selling_price       = ₱112.00 (price displayed to customer, VAT-inclusive)
vatable_amount      = selling_price / 1.12 = ₱100.00
tax_amount          = vatable_amount × 0.12 = ₱12.00

subtotal            = quantity × selling_price
discount_amount     = (based on discount type)
net_amount          = subtotal - discount_amount + addons
vatable_amount      = net_amount / 1.12
tax_amount          = vatable_amount × 0.12
```

### VAT-Exclusive Pricing (common in B2B)
```
selling_price       = ₱100.00 (net of VAT)
tax_amount          = selling_price × 0.12 = ₱12.00
total_with_vat      = ₱112.00

subtotal            = quantity × selling_price
discount_amount     = (based on discount type)
net_amount          = subtotal - discount_amount + addons
tax_amount          = net_amount × tax_rate
total_amount        = net_amount + tax_amount
```

### Setting: `pricing_method` = `VAT_INCLUSIVE` or `VAT_EXCLUSIVE`

---

## Numbering Standards (Philippine)

### Sequential Document Numbers
- Must be **sequential** and **non-repeating** (BIR requirement)
- Format: `PREFIX-YYYYMM-NNNNN` (e.g., `OR-202608-00001`)
- Gaps must be accounted for (voided numbers must be logged)
- Series resets per year or continuous (configurable)

### Item Code Format Options
- Simple sequential: `ITM-00001`
- Category-based: `FOOD-001`, `BVRG-001`
- Custom: User-defined

---

## Units of Measure (Common in PH)

| Code | Description | Common Use |
|------|-------------|------------|
| pcs | Pieces | General items |
| kg | Kilogram | Meat, rice, produce |
| g | Gram | Small items, spices |
| L | Liter | Liquids |
| mL | Milliliter | Small liquid items |
| box | Box | Packed goods |
| pack | Pack | Bundled items |
| ream | Ream | Paper |
| doz | Dozen | Eggs, items in 12s |
| roll | Roll | Tissue, tape |
| gal | Gallon | Water, fuel |
| sack | Sack | Rice, cement |
| case | Case | Canned goods, bottles |
| bundle | Bundle | Vegetables, sticks |
| pair | Pair | Shoes, gloves |
| set | Set | Tool sets, combo items |
| can | Can | Canned goods |
| bot | Bottle | Beverages, sauces |
| jar | Jar | Preserved goods |
| tray | Tray | Eggs, plants |

---

## Summary of Schema Changes

### New Tables (+7 from base)
| Table | Purpose |
|-------|---------|
| customers | Customer master for OR/SI compliance |
| suppliers | Supplier master for purchase tracking |
| sales | POS sales header |
| sale_items | POS sale line items |
| sale_addons | POS sale add-on charges |
| sale_payments | POS split payment records |
| shifts | Cashier shift management |

### Updated Tables
| Table | New Columns |
|-------|-------------|
| stock_transactions | tax_type, tax_rate, tax_amount, vatable_amount, customer_id, supplier_id, document_type, document_no |
| items | taxable flag |
| app_settings | PH-specific + POS settings |

### Total Tables: 16

---

## POS — Philippine Requirements

### Official Receipt (OR) on POS Sales

Per BIR regulations, POS-generated receipts must contain:

| Field | Required | Source |
|-------|----------|--------|
| Business Name (Registered) | Yes | app_settings.business_name |
| Business TIN | Yes | app_settings.business_tin |
| Business Address (Registered) | Yes | app_settings.business_address |
| VAT Registered / Non-VAT label | Yes | app_settings.vat_registered |
| Receipt Number (OR#) | Yes | Sequential, from app_settings |
| Date and Time | Yes | sale_date |
| Cashier Name | Yes | created_by |
| Items (Description, Qty, Unit Price, Amount) | Yes | sale_items |
| Subtotal | Yes | sales.subtotal |
| Discount (type + amount) | If applicable | sales.discount_amount |
| VAT-able Sales | If VAT registered | sales.vatable_amount |
| VAT Amount (12%) | If VAT registered | sales.tax_amount |
| VAT-Exempt Sales | If applicable | When tax_type = EXEMPT |
| Zero-Rated Sales | If applicable | When tax_type = ZERO_RATED |
| Total Amount Due | Yes | sales.total_amount |
| Payment Method | Yes | sales.payment_method |
| Amount Tendered | Yes (for cash) | sales.amount_tendered |
| Change | Yes (for cash) | sales.change_amount |
| "THIS SERVES AS YOUR OFFICIAL RECEIPT" | Yes (for OR) | Static text |
| "THIS IS NOT AN OFFICIAL RECEIPT" | If no OR issued | For non-OR transactions |

### BIR POS Machine Requirements (if CAS-registered)

If the business registers this system as a Computerized Accounting System (CAS) with the BIR:

1. **Permit to Use**: Must obtain BIR permit (Form 1900)
2. **Sequential Numbering**: No gaps in OR/SI numbers
3. **Z-Reading**: Daily end-of-day sales summary (mandatory)
4. **X-Reading**: Shift/interval reading (optional but standard)
5. **Void Logging**: All voids must be logged with reason and authorization
6. **Audit Trail**: All transactions must be traceable (who, when, what)
7. **Data Retention**: Transaction records must be kept for 10 years
8. **No Manual Modification**: Completed sales cannot be edited (void only)

### Senior Citizen / PWD Discount (RA 9994 / RA 7277)

| Rule | Detail |
|------|--------|
| Discount | 20% off on qualifying items |
| VAT | Exempt (no VAT on discounted amount) |
| ID Required | Senior Citizen ID or PWD ID |
| Limit | One transaction per ID per day (per establishment) |
| Fields to capture | SC/PWD ID number, name, signature (can be optional in system) |

Implementation in POS:
- `discount_type = 'SENIOR_PWD'` on sales table
- Automatically sets `tax_type = 'EXEMPT'` and `tax_amount = 0`
- Receipt prints "SC/PWD Discount" and shows VAT-exempt breakdown

### POS Payment Methods (Philippine Common)

| Method | Description | Reference Required |
|--------|-------------|-------------------|
| CASH | Cash payment | No |
| GCASH | GCash e-wallet | GCash reference number |
| BANK_TRANSFER | Bank transfer / online banking | Bank reference number |
| CREDIT | Credit terms / charge to account | Customer ID required |

### POS Reports (BIR-relevant)

| Report | BIR Requirement | Frequency |
|--------|-----------------|-----------|
| Z-Reading | Mandatory (if CAS) | Daily (end-of-day) |
| X-Reading | Recommended | Per-shift or on-demand |
| Voided Transactions | Must be logged | On-demand |
| Sales Summary | For BIR filing | Monthly/Quarterly |
| VAT Summary | For 2550M/2550Q | Monthly/Quarterly |

### Receipt Printing

**Printing is optional.** The system works fully without a printer. Receipts are always viewable on-screen. Printing is an add-on configuration when a printer is available.

**Behavior:**
| Setting | Result |
|---------|--------|
| `pos_printing_enabled = false` | No print button, receipt displayed on-screen only |
| `pos_printing_enabled = true` | Print button available, user can choose to print |
| `pos_auto_print_receipt = true` | Receipt auto-prints after payment (no dialog) |

**When printing IS configured:**

Thermal Printer Compatibility:
- Standard widths: 58mm (mini) or 80mm (standard)
- Connection: USB, Bluetooth, or Network
- Protocol: ESC/POS (Epson standard — most thermal printers support this)

Implementation Approach:
- Primary: Browser print dialog (CSS `@media print` with receipt layout)
- Alternative: Direct ESC/POS via browser API or local print service
- Receipt template renders from sale data (API provides formatted JSON)
- Paper size: 80mm × variable height (auto-cut after footer)

Per-Device Printing (when enabled):
| Device | Printing Method |
|--------|----------------|
| Desktop (browser) | Browser print dialog → USB thermal printer |
| Tablet | Browser print dialog → Bluetooth/WiFi printer |
| Mobile Phone | Browser print dialog → Bluetooth printer |
| POS Unit (dedicated) | Auto-print on sale complete → USB thermal printer |

**When printing is NOT configured:**
- Sale completes normally
- Receipt data saved in database (can be reprinted later if printer is added)
- Receipt preview shown on-screen (closeable)
- User can still use browser's native print (Ctrl+P) on the receipt preview if needed

---

## Notes

- Philippine businesses commonly use **VAT-inclusive** pricing for retail (B2C)
- **Senior Citizen / PWD Discount**: 20% discount + VAT exempt — built into POS as `SENIOR_PWD` discount type
- **BIR CAS (Computerized Accounting System)**: If the system is used as primary books of account, it needs BIR permit. This is optional for internal inventory tracking.
- **Peso sign display**: Use `₱` symbol, formatted as `₱1,234,567.89`
- **POS is optional**: The system works as pure inventory without POS. POS module can be enabled/disabled via `app_settings.pos_enabled`
- **Walk-in customers**: POS supports walk-in (no customer selected) or registered customers for OR/SI compliance
