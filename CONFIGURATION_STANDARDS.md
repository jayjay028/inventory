# Inventory + POS System - Configuration Standards

## Overview

All configuration is externalized to make the system modular. Forms, reports, validation messages, database settings, and UI elements are defined in configuration files — not hardcoded. This allows adding/removing features, forms, and reports without code changes.

---

## Configuration File Structure

```
src/main/resources/
├── application.yml                          # Main Spring Boot config
├── application-dev.yml                      # Dev environment overrides
├── application-prod.yml                     # Production overrides
│
├── config/
│   ├── validation-messages.properties       # Custom validation messages
│   ├── validation-messages_fil.properties   # Filipino translations (optional)
│   ├── error-messages.properties            # Business error messages
│   ├── navigation.yml                       # Sidebar/menu configuration
│   ├── forms.yml                            # Form field definitions per module
│   ├── reports.yml                          # Report registry (available reports)
│   ├── permissions.yml                      # Permission definitions
│   ├── document-types.yml                   # Document type registry
│   ├── units-of-measure.yml                 # UOM options
│   └── payment-methods.yml                  # Payment method options
│
├── reports/                                  # JasperReport templates
│   ├── inventory/
│   │   ├── stock-level.jrxml
│   │   ├── stock-movement.jrxml
│   │   ├── low-stock.jrxml
│   │   ├── transaction-summary.jrxml
│   │   ├── item-list.jrxml
│   │   ├── inventory-count.jrxml
│   │   └── stock-valuation.jrxml
│   ├── financial/
│   │   ├── gross-profit.jrxml
│   │   ├── profit-share.jrxml
│   │   ├── sales-summary.jrxml
│   │   ├── purchase-summary.jrxml
│   │   └── vat-summary.jrxml
│   └── pos/
│       ├── daily-sales.jrxml
│       ├── shift-report.jrxml
│       ├── sales-by-payment.jrxml
│       ├── sales-by-cashier.jrxml
│       ├── top-selling.jrxml
│       ├── voided-transactions.jrxml
│       └── hourly-sales.jrxml
│
├── templates/
│   └── receipt/
│       └── pos-receipt.html                 # Receipt HTML template (for printing)
│
├── db/
│   ├── schema.sql                           # Full DDL (16 tables)
│   ├── seed.sql                             # Default data (admin, settings)
│   └── sample-data.sql                      # Optional test/demo data
│
└── static/                                   # Vue build output (production)
    └── ...
```

---

## application.yml (Main Configuration)

```yaml
server:
  port: 8080
  servlet:
    context-path: /inventory

spring:
  profiles:
    active: dev

  application:
    name: inventory-system

  # ─── JDBC / DataSource ───────────────────────────────────────────
  datasource:
    url: jdbc:mysql://localhost:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 20000
      max-lifetime: 1800000
      pool-name: InventoryHikariPool

  # ─── JPA / Hibernate ────────────────────────────────────────────
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
        jdbc:
          time_zone: Asia/Manila
        default_batch_fetch_size: 20

  # ─── Jackson (JSON) ─────────────────────────────────────────────
  jackson:
    date-format: yyyy-MM-dd'T'HH:mm:ss
    time-zone: Asia/Manila
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false

  # ─── File Upload ────────────────────────────────────────────────
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

  # ─── Messages ───────────────────────────────────────────────────
  messages:
    basename: config/validation-messages,config/error-messages
    encoding: UTF-8
    cache-duration: 3600

# ─── Application Custom Properties ──────────────────────────────
app:
  # JWT
  jwt:
    secret: ${JWT_SECRET:default-dev-secret-change-in-production}
    access-token-expiry: 1800000       # 30 minutes
    refresh-token-expiry: 604800000    # 7 days
    issuer: inventory-system

  # CORS
  cors:
    allowed-origins: http://localhost:5173,http://localhost:8080
    allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600

  # Reports
  reports:
    template-path: classpath:reports/
    output-path: ${java.io.tmpdir}/inventory-reports/
    company-logo-path: classpath:static/images/logo.png

  # POS
  pos:
    receipt-template: classpath:templates/receipt/pos-receipt.html
    receipt-width: 80mm
    printing-enabled: false    # System works with or without printer
    auto-print: false          # Auto-print on sale complete (requires printing-enabled=true)
    kiosk-mode: false          # Enable for dedicated POS terminals
    barcode-scanner: true      # Enable barcode scanner keyboard input

  # Pagination defaults
  pagination:
    default-page-size: 20
    max-page-size: 100

  # Audit
  audit:
    enabled: true
    log-ip-address: true

# ─── Logging ─────────────────────────────────────────────────────
logging:
  level:
    root: INFO
    com.joven.inventory: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  file:
    name: logs/inventory-system.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30

# ─── Springdoc / Swagger ─────────────────────────────────────────
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
```

