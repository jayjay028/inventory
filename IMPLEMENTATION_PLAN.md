# Inventory System - Implementation Plan

## Project Overview

A web-based inventory management system with a REST API backend (Spring MVC + Java 25) and a responsive frontend. The application is mobile-friendly and provides inventory operations including item management, stock tracking, inventory transactions, audit trail, and report generation via JasperReports.

## Technology Stack

### Backend
| Layer | Technology |
|-------|-----------|
| Language | Java 25 |
| Framework | Spring Boot 3.x (Spring MVC) |
| Build Tool | Maven |
| Database | MySQL 8.0 |
| ORM | Hibernate / JPA |
| Security | Spring Security + JWT |
| Reporting | JasperReports |
| Validation | Jakarta Bean Validation |
| Documentation | Springdoc OpenAPI (Swagger UI) |
| Server | Apache Tomcat 9 (external, WAR deployment) |
| Packaging | WAR |

### Frontend
| Layer | Technology |
|-------|-----------|
| Framework | Vue 3 (Composition API) |
| Language | JavaScript |
| Build Tool | Vite |
| CSS Framework | Bootstrap 5 (mobile-first) |
| HTTP Client | Axios |
| Router | Vue Router |
| State Management | Pinia |
| E2E Testing | Playwright |
| Reports | PDF viewer (rendered by JasperReports from API) |

## Supported Devices

| Device | Screen Size | Primary Use | Layout |
|--------|-------------|-------------|--------|
| Desktop / Laptop | 1024px+ | Full admin, reports, inventory management | Sidebar + main content |
| Tablet | 768px–1024px | POS terminal, stock operations, management on-the-go | Collapsible sidebar, touch-optimized |
| Mobile Phone | 320px–768px | Quick stock checks, approvals, lightweight POS | Stacked layout, bottom navigation |
| POS Unit (dedicated) | 800px–1024px (touch) | Cashier-only POS terminal | Full-screen, kiosk-ready, no sidebar |

### Responsive Design Strategy
- **Bootstrap 5 breakpoints**: xs (<576), sm (≥576), md (≥768), lg (≥992), xl (≥1200), xxl (≥1400)
- **Mobile-first CSS**: Base styles for smallest screen, progressively enhanced
- **Touch targets**: Minimum 48×48px on all interactive elements
- **POS Terminal**: Dedicated full-screen layout, no browser chrome dependency, optimized for touch
- **Data tables**: Horizontal scroll on mobile, or switch to card-based list view
- **Navigation**: Sidebar on desktop, hamburger + off-canvas on tablet, bottom nav bar on mobile
- **Forms**: Single-column on mobile, two-column on tablet/desktop
- **Receipt printing**: Works from any device via browser print dialog or connected thermal printer
- **Barcode scanner**: Accepts keyboard input (USB/Bluetooth scanner acts as keyboard) on any device
- **Kiosk mode**: POS unit can run in browser full-screen (F11) or as a Progressive Web App (PWA) for dedicated terminals

## Architecture

### API-Based Architecture

```
Frontend (Vue 3 SPA + Bootstrap 5)
       ↕ REST API (JSON + PDF)
Backend (Spring MVC)
  Controller → Service → Repository → MySQL
      ↕            ↕
     DTO        Entity
      ↕
    Mapper
```

### Package Structure

```
com.joven.inventory
├── config/                  # App, Security, CORS, Jasper config
├── controller/              # REST controllers
├── dto/
│   ├── request/             # Request DTOs
│   └── response/            # Response DTOs
├── entity/                  # JPA entities
├── enums/                   # Enumerations
├── exception/               # Custom exceptions and global handler
├── mapper/                  # Entity-DTO mappers
├── repository/              # JPA repositories
├── report/                  # JasperReport service and templates
├── security/                # JWT filter, provider, utilities
├── service/                 # Business logic interfaces
│   └── impl/                # Service implementations
├── util/                    # Utility classes
└── audit/                   # Audit trail interceptor/listener
```

