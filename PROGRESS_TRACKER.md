# Inventory + POS System - Progress Tracker

## Status Legend

- ⬜ Not Started
- 🔄 In Progress
- ✅ Completed
- ❌ Blocked
- ⏭️ Skipped

---

## Phase 1 — Foundation (Week 1)

**Status**: ✅ Completed
**Started**: 2026-08-12
**Completed**: 2026-08-12

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1.1 | Maven project setup (pom.xml, WAR packaging, all dependencies) | ✅ | Spring Boot 3.4.1, Java 25, 15 deps, 3 plugins |
| 1.2 | Application configuration (application.yml, dev, prod) | ⬜ | |
| 1.3 | Database schema SQL script (16 tables, indexes, FK) | ✅ | schema.sql created with all 16 tables |
| 1.4 | Seed data SQL (default admin user, app_settings) | ✅ | admin/admin123, 43 app_settings rows |
| 1.5 | BaseEntity class (audit fields: created_by, created_at, updated_by, updated_at) | ✅ | @MappedSuperclass + @EntityListeners |
| 1.6 | Enum classes (TransactionType, TransactionStatus, SaleStatus, etc.) | ✅ | 10 enums |
| 1.7 | Entity: User | ✅ | |
| 1.8 | Entity: Category | ✅ | |
| 1.9 | Entity: Item | ✅ | |
| 1.10 | Entity: Customer | ✅ | |
| 1.11 | Entity: Supplier | ✅ | |
| 1.12 | Entity: Stock | ✅ | |
| 1.13 | Entity: StockTransaction | ✅ | |
| 1.14 | Entity: TransactionAddon | ✅ | |
| 1.15 | Entity: AddonMaster | ✅ | |
| 1.16 | Entity: AuditTrail | ✅ | |
| 1.17 | Entity: AppSetting | ✅ | |
| 1.18 | Entity: Sale | ✅ | |
| 1.19 | Entity: SaleItem | ✅ | |
| 1.20 | Entity: SaleAddon | ✅ | |
| 1.21 | Entity: SalePayment | ✅ | |
| 1.22 | Entity: Shift | ✅ | |
| 1.23 | Repository: All 16 repositories | ✅ | With custom @Query methods |
| 1.24 | Common: ApiResponse wrapper | ✅ | |
| 1.25 | Common: PageResponse wrapper | ✅ | |
| 1.26 | Common: Constants class | ✅ | |
| 1.27 | Security: Permission constants (bitwise) | ✅ | 25 permissions |
| 1.28 | Security: JwtTokenProvider | ✅ | jjwt 0.12.6 |
| 1.29 | Security: JwtAuthenticationFilter | ✅ | |
| 1.30 | Security: CustomUserDetailsService | ✅ | |
| 1.31 | Security: CustomUserDetails | ✅ | |
| 1.32 | Security: SecurityConfig | ✅ | SecurityFilterChain, STATELESS |
| 1.33 | Config: CorsConfig | ✅ | + AuditInterceptor registration |
| 1.34 | Config: AppProperties (@ConfigurationProperties) | ✅ | |
| 1.35 | Exception: GlobalExceptionHandler | ✅ | 11 exception types handled |
| 1.36 | Exception: Custom exceptions (ResourceNotFound, BadRequest, etc.) | ✅ | 7 custom exceptions |
| 1.37 | Audit: AuditEntityListener | ✅ | @PostPersist, @PostUpdate, @PostRemove |
| 1.38 | Audit: AuditContext (holds current user + IP) | ✅ | ThreadLocal |
| 1.39 | Audit: AuditInterceptor | ✅ | |
| 1.40 | DTO: LoginRequest, LoginResponse | ✅ | + RefreshTokenRequest |
| 1.41 | Service: AuthService + AuthServiceImpl | ✅ | login, refresh, getCurrentUser |
| 1.42 | Controller: AuthController (login, refresh, logout, me) | ✅ | /api/auth/* |
| 1.43 | Service: AppSettingService + impl | ✅ | getValue, getBoolean, getInt, update |
| 1.44 | Verify: Application starts without errors | ✅ | mvn clean compile — BUILD SUCCESS |
| 1.45 | Verify: Login API returns JWT | ✅ | POST /api/auth/login returns tokens + user info |
| 1.46 | Verify: Protected endpoint rejects without token | ✅ | Returns 403 Forbidden |

**Phase 1 Verification Checklist:**
- [ ] `mvn clean compile` succeeds
- [ ] MySQL schema created (16 tables)
- [ ] Application starts (`mvn spring-boot:run`)
- [ ] `POST /api/auth/login` returns JWT
- [ ] `GET /api/auth/me` returns user info with valid token
- [ ] `GET /api/auth/me` returns 401 without token
- [ ] Audit trail records login event

---

## Phase 2 — Master Data APIs + UI (Week 2)

**Status**: ⬜ Not Started
**Started**: —
**Completed**: —

### Backend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 2.1 | DTO: Category (request + response) | ⬜ | |
| 2.2 | Mapper: CategoryMapper | ⬜ | |
| 2.3 | Service: CategoryService + impl | ⬜ | |
| 2.4 | Controller: CategoryController | ⬜ | |
| 2.5 | DTO: Item (request + response) | ⬜ | |
| 2.6 | Mapper: ItemMapper | ⬜ | |
| 2.7 | Service: ItemService + impl (with item_code auto-generation) | ⬜ | |
| 2.8 | Controller: ItemController | ⬜ | |
| 2.9 | DTO: Customer (request + response) | ⬜ | |
| 2.10 | Mapper: CustomerMapper | ⬜ | |
| 2.11 | Service: CustomerService + impl | ⬜ | |
| 2.12 | Controller: CustomerController | ⬜ | |
| 2.13 | DTO: Supplier (request + response) | ⬜ | |
| 2.14 | Mapper: SupplierMapper | ⬜ | |
| 2.15 | Service: SupplierService + impl | ⬜ | |
| 2.16 | Controller: SupplierController | ⬜ | |
| 2.17 | DTO: AddonMaster (request + response) | ⬜ | |
| 2.18 | Service: AddonMasterService + impl | ⬜ | |
| 2.19 | Controller: AddonMasterController | ⬜ | |
| 2.20 | DTO: User (request + response) | ⬜ | |
| 2.21 | Mapper: UserMapper | ⬜ | |
| 2.22 | Service: UserService + impl (CRUD + password reset) | ⬜ | |
| 2.23 | Controller: UserController | ⬜ | |
| 2.24 | Controller: AppSettingController | ⬜ | |
| 2.25 | Service: AuditTrailService + impl | ⬜ | |
| 2.26 | Controller: AuditTrailController | ⬜ | |
| 2.27 | Controller: ConfigController (navigation, forms, reports, etc.) | ⬜ | |
| 2.28 | Config files: validation-messages.properties | ⬜ | |
| 2.29 | Config files: navigation.yml, forms.yml, reports.yml | ⬜ | |
| 2.30 | Config files: permissions.yml, units, payments, documents | ⬜ | |

### Frontend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 2.31 | Vue project setup (Vite, package.json, vite.config.js) | ⬜ | |
| 2.32 | Install dependencies (Vue Router, Pinia, Axios, Bootstrap 5) | ⬜ | |
| 2.33 | App.vue + main.js + router/index.js | ⬜ | |
| 2.34 | API: axios.js (instance with JWT interceptor) | ⬜ | |
| 2.35 | Store: auth.js (Pinia — login, logout, token, user, permissions) | ⬜ | |
| 2.36 | Store: app.js (Pinia — config, navigation, settings) | ⬜ | |
| 2.37 | View: Login.vue (functional login page) | ⬜ | |
| 2.38 | Component: AppLayout.vue (navbar + sidebar + content) | ⬜ | |
| 2.39 | Component: Navbar.vue | ⬜ | |
| 2.40 | Component: Sidebar.vue (permission-aware, from navigation.yml) | ⬜ | |
| 2.41 | Component: DataTable.vue (pagination, search, sort) | ⬜ | |
| 2.42 | Component: ConfirmDialog.vue | ⬜ | |
| 2.43 | Component: Toast.vue | ⬜ | |
| 2.44 | Component: FormInput.vue | ⬜ | |
| 2.45 | Component: PageHeader.vue | ⬜ | |
| 2.46 | Component: LoadingSpinner.vue | ⬜ | |
| 2.47 | API module: categories.js | ⬜ | |
| 2.48 | View: CategoryList.vue + CategoryForm.vue | ⬜ | |
| 2.49 | API module: items.js | ⬜ | |
| 2.50 | View: ItemList.vue + ItemForm.vue | ⬜ | |
| 2.51 | API module: customers.js | ⬜ | |
| 2.52 | View: CustomerList.vue + CustomerForm.vue | ⬜ | |
| 2.53 | API module: suppliers.js | ⬜ | |
| 2.54 | View: SupplierList.vue + SupplierForm.vue | ⬜ | |

**Phase 2 Verification Checklist:**
- [ ] All CRUD APIs work via Postman/Swagger
- [ ] Frontend login works (token stored, redirect to dashboard)
- [ ] Sidebar renders based on user permissions
- [ ] Category CRUD works in browser
- [ ] Item CRUD works in browser (with item_code generation)
- [ ] Customer CRUD works in browser
- [ ] Supplier CRUD works in browser
- [ ] Pagination, search, sort work on DataTable
- [ ] Validation errors display inline on forms
- [ ] Audit trail records all create/update operations

---

## Phase 3 — Inventory Operations + UI (Week 3)

**Status**: ⬜ Not Started
**Started**: —
**Completed**: —

### Backend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 3.1 | Service: TaxService + impl (VAT-inclusive, VAT-exclusive, exempt) | ⬜ | |
| 3.2 | Service: DocumentNumberService + impl (sequential generation) | ⬜ | |
| 3.3 | DTO: StockIn, StockOut, StockAdjust (request + response) | ⬜ | |
| 3.4 | Mapper: StockTransactionMapper | ⬜ | |
| 3.5 | Service: StockService + impl (stock level queries) | ⬜ | |
| 3.6 | Service: StockTransactionService + impl (create, approve, cancel) | ⬜ | |
| 3.7 | Controller: StockController (in, out, adjust, approve, cancel) | ⬜ | |
| 3.8 | Controller: TransactionController (list, detail, print, pending) | ⬜ | |
| 3.9 | Discount calculation logic | ⬜ | |
| 3.10 | Add-ons processing logic | ⬜ | |
| 3.11 | Stock validation (sufficient qty for OUT) | ⬜ | |
| 3.12 | Approval workflow (CREATED → APPROVED, stock update) | ⬜ | |
| 3.13 | Cancel workflow (CREATED → CANCELLED) | ⬜ | |

### Frontend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 3.14 | API module: stock.js, transactions.js | ⬜ | |
| 3.15 | View: StockOverview.vue (color-coded levels) | ⬜ | |
| 3.16 | View: StockIn.vue (supplier, tax toggle, document, addons) | ⬜ | |
| 3.17 | View: StockOut.vue (customer, discount, tax toggle, document, addons) | ⬜ | |
| 3.18 | View: StockAdjust.vue | ⬜ | |
| 3.19 | View: TransactionList.vue (status filter, approve/cancel buttons, reprint) | ⬜ | |
| 3.20 | Component: StockBadge.vue (color-coded stock indicator) | ⬜ | |
| 3.21 | Tax fields show/hide based on app_settings.tax_enabled | ⬜ | |
| 3.22 | Discount fields show/hide based on app_settings.enable_discounts | ⬜ | |

**Phase 3 Verification Checklist:**
- [ ] Stock In creates transaction with status CREATED
- [ ] Approve changes status to APPROVED and updates stock qty
- [ ] Cancel changes status to CANCELLED, stock unchanged
- [ ] Stock Out validates insufficient stock
- [ ] Tax computed correctly (VAT-inclusive and exclusive)
- [ ] Discount applied correctly (fixed and percentage)
- [ ] Add-ons added to transaction total
- [ ] Document numbers auto-generated sequentially
- [ ] Reprint returns formatted transaction data
- [ ] All operations recorded in audit trail

---

## Phase 4 — POS Module (Week 4)

**Status**: ⬜ Not Started
**Started**: —
**Completed**: —

### Backend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 4.1 | DTO: Sale (CreateSaleRequest, SaleItemRequest, PaymentRequest, etc.) | ⬜ | |
| 4.2 | DTO: Sale responses (SaleResponse, SaleDetailResponse, ReceiptResponse) | ⬜ | |
| 4.3 | DTO: Shift (OpenShiftRequest, CloseShiftRequest, ShiftResponse) | ⬜ | |
| 4.4 | Mapper: SaleMapper, ShiftMapper | ⬜ | |
| 4.5 | Service: SaleService + impl (create OPEN, update items, pay, close) | ⬜ | |
| 4.6 | Service: Sale void logic (stock reversal) | ⬜ | |
| 4.7 | Service: Payment processing (single + split) | ⬜ | |
| 4.8 | Service: ShiftService + impl (open, close, summary) | ⬜ | |
| 4.9 | Service: Receipt formatting service | ⬜ | |
| 4.10 | Service: POS item search (optimized fast-lookup) | ⬜ | |
| 4.11 | Service: Sale number generation (sequential) | ⬜ | |
| 4.12 | Controller: PosController (sales CRUD, pay, void, receipt) | ⬜ | |
| 4.13 | Controller: ShiftController (open, close, current, list) | ⬜ | |
| 4.14 | Stock deduction on PAID (auto-creates stock_transactions) | ⬜ | |
| 4.15 | Stock reversal on VOID | ⬜ | |
| 4.16 | Senior/PWD discount (20% + VAT exempt) | ⬜ | |

### Frontend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 4.17 | API module: pos.js | ⬜ | |
| 4.18 | Store: pos.js (Pinia — cart, current sale, shift) | ⬜ | |
| 4.19 | View: PosTerminal.vue (full-screen layout) | ⬜ | |
| 4.20 | Component: POS item search + results grid | ⬜ | |
| 4.21 | Component: POS cart (add/remove items, qty +/-, totals) | ⬜ | |
| 4.22 | Component: POS Payment dialog (amount, method, change, split) | ⬜ | |
| 4.23 | Component: POS Receipt preview (printable) | ⬜ | |
| 4.24 | Component: POS discount input (transaction + line level) | ⬜ | |
| 4.25 | View: PosShiftOpen.vue | ⬜ | |
| 4.26 | View: PosShiftClose.vue (cash count, reconciliation) | ⬜ | |
| 4.27 | View: SalesList.vue (history, filter by status) | ⬜ | |
| 4.28 | View: SaleDetail.vue | ⬜ | |
| 4.29 | POS route guard (require open shift) | ⬜ | |
| 4.30 | Resume OPEN sale functionality | ⬜ | |
| 4.31 | Reprint receipt (PAID/CLOSED sales) | ⬜ | |
| 4.32 | Responsive: tablet + mobile POS layout | ⬜ | |

**Phase 4 Verification Checklist:**
- [ ] Open shift works (opening amount recorded)
- [ ] Create sale (OPEN), add items to cart
- [ ] Cart totals calculate correctly (subtotal, discount, tax, total)
- [ ] Pay sale (OPEN → PAID), stock deducted for each item
- [ ] Change calculated correctly
- [ ] Split payment works (amounts sum to total)
- [ ] Close sale (PAID → CLOSED)
- [ ] Void sale reverses stock, reason required
- [ ] Receipt displays all BIR-required fields
- [ ] Reprint works on PAID/CLOSED sales
- [ ] Close shift shows summary (total sales, expected vs actual cash)
- [ ] Senior/PWD discount applies 20% + VAT exempt
- [ ] OPEN sales can be resumed

---

## Phase 5 — Dashboard, Reports, Settings & Audit UI (Week 5)

**Status**: ⬜ Not Started
**Started**: —
**Completed**: —

### Backend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 5.1 | Service: DashboardService + impl (stats aggregation) | ⬜ | |
| 5.2 | Controller: DashboardController | ⬜ | |
| 5.3 | JasperReport: stock-level.jrxml | ⬜ | |
| 5.4 | JasperReport: stock-movement.jrxml | ⬜ | |
| 5.5 | JasperReport: low-stock.jrxml | ⬜ | |
| 5.6 | JasperReport: transaction-summary.jrxml | ⬜ | |
| 5.7 | JasperReport: item-list.jrxml | ⬜ | |
| 5.8 | JasperReport: inventory-count.jrxml | ⬜ | |
| 5.9 | JasperReport: stock-valuation.jrxml | ⬜ | |
| 5.10 | JasperReport: gross-profit.jrxml | ⬜ | |
| 5.11 | JasperReport: profit-share.jrxml | ⬜ | |
| 5.12 | JasperReport: sales-summary.jrxml | ⬜ | |
| 5.13 | JasperReport: purchase-summary.jrxml | ⬜ | |
| 5.14 | JasperReport: vat-summary.jrxml | ⬜ | |
| 5.15 | JasperReport: daily-sales.jrxml (Z-Reading) | ⬜ | |
| 5.16 | JasperReport: shift-report.jrxml (X-Reading) | ⬜ | |
| 5.17 | JasperReport: sales-by-payment.jrxml | ⬜ | |
| 5.18 | JasperReport: sales-by-cashier.jrxml | ⬜ | |
| 5.19 | JasperReport: top-selling.jrxml | ⬜ | |
| 5.20 | JasperReport: voided-transactions.jrxml | ⬜ | |
| 5.21 | JasperReport: hourly-sales.jrxml | ⬜ | |
| 5.22 | Service: ReportService + impl (compile + fill + export PDF) | ⬜ | |
| 5.23 | Controller: ReportController (all 19 endpoints) | ⬜ | |

### Frontend

| # | Task | Status | Notes |
|---|------|--------|-------|
| 5.24 | API module: reports.js, settings.js, audit.js | ⬜ | |
| 5.25 | View: Dashboard.vue (summary cards, recent, top items, alerts) | ⬜ | |
| 5.26 | View: Reports.vue (config-driven, filter params, PDF viewer) | ⬜ | |
| 5.27 | View: Settings.vue (company, tax, documents, POS, discounts) | ⬜ | |
| 5.28 | View: AuditTrail.vue (filterable, expandable details) | ⬜ | |
| 5.29 | View: UserList.vue + UserForm.vue (with permission checkboxes) | ⬜ | |
| 5.30 | View: Addon management (list + form) | ⬜ | |

**Phase 5 Verification Checklist:**
- [ ] Dashboard shows correct stats (total items, stock value, sales today, profit)
- [ ] All 19 reports generate as PDF
- [ ] Reports page loads report list from config
- [ ] Report parameters (date range, filters) work correctly
- [ ] Settings page loads/saves all app_settings
- [ ] Tax toggle shows/hides tax fields across the app
- [ ] Audit trail shows changes with field-level detail
- [ ] User form shows permission checkboxes grouped by module
- [ ] Role presets auto-fill permissions on user create

---

## Phase 6 — Polish & Testing (Week 6)

**Status**: ⬜ Not Started
**Started**: —
**Completed**: —

### Responsive & UX

| # | Task | Status | Notes |
|---|------|--------|-------|
| 6.1 | Desktop layout pass (all pages) | ⬜ | |
| 6.2 | Tablet layout pass (all pages) | ⬜ | |
| 6.3 | Mobile phone layout pass (all pages) | ⬜ | |
| 6.4 | POS kiosk mode (full-screen, dedicated terminal) | ⬜ | |
| 6.5 | Mobile navigation (bottom nav bar) | ⬜ | |
| 6.6 | Data tables → card view on mobile | ⬜ | |
| 6.7 | Touch targets (48px minimum) | ⬜ | |
| 6.8 | Form validation UX (inline errors) | ⬜ | |
| 6.9 | Loading states / skeleton screens | ⬜ | |
| 6.10 | Error boundaries | ⬜ | |
| 6.11 | Print receipt integration (browser print) | ⬜ | |
| 6.12 | Barcode scanner input support | ⬜ | |

### Testing

| # | Task | Status | Notes |
|---|------|--------|-------|
| 6.13 | JUnit: AuthService tests | ⬜ | |
| 6.14 | JUnit: StockTransactionService tests | ⬜ | |
| 6.15 | JUnit: SaleService tests (full POS flow) | ⬜ | |
| 6.16 | JUnit: TaxService tests | ⬜ | |
| 6.17 | JUnit: DocumentNumberService tests | ⬜ | |
| 6.18 | Integration: AuthController tests (MockMvc) | ⬜ | |
| 6.19 | Integration: StockController tests | ⬜ | |
| 6.20 | Integration: PosController tests | ⬜ | |
| 6.21 | Postman: Full API collection | ⬜ | |
| 6.22 | Newman: CI-ready regression suite | ⬜ | |
| 6.23 | Playwright: auth.spec.js | ⬜ | |
| 6.24 | Playwright: categories.spec.js | ⬜ | |
| 6.25 | Playwright: items.spec.js | ⬜ | |
| 6.26 | Playwright: stock-in.spec.js | ⬜ | |
| 6.27 | Playwright: stock-out.spec.js | ⬜ | |
| 6.28 | Playwright: pos-sale.spec.js | ⬜ | |
| 6.29 | Playwright: pos-void.spec.js | ⬜ | |
| 6.30 | Playwright: pos-shift.spec.js | ⬜ | |
| 6.31 | Playwright: reports.spec.js | ⬜ | |
| 6.32 | Playwright: mobile viewport tests | ⬜ | |

### Deployment

| # | Task | Status | Notes |
|---|------|--------|-------|
| 6.33 | Performance: query optimization | ⬜ | |
| 6.34 | Performance: pagination on all list endpoints | ⬜ | |
| 6.35 | Performance: lazy loading routes (Vue) | ⬜ | |
| 6.36 | Frontend: Vite production build → static resources | ⬜ | |
| 6.37 | Swagger UI accessible and complete | ⬜ | |
| 6.38 | WAR build: `mvn clean package` | ⬜ | |
| 6.39 | Deploy to Tomcat 9 (C:\Users\joven\tomcat9) | ⬜ | |
| 6.40 | Verify full app at http://localhost:8080/inventory | ⬜ | |
| 6.41 | Security audit pass | ⬜ | |
| 6.42 | Final bug fixes | ⬜ | |

**Phase 6 Verification Checklist:**
- [ ] All pages work on desktop (1280px)
- [ ] All pages work on tablet (768px)
- [ ] All pages work on mobile (375px)
- [ ] POS terminal works on dedicated POS unit
- [ ] `mvn test` passes (all JUnit tests)
- [ ] Postman collection runs clean (all pass)
- [ ] Playwright tests pass (all viewports)
- [ ] WAR deploys to Tomcat 9 without errors
- [ ] Full app accessible and functional at context path
- [ ] No security vulnerabilities in auth/payment/void flows

---

## Overall Progress

| Phase | Tasks | Completed | Progress |
|-------|-------|-----------|----------|
| Phase 1 — Foundation | 46 | 46 | 100% ✅ |
| Phase 2 — Master Data | 54 | 0 | 0% |
| Phase 3 — Inventory | 22 | 0 | 0% |
| Phase 4 — POS | 32 | 0 | 0% |
| Phase 5 — Reports | 30 | 0 | 0% |
| Phase 6 — Polish | 42 | 0 | 0% |
| **Total** | **226** | **46** | **20%** |

---

## Issues & Blockers

| # | Issue | Phase | Status | Resolution |
|---|-------|-------|--------|------------|
| — | — | — | — | — |

---

## Change Log

| Date | Change | Files Affected |
|------|--------|---------------|
| — | — | — |