---

## application-dev.yml (Development Overrides)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila
    username: root
    password: ""
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate

logging:
  level:
    com.joven.inventory: DEBUG
    org.hibernate.SQL: DEBUG
```

---

## application-prod.yml (Production Overrides)

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
  jpa:
    show-sql: false

app:
  jwt:
    secret: ${JWT_SECRET}
  cors:
    allowed-origins: ${CORS_ORIGINS}

logging:
  level:
    root: WARN
    com.joven.inventory: INFO
    org.hibernate.SQL: WARN
  file:
    name: /var/log/inventory/inventory-system.log
```

---

## config/validation-messages.properties

```properties
# ─── Common ───────────────────────────────────────────────────────
field.required={0} is required
field.size.max={0} must not exceed {1} characters
field.size.range={0} must be between {1} and {2} characters
field.invalid.format={0} has invalid format
field.must.be.positive={0} must be a positive number
field.must.be.zero.or.positive={0} must be zero or a positive number
field.already.exists={0} already exists
field.not.found={0} not found

# ─── Item ─────────────────────────────────────────────────────────
item.code.required=Item code is required
item.code.size=Item code must not exceed 50 characters
item.code.exists=Item code already exists: {0}
item.name.required=Item name is required
item.name.size=Item name must not exceed 200 characters
item.category.required=Category is required
item.unit.required=Unit of measure is required
item.price.required=Selling price is required
item.price.min=Selling price must be zero or positive
item.cost.required=Cost price is required
item.cost.min=Cost price must be zero or positive
item.not.found=Item not found: {0}
item.inactive=Item is inactive and cannot be used in transactions

# ─── Category ─────────────────────────────────────────────────────
category.name.required=Category name is required
category.name.size=Category name must not exceed 100 characters
category.name.exists=Category name already exists: {0}
category.not.found=Category not found: {0}

# ─── Customer ─────────────────────────────────────────────────────
customer.name.required=Customer name is required
customer.name.size=Customer name must not exceed 200 characters
customer.not.found=Customer not found: {0}
customer.tin.format=TIN format must be XXX-XXX-XXX-XXX

# ─── Supplier ─────────────────────────────────────────────────────
supplier.name.required=Supplier name is required
supplier.name.size=Supplier name must not exceed 200 characters
supplier.not.found=Supplier not found: {0}

# ─── Stock ────────────────────────────────────────────────────────
stock.quantity.required=Quantity is required
stock.quantity.min=Quantity must be greater than zero
stock.insufficient=Insufficient stock. Available: {0}, Requested: {1}
stock.item.inactive=Cannot transact on inactive item

# ─── POS / Sale ──────────────────────────────────────────────────
sale.items.required=At least one item is required
sale.payment.required=Payment information is required
sale.amount.insufficient=Amount tendered is less than total amount due
sale.not.found=Sale not found: {0}
sale.already.voided=Sale is already voided
sale.void.reason.required=Void reason is required
sale.shift.required=No open shift. Please open a shift before making sales.

# ─── Shift ────────────────────────────────────────────────────────
shift.already.open=You already have an open shift
shift.not.open=No open shift found
shift.opening.required=Opening amount is required
shift.closing.required=Closing amount is required

# ─── User ─────────────────────────────────────────────────────────
user.username.required=Username is required
user.username.size=Username must be between 3 and 50 characters
user.username.exists=Username already exists: {0}
user.password.required=Password is required
user.password.size=Password must be at least 8 characters
user.fullname.required=Full name is required
user.not.found=User not found: {0}
user.invalid.credentials=Invalid username or password
user.account.disabled=Account is disabled

# ─── Discount ─────────────────────────────────────────────────────
discount.exceeds.limit=Discount exceeds maximum allowed: {0}%
discount.value.invalid=Discount value must be positive

# ─── Tax ──────────────────────────────────────────────────────────
tax.rate.invalid=Tax rate must be between 0 and 100

# ─── Document ─────────────────────────────────────────────────────
document.number.generation.failed=Failed to generate document number
```