### Frontend Structure (Vue 3 SPA)

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── public/
│   └── favicon.ico
└── src/
    ├── main.js                  # App entry point
    ├── App.vue                  # Root component
    ├── api/
    │   ├── axios.js             # Axios instance with JWT interceptor
    │   ├── auth.js              # Auth API calls
    │   ├── categories.js        # Category API calls
    │   ├── items.js             # Item API calls
    │   ├── customers.js         # Customer API calls
    │   ├── suppliers.js         # Supplier API calls
    │   ├── stock.js             # Stock API calls
    │   ├── transactions.js      # Transaction API calls
    │   ├── pos.js               # POS sales, shifts, item lookup
    │   ├── users.js             # User API calls
    │   ├── addons.js            # Add-on master API calls
    │   ├── settings.js          # App settings API calls
    │   ├── audit.js             # Audit trail API calls
    │   └── reports.js           # Report API calls
    ├── router/
    │   └── index.js             # Vue Router configuration
    ├── stores/
    │   ├── auth.js              # Auth state (Pinia)
    │   └── app.js               # App-wide state (Pinia)
    ├── views/
    │   ├── Login.vue
    │   ├── Dashboard.vue
    │   ├── categories/
    │   │   ├── CategoryList.vue
    │   │   └── CategoryForm.vue
    │   ├── items/
    │   │   ├── ItemList.vue
    │   │   └── ItemForm.vue
    │   ├── customers/
    │   │   ├── CustomerList.vue
    │   │   └── CustomerForm.vue
    │   ├── suppliers/
    │   │   ├── SupplierList.vue
    │   │   └── SupplierForm.vue
    │   ├── stock/
    │   │   ├── StockOverview.vue
    │   │   ├── StockIn.vue
    │   │   ├── StockOut.vue
    │   │   └── StockAdjust.vue
    │   ├── transactions/
    │   │   └── TransactionList.vue
    │   ├── reports/
    │   │   └── Reports.vue
    │   ├── users/
    │   │   ├── UserList.vue
    │   │   └── UserForm.vue
    │   ├── settings/
    │   │   └── Settings.vue
    │   ├── pos/
    │   │   ├── PosTerminal.vue      # Main POS cashier screen (full-screen)
    │   │   ├── PosCart.vue           # Cart component (items list, qty, totals)
    │   │   ├── PosPayment.vue       # Payment dialog (amount, method, change)
    │   │   ├── PosReceipt.vue       # Receipt preview / print
    │   │   ├── PosShiftOpen.vue     # Open shift dialog (opening amount)
    │   │   ├── PosShiftClose.vue    # Close shift (cash count, summary)
    │   │   ├── SalesList.vue        # Sales history (filterable)
    │   │   └── SaleDetail.vue       # Sale detail view (items, payments)
    │   └── audit/
    │       └── AuditTrail.vue
    ├── components/
    │   ├── layout/
    │   │   ├── AppLayout.vue    # Main layout (navbar + sidebar + content)
    │   │   ├── Navbar.vue
    │   │   └── Sidebar.vue
    │   ├── common/
    │   │   ├── DataTable.vue    # Reusable table with pagination & search
    │   │   ├── ConfirmDialog.vue
    │   │   ├── Toast.vue
    │   │   ├── LoadingSpinner.vue
    │   │   ├── PageHeader.vue
    │   │   └── FormInput.vue
    │   └── stock/
    │       └── StockBadge.vue   # Color-coded stock level indicator
    ├── composables/
    │   ├── useAuth.js           # Auth logic (login, logout, token refresh)
    │   ├── usePagination.js     # Pagination helper
    │   └── useToast.js          # Toast notification helper
    └── assets/
        └── css/
            └── app.css          # Custom styles
```

### Frontend Build & Deployment
- Developed in `frontend/` directory with Vite dev server (hot reload)
- Production build outputs to `src/main/resources/static/`
- Final WAR includes the compiled Vue app — no separate frontend server needed
- Vite proxy configured to forward `/api` calls to Spring Boot during development

### Frontend Testing (Playwright)

```
frontend/
├── e2e/
│   ├── playwright.config.js       # Playwright configuration
│   ├── tests/
│   │   ├── auth.spec.js           # Login, logout, token refresh
│   │   ├── categories.spec.js    # Category CRUD flow
│   │   ├── items.spec.js         # Item CRUD flow
│   │   ├── customers.spec.js     # Customer CRUD flow
│   │   ├── suppliers.spec.js     # Supplier CRUD flow
│   │   ├── stock-in.spec.js      # Stock In with tax/discount
│   │   ├── stock-out.spec.js     # Stock Out with validation
│   │   ├── stock-adjust.spec.js  # Stock Adjustment
│   │   ├── pos-sale.spec.js      # Full POS sale flow (cart → pay → receipt)
│   │   ├── pos-void.spec.js      # Void sale flow
│   │   ├── pos-shift.spec.js     # Shift open/close
│   │   ├── reports.spec.js       # Generate and verify PDF reports
│   │   ├── settings.spec.js      # Admin settings update
│   │   ├── users.spec.js         # User management (Admin)
│   │   └── audit-trail.spec.js   # Audit trail viewing
│   └── fixtures/
│       ├── test-data.js           # Reusable test data (items, customers, etc.)
│       └── auth.setup.js          # Login helper for authenticated tests
```

**Playwright Test Strategy:**
- Run against local dev server (Vite + Spring Boot)
- Tests cover critical user flows end-to-end
- Authenticated tests use stored auth state (login once, reuse)
- POS tests validate: cart add/remove, payment, stock deduction, receipt content
- Mobile viewport tests included for responsive verification
- **Device viewports tested:**
  - Desktop: 1280×720
  - Tablet landscape: 1024×768
  - Tablet portrait: 768×1024
  - Mobile: 375×667 (iPhone SE)
  - POS terminal: 1024×768 (touch, full-screen)

## Database Design

> Full schema details are in `DATABASE_SCHEMA.md`

### Tables (16 total)
| # | Table | Purpose |
|---|-------|---------|
| 1 | users | System users (ADMIN, STAFF) |
| 2 | categories | Item categories |
| 3 | items | Item master data with item_code, pricing, taxable flag |
| 4 | customers | Customer master (for OR/SI, PH compliance) |
| 5 | suppliers | Supplier master (for PO/RR) |
| 6 | stock | Current stock per item (1:1) |
| 7 | stock_transactions | All movements with pricing, tax (optional), discount, document info |
| 8 | transaction_addons | Add-on charges per stock transaction |
| 9 | addon_master | Predefined add-on types |
| 10 | audit_trail | Field-level change history |
| 11 | app_settings | Key-value config (company, tax, documents, POS, discounts) |
| 12 | sales | POS sales header (cart total, payment, status) |
| 13 | sale_items | POS sale line items (item snapshot, qty, price) |
| 14 | sale_addons | Add-on charges per POS sale |
| 15 | sale_payments | Split/multiple payment records |
| 16 | shifts | Cashier shift management (open/close, cash count) |

## REST API Design

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/auth/refresh` | Refresh token |
| POST | `/api/auth/logout` | Invalidate token |
| GET | `/api/auth/me` | Get current user info |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all (paginated, searchable) |
| GET | `/api/categories/{id}` | Get by ID |
| POST | `/api/categories` | Create |
| PUT | `/api/categories/{id}` | Update |
| PATCH | `/api/categories/{id}/status` | Activate/deactivate |