---

## config/error-messages.properties

```properties
# ─── System Errors ────────────────────────────────────────────────
error.internal=An unexpected error occurred. Please try again.
error.unauthorized=Authentication required. Please login.
error.forbidden=You do not have permission to perform this action.
error.not.found=The requested resource was not found.
error.duplicate=A record with this value already exists.
error.validation=Please check your input and try again.
error.business.rule=Operation cannot be completed due to business rules.

# ─── Specific ─────────────────────────────────────────────────────
error.token.expired=Your session has expired. Please login again.
error.token.invalid=Invalid authentication token.
error.file.upload.size=File size exceeds the maximum limit of {0}.
error.report.generation=Failed to generate report. Please try again.
error.concurrent.modification=This record was modified by another user. Please refresh and try again.
```

---

## config/navigation.yml

Defines the sidebar menu structure. UI reads this to build navigation dynamically.

```yaml
# Navigation menu configuration
# Each module can be enabled/disabled without code changes
# Permission field maps to bitwise permission constant

navigation:
  - id: dashboard
    label: Dashboard
    icon: bi-speedometer2
    route: /dashboard
    permission: VIEW_DASHBOARD
    order: 1

  - id: pos
    label: POS Terminal
    icon: bi-cart-check
    route: /pos
    permission: USE_POS
    order: 2
    badge: active-shift  # Shows badge if shift is open

  - id: inventory
    label: Inventory
    icon: bi-box-seam
    order: 3
    children:
      - id: items
        label: Items
        route: /items
        permission: VIEW_ITEMS
      - id: categories
        label: Categories
        route: /categories
        permission: VIEW_CATEGORIES
      - id: stock-overview
        label: Stock Overview
        route: /stock
        permission: VIEW_STOCK
      - id: stock-in
        label: Stock In
        route: /stock/in
        permission: MANAGE_STOCK_IN
      - id: stock-out
        label: Stock Out
        route: /stock/out
        permission: MANAGE_STOCK_OUT
      - id: stock-adjust
        label: Stock Adjustment
        route: /stock/adjust
        permission: MANAGE_STOCK_ADJ
      - id: transactions
        label: Transactions
        route: /transactions
        permission: VIEW_TRANSACTIONS

  - id: sales
    label: Sales
    icon: bi-receipt
    order: 4
    children:
      - id: sales-list
        label: Sales History
        route: /sales
        permission: USE_POS
      - id: shifts
        label: Shifts
        route: /shifts
        permission: MANAGE_SHIFTS

  - id: contacts
    label: Contacts
    icon: bi-people
    order: 5
    children:
      - id: customers
        label: Customers
        route: /customers
        permission: VIEW_CUSTOMERS
      - id: suppliers
        label: Suppliers
        route: /suppliers
        permission: VIEW_SUPPLIERS

  - id: reports
    label: Reports
    icon: bi-file-earmark-bar-graph
    route: /reports
    permission: VIEW_REPORTS
    order: 6

  - id: admin
    label: Administration
    icon: bi-gear
    order: 7
    children:
      - id: users
        label: Users
        route: /users
        permission: MANAGE_USERS
      - id: addons
        label: Add-ons
        route: /addons
        permission: MANAGE_ADDONS
      - id: settings
        label: Settings
        route: /settings
        permission: MANAGE_SETTINGS
      - id: audit-trail
        label: Audit Trail
        route: /audit
        permission: VIEW_AUDIT_TRAIL
```

---

## config/forms.yml

Defines form field configurations per module. Frontend reads this to dynamically render forms.

```yaml
# Form configuration
# Fields can be added/removed/reordered without code changes
# type: text, number, decimal, textarea, select, date, checkbox, search-select
# required: validation enforcement
# visible: show/hide dynamically based on settings

forms:
  item:
    title: Item
    fields:
      - name: itemCode
        label: Item Code
        type: text
        maxLength: 50
        required: true
        placeholder: "e.g., ITM-00001"
        autoGenerate: item_code_auto_generate  # References app_setting key

      - name: name
        label: Item Name
        type: text
        maxLength: 200
        required: true

      - name: description
        label: Description
        type: textarea
        maxLength: 2000
        required: false

      - name: categoryId
        label: Category
        type: search-select
        source: /api/categories?active=true
        displayField: name
        valueField: id
        required: true

      - name: unit
        label: Unit of Measure
        type: select
        source: config  # Loads from units-of-measure.yml
        required: true

      - name: price
        label: Selling Price (₱)
        type: decimal
        min: 0
        precision: 2
        required: true

      - name: costPrice
        label: Cost Price (₱)
        type: decimal
        min: 0
        precision: 2
        required: true

      - name: reorderLevel
        label: Reorder Level
        type: number
        min: 0
        required: false
        default: 0

      - name: taxable
        label: Taxable
        type: checkbox
        default: true
        visibleWhen: tax_enabled  # Only show if tax is enabled in settings

  category:
    title: Category
    fields:
      - name: name
        label: Category Name
        type: text
        maxLength: 100
        required: true

      - name: description
        label: Description
        type: textarea
        maxLength: 255
        required: false

  customer:
    title: Customer
    fields:
      - name: name
        label: Customer / Business Name
        type: text
        maxLength: 200
        required: true

      - name: tin
        label: TIN
        type: text
        maxLength: 20
        required: false
        placeholder: "XXX-XXX-XXX-XXX"
        mask: "###-###-###-###"

      - name: address
        label: Address
        type: textarea
        maxLength: 500
        required: false

      - name: contactPerson
        label: Contact Person
        type: text
        maxLength: 150
        required: false

      - name: contactNumber
        label: Contact Number
        type: text
        maxLength: 20
        required: false

      - name: email
        label: Email
        type: text
        maxLength: 150
        required: false

  supplier:
    title: Supplier
    fields:
      - name: name
        label: Supplier / Business Name
        type: text
        maxLength: 200
        required: true

      - name: tin
        label: TIN
        type: text
        maxLength: 20
        required: false
        placeholder: "XXX-XXX-XXX-XXX"

      - name: address
        label: Address
        type: textarea
        maxLength: 500
        required: false

      - name: contactPerson
        label: Contact Person
        type: text
        maxLength: 150
        required: false

      - name: contactNumber
        label: Contact Number
        type: text
        maxLength: 20
        required: false

      - name: email
        label: Email
        type: text
        maxLength: 150
        required: false

  stockIn:
    title: Stock In
    fields:
      - name: itemId
        label: Item
        type: search-select
        source: /api/items?active=true
        displayField: name
        valueField: id
        required: true
        showStock: true  # Show current stock next to selection

      - name: quantity
        label: Quantity
        type: number
        min: 1
        required: true

      - name: unitCost
        label: Unit Cost (₱)
        type: decimal
        min: 0
        precision: 2
        required: true
        autoFill: costPrice  # Auto-fill from item's cost price

      - name: supplierId
        label: Supplier
        type: search-select
        source: /api/suppliers?active=true
        displayField: name
        valueField: id
        required: false

      - name: documentType
        label: Document Type
        type: select
        source: config  # From document-types.yml, filtered for IN
        required: false
        default: NONE

      - name: referenceNo
        label: Reference No.
        type: text
        maxLength: 50
        required: false

      - name: taxEnabled
        label: Apply Tax
        type: checkbox
        default: false
        visibleWhen: tax_enabled

      - name: transactionDate
        label: Transaction Date
        type: date
        required: true
        default: today

      - name: remarks
        label: Remarks
        type: textarea
        maxLength: 500
        required: false

  stockOut:
    title: Stock Out
    fields:
      - name: itemId
        label: Item
        type: search-select
        source: /api/items?active=true
        displayField: name
        valueField: id
        required: true
        showStock: true

      - name: quantity
        label: Quantity
        type: number
        min: 1
        required: true
        validateStock: true  # Cannot exceed current stock

      - name: unitPrice
        label: Unit Price (₱)
        type: decimal
        min: 0
        precision: 2
        required: true
        autoFill: price  # Auto-fill from item's selling price

      - name: customerId
        label: Customer
        type: search-select
        source: /api/customers?active=true
        displayField: name
        valueField: id
        required: false

      - name: discountType
        label: Discount Type
        type: select
        options: [NONE, FIXED, PERCENTAGE]
        default: NONE
        visibleWhen: enable_discounts

      - name: discountValue
        label: Discount Value
        type: decimal
        min: 0
        precision: 2
        visibleWhen: enable_discounts

      - name: documentType
        label: Document Type
        type: select
        source: config
        required: false
        default: NONE

      - name: taxEnabled
        label: Apply Tax
        type: checkbox
        default: false
        visibleWhen: tax_enabled

      - name: transactionDate
        label: Transaction Date
        type: date
        required: true
        default: today

      - name: remarks
        label: Remarks
        type: textarea
        maxLength: 500
        required: false
```