### Items
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/items` | List all (paginated, filterable by category/status) |
| GET | `/api/items/{id}` | Get by ID |
| POST | `/api/items` | Create |
| PUT | `/api/items/{id}` | Update |
| PATCH | `/api/items/{id}/status` | Activate/deactivate |
| GET | `/api/items/search?q=` | Search by name/item_code |
| GET | `/api/items/low-stock` | Items below reorder level |

### Customers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | List all (paginated, searchable) |
| GET | `/api/customers/{id}` | Get by ID |
| POST | `/api/customers` | Create |
| PUT | `/api/customers/{id}` | Update |
| PATCH | `/api/customers/{id}/status` | Activate/deactivate |
| GET | `/api/customers/search?q=` | Search by name/TIN |

### Suppliers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/suppliers` | List all (paginated, searchable) |
| GET | `/api/suppliers/{id}` | Get by ID |
| POST | `/api/suppliers` | Create |
| PUT | `/api/suppliers/{id}` | Update |
| PATCH | `/api/suppliers/{id}/status` | Activate/deactivate |
| GET | `/api/suppliers/search?q=` | Search by name/TIN |

### Stock
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stock` | Current stock levels (paginated) |
| GET | `/api/stock/{itemId}` | Stock for specific item |
| POST | `/api/stock/in` | Create stock in (status = CREATED) |
| POST | `/api/stock/out` | Create stock out (status = CREATED) |
| POST | `/api/stock/adjust` | Create stock adjustment (status = CREATED) |
| PATCH | `/api/stock/transactions/{id}/approve` | Approve transaction (stock updated) |
| PATCH | `/api/stock/transactions/{id}/cancel` | Cancel transaction (no stock impact) |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | List (paginated, filterable by date/type/item/status/customer/supplier) |
| GET | `/api/transactions/{id}` | Get by ID (includes addons) |
| GET | `/api/transactions/{id}/print` | Get printable transaction data (reprint) |
| GET | `/api/transactions/pending` | List CREATED (pending approval) transactions |

### Users (Admin only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get by ID |
| POST | `/api/users` | Create user |
| PUT | `/api/users/{id}` | Update user |
| PATCH | `/api/users/{id}/status` | Activate/deactivate |
| PATCH | `/api/users/{id}/password` | Reset password |

### Add-on Master
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/addons` | List all add-on types |
| GET | `/api/addons/{id}` | Get by ID |
| POST | `/api/addons` | Create add-on type |
| PUT | `/api/addons/{id}` | Update |
| PATCH | `/api/addons/{id}/status` | Activate/deactivate |

### Audit Trail
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/audit` | List audit entries (paginated, filterable) |
| GET | `/api/audit/entity/{name}/{id}` | Audit history for specific entity |

### Settings (Admin only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/settings` | Get all settings |
| GET | `/api/settings/{key}` | Get specific setting |
| PUT | `/api/settings` | Update multiple settings |
| PUT | `/api/settings/{key}` | Update single setting |