---

## config/reports.yml

Registry of all available reports. UI reads this to build the reports page.

```yaml
# Report registry
# Add/remove reports here without changing code
# Frontend dynamically builds report selection UI from this config

reports:
  inventory:
    label: Inventory Reports
    icon: bi-box-seam
    items:
      - id: stock-level
        label: Stock Level Report
        description: Current stock quantities for all items
        endpoint: /api/reports/stock-level
        template: inventory/stock-level.jrxml
        parameters:
          - name: categoryId
            label: Category
            type: select
            source: /api/categories?active=true
            required: false
          - name: active
            label: Active Only
            type: checkbox
            default: true

      - id: stock-movement
        label: Stock Movement Report
        description: In/Out/Adjustment summary per item
        endpoint: /api/reports/stock-movement
        template: inventory/stock-movement.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true
          - name: categoryId
            label: Category
            type: select
            source: /api/categories?active=true
            required: false

      - id: low-stock
        label: Low Stock Alert
        description: Items at or below reorder level
        endpoint: /api/reports/low-stock
        template: inventory/low-stock.jrxml
        parameters: []

      - id: transaction-summary
        label: Transaction Summary
        description: All transactions in a period
        endpoint: /api/reports/transaction-summary
        template: inventory/transaction-summary.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true
          - name: type
            label: Transaction Type
            type: select
            options: [ALL, IN, OUT, ADJUSTMENT]
            default: ALL

      - id: item-list
        label: Item List
        description: Full item catalog
        endpoint: /api/reports/item-list
        template: inventory/item-list.jrxml
        parameters:
          - name: categoryId
            label: Category
            type: select
            source: /api/categories?active=true
            required: false

      - id: inventory-count
        label: Inventory Count Sheet
        description: Physical count form for stock-taking
        endpoint: /api/reports/inventory-count
        template: inventory/inventory-count.jrxml
        parameters:
          - name: categoryId
            label: Category
            type: select
            source: /api/categories?active=true
            required: false

      - id: stock-valuation
        label: Stock Valuation
        description: Total inventory value (cost × quantity)
        endpoint: /api/reports/stock-valuation
        template: inventory/stock-valuation.jrxml
        parameters:
          - name: categoryId
            label: Category
            type: select
            source: /api/categories?active=true
            required: false

  financial:
    label: Financial Reports
    icon: bi-currency-exchange
    items:
      - id: gross-profit
        label: Gross Profit Report
        description: Revenue minus cost per item
        endpoint: /api/reports/gross-profit
        template: financial/gross-profit.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true
          - name: categoryId
            label: Category
            type: select
            source: /api/categories?active=true
            required: false

      - id: profit-share
        label: Profit Share Summary
        description: Each item's contribution to total profit
        endpoint: /api/reports/profit-share
        template: financial/profit-share.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true
          - name: groupBy
            label: Group By
            type: select
            options: [ITEM, CATEGORY]
            default: ITEM

      - id: sales-summary
        label: Sales Summary
        description: Daily/monthly sales with VAT breakdown
        endpoint: /api/reports/sales-summary
        template: financial/sales-summary.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true

      - id: purchase-summary
        label: Purchase Summary
        description: Stock-in costs by supplier
        endpoint: /api/reports/purchase-summary
        template: financial/purchase-summary.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true
          - name: supplierId
            label: Supplier
            type: select
            source: /api/suppliers?active=true
            required: false

      - id: vat-summary
        label: VAT Summary (BIR)
        description: Vatable, exempt, zero-rated breakdown
        endpoint: /api/reports/vat-summary
        template: financial/vat-summary.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true

  pos:
    label: POS Reports
    icon: bi-cart-check
    items:
      - id: daily-sales
        label: Daily Sales (Z-Reading)
        description: End-of-day sales summary
        endpoint: /api/reports/pos/daily-sales
        template: pos/daily-sales.jrxml
        parameters:
          - name: date
            label: Date
            type: date
            required: true
            default: today

      - id: shift-report
        label: Shift Report (X-Reading)
        description: Per-shift sales and cash summary
        endpoint: /api/reports/pos/shift
        template: pos/shift-report.jrxml
        parameters:
          - name: shiftId
            label: Shift
            type: select
            source: /api/pos/shifts?status=CLOSED
            displayField: label
            valueField: id
            required: true

      - id: sales-by-payment
        label: Sales by Payment Method
        description: Breakdown by cash, GCash, bank, credit
        endpoint: /api/reports/pos/sales-by-payment
        template: pos/sales-by-payment.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true

      - id: sales-by-cashier
        label: Sales by Cashier
        description: Per-cashier sales summary
        endpoint: /api/reports/pos/sales-by-cashier
        template: pos/sales-by-cashier.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true

      - id: top-selling
        label: Top Selling Items
        description: Items ranked by quantity or revenue
        endpoint: /api/reports/pos/top-selling
        template: pos/top-selling.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true
          - name: limit
            label: Top N
            type: number
            default: 20
            min: 5
            max: 100

      - id: voided-transactions
        label: Voided Transactions
        description: All voided sales with reasons
        endpoint: /api/reports/pos/voided
        template: pos/voided-transactions.jrxml
        parameters:
          - name: from
            label: Date From
            type: date
            required: true
          - name: to
            label: Date To
            type: date
            required: true

      - id: hourly-sales
        label: Hourly Sales
        description: Sales by hour for peak analysis
        endpoint: /api/reports/pos/hourly-sales
        template: pos/hourly-sales.jrxml
        parameters:
          - name: date
            label: Date
            type: date
            required: true
            default: today
```

---

## config/units-of-measure.yml

```yaml
# Units of measure
# Add or remove units without code changes
# Frontend populates dropdown from this config

units:
  - code: pcs
    label: Pieces
  - code: kg
    label: Kilogram
  - code: g
    label: Gram
  - code: L
    label: Liter
  - code: mL
    label: Milliliter
  - code: box
    label: Box
  - code: pack
    label: Pack
  - code: ream
    label: Ream
  - code: doz
    label: Dozen
  - code: roll
    label: Roll
  - code: gal
    label: Gallon
  - code: sack
    label: Sack
  - code: case
    label: Case
  - code: bundle
    label: Bundle
  - code: pair
    label: Pair
  - code: set
    label: Set
  - code: can
    label: Can
  - code: bot
    label: Bottle
  - code: jar
    label: Jar
  - code: tray
    label: Tray
```

---

## config/payment-methods.yml

```yaml
# Payment methods available in POS
# Can add new methods without code changes (if ENUM allows)

payment_methods:
  - code: CASH
    label: Cash
    icon: bi-cash
    requires_reference: false
    active: true

  - code: GCASH
    label: GCash
    icon: bi-phone
    requires_reference: true
    reference_label: GCash Ref #
    active: true

  - code: BANK_TRANSFER
    label: Bank Transfer
    icon: bi-bank
    requires_reference: true
    reference_label: Bank Ref #
    active: true

  - code: CREDIT
    label: Credit / Charge
    icon: bi-credit-card
    requires_reference: false
    requires_customer: true
    active: true
```