### POS - Sales
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/pos/sales` | Create a new sale (status = OPEN) |
| GET | `/api/pos/sales` | List sales (paginated, filterable by date/cashier/status) |
| GET | `/api/pos/sales/{id}` | Get sale details with line items, addons, payments |
| PUT | `/api/pos/sales/{id}/items` | Update items in OPEN sale (add/remove/change qty) |
| POST | `/api/pos/sales/{id}/pay` | Process payment (OPEN → PAID, stock deducted) |
| PATCH | `/api/pos/sales/{id}/close` | Close a PAID sale (PAID → CLOSED) |
| POST | `/api/pos/sales/{id}/void` | Void a sale (requires reason, permission) |
| GET | `/api/pos/sales/{id}/receipt` | Get receipt data (for print/reprint, any PAID/CLOSED sale) |
| GET | `/api/pos/sales/today` | Today's sales summary |
| GET | `/api/pos/sales/open` | List currently OPEN sales (for resume) |

### POS - Shift Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/pos/shifts/open` | Open a new shift (with opening amount) |
| POST | `/api/pos/shifts/{id}/close` | Close shift (with cash count) |
| GET | `/api/pos/shifts/current` | Get current open shift for logged-in user |
| GET | `/api/pos/shifts` | List shifts (paginated, filterable) |
| GET | `/api/pos/shifts/{id}` | Shift details with sales summary |

### POS - Quick Item Lookup
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pos/items/search?q=` | Quick search by name/code (fast, for POS screen) |
| GET | `/api/pos/items/{id}/stock` | Check current stock for an item |

### Reports (JasperReports — returns PDF)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/stock-level` | Current stock level report |
| GET | `/api/reports/stock-movement?from=&to=` | Stock movement report |
| GET | `/api/reports/low-stock` | Low stock alert report |
| GET | `/api/reports/transaction-summary?from=&to=` | Transaction summary |
| GET | `/api/reports/item-list` | Item catalog report |
| GET | `/api/reports/gross-profit?from=&to=` | Gross profit report |
| GET | `/api/reports/profit-share?from=&to=` | Profit share breakdown |
| GET | `/api/reports/sales-summary?from=&to=` | Sales summary with VAT breakdown (PH) |
| GET | `/api/reports/purchase-summary?from=&to=` | Purchase summary |
| GET | `/api/reports/vat-summary?from=&to=` | VAT summary for BIR filing (PH) |
| GET | `/api/reports/inventory-count` | Physical count sheet |
| GET | `/api/reports/stock-valuation` | Total inventory value |
| GET | `/api/reports/pos/daily-sales?date=` | POS daily sales (Z-Reading) |
| GET | `/api/reports/pos/shift?id=` | POS shift report (X-Reading) |
| GET | `/api/reports/pos/sales-by-payment?from=&to=` | Sales by payment method |
| GET | `/api/reports/pos/sales-by-cashier?from=&to=` | Sales by cashier |
| GET | `/api/reports/pos/top-selling?from=&to=` | Top selling items |
| GET | `/api/reports/pos/voided?from=&to=` | Voided transactions report |
| GET | `/api/reports/pos/hourly-sales?date=` | Hourly sales breakdown |

### Standard API Response Format

#### Success
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { },
  "timestamp": "2026-08-12T11:00:00"
}
```

#### Success (Paginated)
```json
{
  "success": true,
  "message": "Records retrieved successfully",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "timestamp": "2026-08-12T11:00:00"
}
```

#### Error
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    { "field": "name", "message": "Name is required" }
  ],
  "timestamp": "2026-08-12T11:00:00"
}
```

## Audit Trail Implementation

### Strategy
- Use JPA `@EntityListeners` with a custom `AuditEntityListener`
- Capture CREATE, UPDATE, DELETE actions automatically
- Track field-level changes on UPDATE (old value → new value)
- Record the user performing the action (from SecurityContext)
- Record IP address (from request context)

### What Gets Audited
| Entity | Actions Tracked |
|--------|----------------|
| Category | Create, Update, Deactivate |
| Item | Create, Update, Deactivate |
| Customer | Create, Update, Deactivate |
| Supplier | Create, Update, Deactivate |
| Stock Transaction | Create |
| User | Create, Update, Deactivate, Password Reset |
| App Settings | Update |

### Audit Trail UI
- Filterable by entity, user, date range, action type
- View full change history for any specific record
- Admin-only access

## JasperReports Implementation