---

## config/document-types.yml

```yaml
# Document types
# Filtered by transaction type in UI

document_types:
  - code: OR
    label: Official Receipt
    applicable_for: [OUT, POS]
    sequential: true
    prefix_setting: or_prefix
    next_number_setting: or_next_number

  - code: SI
    label: Sales Invoice
    applicable_for: [OUT, POS]
    sequential: true
    prefix_setting: si_prefix
    next_number_setting: si_next_number

  - code: DR
    label: Delivery Receipt
    applicable_for: [OUT]
    sequential: true
    prefix_setting: dr_prefix
    next_number_setting: dr_next_number

  - code: PO
    label: Purchase Order
    applicable_for: [IN]
    sequential: true
    prefix_setting: po_prefix
    next_number_setting: po_next_number

  - code: RR
    label: Receiving Report
    applicable_for: [IN]
    sequential: true
    prefix_setting: rr_prefix
    next_number_setting: rr_next_number

  - code: NONE
    label: No Document
    applicable_for: [IN, OUT, ADJUSTMENT]
    sequential: false
```

---

## config/permissions.yml

```yaml
# Permission definitions
# Used by both backend (constants) and frontend (UI checkboxes)
# Bit positions must never change once deployed (append only)

permissions:
  - bit: 0
    code: VIEW_DASHBOARD
    label: View Dashboard
    group: General

  - bit: 1
    code: VIEW_ITEMS
    label: View Items
    group: Inventory

  - bit: 2
    code: MANAGE_ITEMS
    label: Manage Items
    group: Inventory

  - bit: 3
    code: VIEW_CATEGORIES
    label: View Categories
    group: Inventory

  - bit: 4
    code: MANAGE_CATEGORIES
    label: Manage Categories
    group: Inventory

  - bit: 5
    code: VIEW_CUSTOMERS
    label: View Customers
    group: Contacts

  - bit: 6
    code: MANAGE_CUSTOMERS
    label: Manage Customers
    group: Contacts

  - bit: 7
    code: VIEW_SUPPLIERS
    label: View Suppliers
    group: Contacts

  - bit: 8
    code: MANAGE_SUPPLIERS
    label: Manage Suppliers
    group: Contacts

  - bit: 9
    code: VIEW_STOCK
    label: View Stock
    group: Inventory

  - bit: 10
    code: MANAGE_STOCK_IN
    label: Stock In
    group: Inventory

  - bit: 11
    code: MANAGE_STOCK_OUT
    label: Stock Out
    group: Inventory

  - bit: 12
    code: MANAGE_STOCK_ADJ
    label: Stock Adjustment
    group: Inventory

  - bit: 13
    code: VIEW_TRANSACTIONS
    label: View Transactions
    group: Inventory

  - bit: 14
    code: USE_POS
    label: Use POS Terminal
    group: POS

  - bit: 15
    code: VOID_SALES
    label: Void Sales
    group: POS

  - bit: 16
    code: MANAGE_SHIFTS
    label: Manage Shifts
    group: POS

  - bit: 17
    code: VIEW_REPORTS
    label: View Reports
    group: Reports

  - bit: 18
    code: VIEW_AUDIT_TRAIL
    label: View Audit Trail
    group: Administration

  - bit: 19
    code: MANAGE_USERS
    label: Manage Users
    group: Administration

  - bit: 20
    code: MANAGE_SETTINGS
    label: Manage Settings
    group: Administration

  - bit: 21
    code: MANAGE_ADDONS
    label: Manage Add-ons
    group: Administration

  - bit: 22
    code: APPROVE_TRANSACTIONS
    label: Approve Stock Transactions
    group: Inventory

  - bit: 23
    code: CANCEL_TRANSACTIONS
    label: Cancel Stock Transactions
    group: Inventory

  - bit: 24
    code: REPRINT
    label: Reprint Transactions & Receipts
    group: General

# Role presets (applied on user creation)
role_presets:
  ADMIN:
    description: Full access to all features
    access_rights: 33554431  # All bits 0-24 set

  CASHIER:
    description: POS terminal, view items and stock
    access_rights: 82435    # Dashboard + View Items + View Stock + POS + Shifts

  STOCK_CLERK:
    description: Inventory operations with approval
    access_rights: 4210687  # Dashboard + Items + Stock + Transactions + Approve

  VIEWER:
    description: View-only access to all modules
    access_rights: 139947   # Dashboard + all VIEW permissions
```