### Reports
| Report | Description | Parameters |
|--------|-------------|------------|
| **Inventory Reports** | | |
| Stock Level | Current quantities for all items | Category filter (optional) |
| Stock Movement | In/Out/Adjust summary per item | Date range |
| Low Stock Alert | Items below reorder level | None |
| Transaction Summary | All transactions in a period | Date range, type filter |
| Item List | Full item catalog | Category filter, active filter |
| Inventory Count Sheet | Physical count form for stock-taking | Category filter |
| Stock Valuation | Total inventory value (cost × qty on hand) | As-of date, category filter |
| **Financial Reports** | | |
| Gross Profit | Profit per item (revenue - cost) | Date range, category filter |
| Profit Share | Each item's % contribution to total profit | Date range, group by (item/category) |
| Sales Summary | Daily/monthly sales with VAT breakdown (PH) | Date range |
| Purchase Summary | Stock-in costs summary | Date range, supplier filter |
| VAT Summary | Vatable, exempt, zero-rated breakdown (PH/BIR) | Month/Quarter |
| **POS Reports** | | |
| Daily Sales (Z-Reading) | End-of-day sales summary with totals | Date |
| Shift Report (X-Reading) | Per-shift sales and cash summary | Shift ID |
| Sales by Payment Method | Breakdown by cash/GCash/bank/credit | Date range |
| Sales by Cashier | Per-cashier performance summary | Date range, cashier |
| Top Selling Items | Items ranked by qty or revenue | Date range, limit |
| Voided Transactions | All voided sales with reasons | Date range |
| Hourly Sales | Sales by hour (peak analysis) | Date |

### Technical Details
- `.jrxml` template files stored in `src/main/resources/reports/`
- Compiled to `.jasper` at build time or first use
- Reports served as PDF via API (Content-Type: application/pdf)
- Frontend opens PDF in new tab or embedded viewer
- Data source: JPA query results passed to JasperReports

## Security

### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. Server validates, returns JWT access token + refresh token
3. Frontend stores token in memory (not localStorage for XSS safety)
4. All API calls include `Authorization: Bearer <token>` header
5. Token expiry: 30 minutes (access), 7 days (refresh)

### Authorization
| Role | Access |
|------|--------|
| ADMIN | All permissions (access_rights = all bits set) |
| STAFF | Customizable via bitwise access_rights per user |

**Bitwise Permissions:**
- Each permission is a bit flag (power of 2) stored as `BIGINT` in `users.access_rights`
- 22 permissions covering: Dashboard, Items, Categories, Customers, Suppliers, Stock, POS, Reports, Audit, Users, Settings
- Checked via `(user.accessRights & PERMISSION) != 0`
- Backend: Custom `@RequiresPermission` annotation or checked in service layer
- Frontend: Route guards + conditional UI elements based on access_rights
- Admin UI: Checkbox grid for assigning permissions per user, with role presets

### Security Measures
- BCrypt password hashing
- JWT with expiration
- Role-based endpoint protection via `@PreAuthorize`
- CORS configuration for allowed origins
- Input validation on all endpoints
- Parameterized queries (JPA — SQL injection prevention)
- Rate limiting on login endpoint
- No sensitive data in JWT payload
- Audit logging for all data changes

## UI Pages & Forms

### Login Page
- Username + password form
- "Remember me" option
- Error message display
- Mobile-optimized layout

### Dashboard
- Summary cards: Total Items, Total Stock Value, Low Stock Count, Today's Transactions, Gross Profit (Today/Month)
- Recent transactions table (last 10)
- Low stock alerts list
- Top 5 profit contributors
- Quick action buttons (Stock In, Stock Out)

### Category Management
- Data table with search, pagination
- Modal form for create/edit
- Status toggle (active/inactive)
- Confirmation dialog for deactivation

### Item Management
- Data table with search, filter by category, filter by status
- Full page form for create/edit
- Fields: Item Code (auto-generate option), Name, Description, Category, Unit, Selling Price, Cost Price, Reorder Level, Taxable toggle
- Current stock display inline
- Status toggle

### Customer Management
- Data table with search, pagination
- Modal or page form for create/edit
- Fields: Name, TIN, Address, Contact Person, Contact Number, Email
- Status toggle

### Supplier Management
- Data table with search, pagination
- Modal or page form for create/edit
- Fields: Name, TIN, Address, Contact Person, Contact Number, Email
- Status toggle

### Stock Operations
- **Stock In Form**: Supplier selector (optional), Item selector (searchable), Quantity, Unit Cost, Document Type (PO/RR/None), Document No (auto-generate), Reference No, "Apply Tax" checkbox (if tax enabled), Remarks, Date
- **Stock Out Form**: Customer selector (optional), Item selector (searchable), Quantity (with stock validation), Unit Price, Discount section, Add-ons section, Document Type (OR/SI/DR/None), Document No (auto-generate), "Apply Tax" checkbox (if tax enabled), Remarks, Date
- **Stock Adjustment Form**: Item selector, Adjustment Type (+/-/Set), Quantity, Remarks
- All forms show current stock level for selected item
- Tax fields shown/hidden based on `app_settings.tax_enabled`
- When tax is shown, user can toggle "Apply Tax" per transaction

### Stock Overview
- Table: Item, Item Code, Category, Current Qty, Unit, Reorder Level, Status indicator
- Color coding: Red (below reorder), Yellow (at reorder), Green (above)
- Filter by category, status