---

## How Frontend Uses These Configs

### Loading Configuration at Startup

```javascript
// api/config.js
export async function loadAppConfig() {
  const [navigation, forms, reports, permissions, units, payments, documents] = await Promise.all([
    axios.get('/api/config/navigation'),
    axios.get('/api/config/forms'),
    axios.get('/api/config/reports'),
    axios.get('/api/config/permissions'),
    axios.get('/api/config/units'),
    axios.get('/api/config/payment-methods'),
    axios.get('/api/config/document-types'),
  ]);
  return { navigation, forms, reports, permissions, units, payments, documents };
}
```

### Backend Config Endpoint

```java
/**
 * REST controller for serving application configuration to the frontend.
 * Returns navigation, forms, reports, permissions, and other config data
 * that the Vue frontend uses to build UI dynamically.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    /** Returns sidebar navigation structure (permission-aware). */
    @GetMapping("/navigation")
    public ResponseEntity<?> getNavigation() { /* Load navigation.yml */ }

    /** Returns form field definitions for all modules. */
    @GetMapping("/forms")
    public ResponseEntity<?> getForms() { /* Load forms.yml */ }

    /** Returns report registry with parameters. */
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() { /* Load reports.yml */ }

    /** Returns permission definitions and role presets. */
    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions() { /* Load permissions.yml */ }

    /** Returns available units of measure. */
    @GetMapping("/units")
    public ResponseEntity<?> getUnits() { /* Load units-of-measure.yml */ }

    /** Returns available payment methods for POS. */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethods() { /* Load payment-methods.yml */ }

    /** Returns document type definitions. */
    @GetMapping("/document-types")
    public ResponseEntity<?> getDocumentTypes() { /* Load document-types.yml */ }
}
```

---

## Benefits of This Approach

| Benefit | How |
|---------|-----|
| **Add a new form field** | Add to `forms.yml` → UI renders it automatically |
| **Add a new report** | Add to `reports.yml` + create .jrxml → appears in reports page |
| **Add a new menu item** | Add to `navigation.yml` → sidebar updates |
| **Add a unit of measure** | Add to `units-of-measure.yml` → appears in dropdown |
| **Add a payment method** | Add to `payment-methods.yml` → appears in POS |
| **Change validation message** | Edit `validation-messages.properties` → no recompile |
| **Add a permission** | Add to `permissions.yml` + new bit → UI shows new checkbox |
| **Localize messages** | Create `_fil.properties` file → Filipino messages |
| **Switch environment** | Change `spring.profiles.active` → different DB, logging, etc. |

---

## Summary

| Config File | Purpose | Used By |
|-------------|---------|---------|
| application.yml | JDBC, JPA, JWT, CORS, logging | Backend |
| application-dev.yml | Dev overrides | Backend (dev) |
| application-prod.yml | Production overrides | Backend (prod) |
| validation-messages.properties | Field validation messages | Backend + Frontend |
| error-messages.properties | Business/system error messages | Backend |
| navigation.yml | Sidebar menu structure | Frontend |
| forms.yml | Form field definitions | Frontend |
| reports.yml | Report registry + parameters | Frontend + Backend |
| permissions.yml | Permission definitions + presets | Frontend + Backend |
| units-of-measure.yml | UOM dropdown options | Frontend |
| payment-methods.yml | POS payment options | Frontend |
| document-types.yml | Document type registry | Frontend + Backend |