### Transaction History
- Table: Date, Item, Type, Qty, Status (CREATED/APPROVED/CANCELLED), Document, Customer/Supplier, Net Amount, Tax, Total, User
- Filter by: date range, transaction type, item, customer, supplier, **status**
- Sortable columns
- **Status badges**: CREATED (yellow/pending), APPROVED (green), CANCELLED (red)
- **Approve button**: On CREATED transactions (permission required)
- **Cancel button**: On CREATED transactions (permission required, with reason)
- **Print/Reprint button**: Available on any APPROVED transaction
- **Pending approvals tab**: Quick view of all CREATED transactions awaiting approval

### Reports Page
- Report selection cards (12 reports)
- Date range pickers where applicable
- Category/type/customer/supplier filters where applicable
- "Generate" button → opens PDF in new tab or embedded viewer

### Settings Page (Admin)
- **Company Info**: Business Name, TIN, Address, Contact, Email, Logo
- **Tax Settings**: Tax Enabled toggle, VAT Registered toggle, Default Tax Rate, Pricing Method (VAT-inclusive/exclusive)
- **Item Settings**: Auto-generate Item Code, Item Code Prefix
- **Document Numbering**: OR/SI/DR/PO/RR prefixes and next numbers, format pattern
- **Discount Settings**: Enable Discounts, Max Discount % per role
- **General**: Default Page Size, Low Stock Alert, Currency Symbol, Timezone

### Audit Trail (Admin)
- Table: Timestamp, User, Entity, Action, Details
- Filter by: entity type, user, date range, action
- Expandable row to show field-level changes

### User Management (Admin)
- Data table with user list
- Modal form for create/edit
- Password reset action
- Status toggle

### POS Terminal (Cashier)
- **Full-screen layout** — no sidebar, dedicated POS interface
- **Desktop/Tablet Landscape Layout**:
  - Left Panel: Item search bar (by name/item_code), item results grid (tap/click to add to cart)
  - Right Panel: Cart list (item, qty +/-, unit price, line total, remove button)
  - Bottom Bar: Subtotal, Discount, Tax, Total, [Pay] button
- **Tablet Portrait Layout**:
  - Top: Search bar + item grid
  - Bottom: Cart summary (expandable), [Pay] button
- **Mobile Phone Layout**:
  - Tab-based: [Search] tab → [Cart] tab → [Pay] tab
  - Swipe between tabs
  - Cart badge shows item count
- **POS Dedicated Unit (Kiosk Mode)**:
  - Same as desktop/tablet landscape
  - Full-screen (no browser address bar)
  - Auto-focus on search input (ready for barcode scan)
  - Larger fonts and buttons (48px+ touch targets)
  - Optional on-screen numpad for quantity
- **Keyboard support**: Barcode scanner input, Enter to search, F-keys for actions
- **Touch-optimized**: Large buttons (min 48px), swipe to remove, pinch-to-zoom disabled

### POS Payment Dialog
- Total amount due (large, prominent)
- Payment method selector: Cash, GCash, Bank Transfer, Credit, Split Payment
- Amount tendered input (auto-focus for cash)
- Change computation (live, as you type)
- "Apply Tax" toggle (if tax enabled in settings)
- Transaction-level discount input (type + value)
- Add-on charges section
- Customer selector (optional, for OR/SI)
- Split payment: add multiple payment rows (method + amount + ref#)
- [Complete Sale] button → prints receipt

### POS Receipt Preview
- Business name, address, TIN (from settings)
- Receipt/Sale number
- Date & time
- Cashier name
- Items table: Item, Qty, Price, Amount
- Subtotal, Discount, Add-ons, Tax breakdown (if enabled), Total
- Payment method + amount tendered + change
- Footer message (from settings)
- [Print] and [Close] buttons

### POS Shift Management
- **Open Shift**: Opening cash amount input → starts shift
- **Close Shift**: Cash count input, auto-calculates expected vs. actual, over/short display, summary of transactions, [Close Shift] button
- **Shift Required**: POS won't allow sales until shift is opened

### POS Sales History
- Data table: Sale #, Date/Time, Customer, Items Count, Total, Payment Method, Status, Cashier
- Filter by: date range, **status (OPEN/PAID/CLOSED/VOIDED)**, cashier, payment method
- **Status badges**: OPEN (blue), PAID (green), CLOSED (gray), VOIDED (red)
- Click to view full detail
- **Resume button**: On OPEN sales (continue adding items / proceed to pay)
- **Print/Reprint button**: On PAID/CLOSED sales
- **Close button**: On PAID sales (finalize)
- **Void button**: On OPEN/PAID/CLOSED sales (permission required, requires reason)

## Implementation Phases

### Phase 1 - Project Foundation (Week 1)
- [ ] Maven project setup (pom.xml with all dependencies, WAR packaging)
- [ ] Application configuration (application.yml)
- [ ] Database schema SQL script (16 tables)
- [ ] Entity classes with JPA annotations (all 16 entities)
- [ ] Repository interfaces
- [ ] Global exception handler
- [ ] Standard API response wrapper
- [ ] Security configuration (JWT)
- [ ] Auth controller (login, refresh, logout)
- [ ] Audit trail entity and listener
- [ ] CORS configuration
- [ ] App settings service (read/write config values)

### Phase 2 - Master Data APIs + UI (Week 2)
- [ ] Category service + controller
- [ ] Item service + controller (with item_code auto-generation)
- [ ] Customer service + controller
- [ ] Supplier service + controller
- [ ] Add-on master service + controller
- [ ] User service + controller (Admin)
- [ ] Settings controller (Admin)
- [ ] Audit trail service + controller
- [ ] Frontend: Vue project setup (Vite, Vue Router, Pinia, Axios, Bootstrap 5)
- [ ] Frontend: Login page + auth store
- [ ] Frontend: AppLayout (Navbar, Sidebar, content area)
- [ ] Frontend: Axios interceptor (JWT, error handling)
- [ ] Frontend: Reusable DataTable component
- [ ] Frontend: CategoryList + CategoryForm views
- [ ] Frontend: ItemList + ItemForm views
- [ ] Frontend: CustomerList + CustomerForm views
- [ ] Frontend: SupplierList + SupplierForm views

### Phase 3 - Inventory Operations + UI (Week 3)
- [ ] Stock service + controller
- [ ] Stock transaction service + controller
- [ ] Stock In business logic (with optional tax, supplier, document numbering)
- [ ] Stock Out business logic (with optional tax, discount, addons, customer, document numbering)
- [ ] Stock Adjustment business logic
- [ ] Document number generation service
- [ ] Tax computation service (optional, based on settings)
- [ ] Frontend: StockOverview view
- [ ] Frontend: StockIn view (with tax toggle, supplier, document fields)
- [ ] Frontend: StockOut view (with tax toggle, customer, discount, addons)
- [ ] Frontend: StockAdjust view
- [ ] Frontend: TransactionList view

### Phase 4 - POS Module (Week 4)
- [ ] Sale service (create sale, cart processing, stock deduction)
- [ ] Sale void service (void logic, stock reversal)
- [ ] Payment processing service (single, multiple/split)
- [ ] Shift service (open, close, summary calculations)
- [ ] Receipt data service (format receipt for print)
- [ ] POS item search (optimized fast-lookup)
- [ ] Sale number generation (sequential)
- [ ] POS controllers (sales, shifts, item lookup)
- [ ] Frontend: POS Terminal view (full-screen, item search, cart)
- [ ] Frontend: POS Payment dialog (amount, method, change, discount, tax)
- [ ] Frontend: POS Receipt preview (print-ready layout)
- [ ] Frontend: POS Shift Open/Close dialogs
- [ ] Frontend: SalesList view (sales history)
- [ ] Frontend: SaleDetail view
- [ ] Frontend: POS route guard (require open shift)

### Phase 5 - Dashboard, Reports, Settings & Audit UI (Week 5)
- [ ] Dashboard API (aggregated stats, gross profit, top items, POS today summary)
- [ ] JasperReport templates (.jrxml) — 19 reports total
- [ ] Report generation service
- [ ] Report controller (PDF endpoints)
- [ ] Frontend: Dashboard view (summary cards, profit stats, POS stats)
- [ ] Frontend: Reports view with PDF viewer (inventory + POS reports)
- [ ] Frontend: Settings view (company, tax, documents, POS, discounts)
- [ ] Frontend: AuditTrail view
- [ ] Frontend: UserList + UserForm views (Admin)
- [ ] Frontend: Add-on master management

### Phase 6 - Polish & Testing (Week 6)
- [ ] Mobile responsiveness pass (all views on phone, tablet, desktop)
- [ ] POS terminal mobile/tablet layout (stacked: search → cart → pay)
- [ ] POS kiosk mode (full-screen, no browser chrome, dedicated terminal)
- [ ] Mobile navigation (bottom nav bar on phone, hamburger on tablet)
- [ ] Data tables → card view on mobile breakpoints
- [ ] Touch optimization (48px targets, swipe gestures, no hover dependencies)
- [ ] Form validation UX (inline errors, disable submit on invalid)
- [ ] Loading states, skeleton screens, error boundaries
- [ ] Tax enable/disable UX (show/hide tax fields dynamically)
- [ ] Receipt printing integration (browser print / thermal printer API)
- [ ] Barcode scanner input support (keyboard event listener)
- [ ] Performance: query optimization, pagination, lazy loading routes
- [ ] Unit tests for backend services (JUnit 5 + Spring Boot Test)
- [ ] Integration tests for controllers (MockMvc)
- [ ] E2E tests with Playwright (login, CRUD flows, POS sale flow, reports)
- [ ] Playwright mobile/tablet viewport tests
- [ ] API test cases (Postman/Newman regression)
- [ ] Frontend: Vite build → static resources in WAR
- [ ] API documentation (Swagger UI)
- [ ] Final bug fixes and deployment test on Tomcat 9

## Maven Dependencies

```xml
<!-- Spring Boot Starters -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-security
spring-boot-starter-tomcat          (scope: provided)

<!-- Database -->
mysql-connector-j

<!-- JWT -->
jjwt-api
jjwt-impl
jjwt-jackson

<!-- Reporting -->
jasperreports

<!-- Documentation -->
springdoc-openapi-starter-webmvc-ui

<!-- Utilities -->
lombok

<!-- Dev & Test -->
spring-boot-devtools
spring-boot-starter-test
spring-security-test
```

**Packaging**: `<packaging>war</packaging>`

## Configuration (application.yml)

```yaml
server:
  port: 8080
  servlet:
    context-path: /inventory

spring:
  application:
    name: inventory-system
  datasource:
    url: jdbc:mysql://localhost:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry: 1800000    # 30 minutes
    refresh-token-expiry: 604800000  # 7 days
  cors:
    allowed-origins: http://localhost:8080
  reports:
    template-path: classpath:reports/
```

## Deployment (Tomcat 9)

### WAR Packaging
- `pom.xml` packaging set to `war`
- `spring-boot-starter-tomcat` marked as `provided` scope
- Main application class extends `SpringBootServletInitializer`
- WAR file deployed to `TOMCAT_HOME/webapps/`

### Tomcat 9 Requirements
- Apache Tomcat 9.0.x
- Java 25 configured in `CATALINA_OPTS` or `setenv.bat`/`setenv.sh`
- Context path: `/inventory` (from WAR filename or server.xml)
- Environment variables for DB credentials and JWT secret set in Tomcat's `setenv.bat`:
  ```bat
  set "CATALINA_OPTS=-DDB_USERNAME=root -DDB_PASSWORD=secret -DJWT_SECRET=your-secret-key"
  ```

### Development Mode
- Can still run via `mvn spring-boot:run` for local development
- Embedded Tomcat available during development (not marked `provided` in dev profile)

## Decisions & Notes

- **API-first approach**: Backend is purely REST API. Vue 3 frontend consumes via Axios + JWT.
- **Vue 3 + Vite**: Frontend is a Vue 3 SPA using Composition API, built with Vite. Production build output goes to `src/main/resources/static/`.
- **WAR deployment to Tomcat 9**: Application packaged as WAR and deployed to external Tomcat 9 server. Embedded Tomcat still usable in dev via `mvn spring-boot:run`.
- **Frontend served from same WAR**: Compiled Vue app bundled inside the WAR — no separate frontend server in production.
- **JWT in memory**: Pinia auth store holds JWT in memory, not localStorage (XSS mitigation). Refresh token in httpOnly cookie.
- **Philippine localization**: Currency ₱ (PHP), Asia/Manila timezone, BIR-compliant document numbering, VAT computation, Senior/PWD discount.
- **Tax is optional**: Controlled by `app_settings.tax_enabled`. When OFF, all tax fields are hidden and not computed. When ON, user can still choose per-transaction whether to apply tax. This allows the system to work for both VAT-registered and non-VAT businesses.
- **POS integrated with Inventory**: POS sales auto-deduct from stock. Sales stored separately from manual stock transactions for clarity, but both affect the same `stock` table.
- **POS is shift-based**: Cashiers open/close shifts. Sales are linked to shifts for accountability and Z-reading reports.
- **Void, not edit**: Completed POS sales cannot be edited. They can only be voided (full reversal) with a reason. This maintains audit integrity.
- **Receipt printing**: Uses browser print dialog or can integrate with thermal printer via browser API. Receipt data served as formatted JSON from API.
- **Audit trail is automatic**: JPA entity listeners capture all changes without manual code in each service.
- **JasperReports for PDF**: 19 reports rendered server-side as PDF, served via API.
- **Soft delete everywhere**: `active` flag instead of hard delete. Audit trail captures deactivation.
- **Stock transactions are immutable**: Never edited or deleted (append-only for audit).
- **No ORM-generated schema**: Schema managed via SQL scripts for production control.
- **Context path**: Application accessible at `/inventory` (e.g., `http://localhost:8080/inventory/api/auth/login`).
- **Document numbering**: Sequential, non-repeating numbers per document type (BIR requirement). Managed via app_settings.
- **6-week timeline**: Extended from 5 to 6 weeks to accommodate POS module.
- **Multi-device support**: Single Vue SPA serves desktop, tablet, mobile phone, and dedicated POS units. Responsive via Bootstrap 5 breakpoints. POS terminal supports kiosk/full-screen mode for dedicated hardware.

## Next Steps

Once this plan is approved:
1. Generate the Maven project structure with pom.xml
2. Create the database schema SQL script
3. Start implementing Phase 1 (Foundation — entities, security, auth API)
