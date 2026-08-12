# Inventory + POS System - Project Index

## Quick Start

When starting a development session, read this file first. It provides the full project context and links to all documentation. Then tell me which phase, task, or step to work on.

---

## Project Info

| Field | Value |
|-------|-------|
| Project | Inventory + POS System |
| Author | Joven Q. Divinagracia Jr. |
| Location | C:\Users\joven\inventory |
| Started | 2026-08-12 |

---

## Project Summary

**System**: Inventory Management + POS (Point of Sale)
**Target**: Philippine-standard, multi-device (desktop, tablet, mobile, POS unit)
**Architecture**: REST API backend + Vue 3 SPA frontend, deployed as single WAR

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 25, Spring Boot 3.x, Spring MVC, Hibernate/JPA, Spring Security + JWT |
| Frontend | Vue 3 (Composition API), Vite, Bootstrap 5, Axios, Pinia |
| Database | MySQL 8.0 (16 tables) |
| Reports | JasperReports (19 PDF reports) |
| Testing | JUnit 5, Postman/Newman, Playwright |
| Build | Maven 3.9.16 (WAR packaging) |
| Server | Apache Tomcat 9 (external) |
| Config | YAML/Properties-driven (forms, reports, menus, permissions) |

---

## Documentation Files

Read these files for full context:

| File | Content |
|------|---------|
| `PROJECT_INDEX.md` | This file — master index, quick start, summary |
| `PROGRESS_TRACKER.md` | **Live progress** — task status per phase, updated during development |
| `IMPLEMENTATION_PLAN.md` | Full project plan — tech stack, architecture, package structure, API endpoints, frontend structure, UI descriptions, 6-phase timeline |
| `DATABASE_SCHEMA.md` | 16 tables — columns, types, indexes, FK constraints, pricing calculations, status workflows, business rules, 25 permissions |
| `PH_STANDARDS.md` | Philippine localization — VAT, BIR compliance, document numbering, receipt requirements, payment methods, Senior/PWD discount |
| `BACKEND_STANDARDS.md` | Java coding standards — package structure, naming conventions, patterns (controller/service/mapper/DTO), Lombok, logging, testing, security |
| `CONFIGURATION_STANDARDS.md` | 12 configuration files — application.yml, validation messages, forms.yml, reports.yml, navigation.yml, permissions.yml, units, payment methods, document types |
| `AGENT_ASSIGNMENTS.md` | 14 specialist agents — who does what per phase, review workflow, critical paths |

---

## Core Concepts

### Status Workflows

**Inventory (Stock In/Out/Adjust):**
```
CREATED → APPROVED (stock updated)
        → CANCELLED (no stock impact)
```

**POS (Sales):**
```
OPEN → PAID (stock deducted) → CLOSED (finalized)
  │      │                        │
  └─ VOIDED ── VOIDED ───────────┘ (stock reversed)
```

### Tax
- Optional — controlled by `app_settings.tax_enabled`
- Per-transaction toggle: "Apply Tax" checkbox
- Works with or without tax (for VAT and non-VAT businesses)

### Printing
- Optional — system works fully without a printer
- Controlled by `pos_printing_enabled` setting
- Any APPROVED transaction or PAID/CLOSED sale can be reprinted anytime

### Permissions
- Bitwise (25 permissions, stored as BIGINT on users table)
- Checked via `(accessRights & PERMISSION) != 0`
- Role presets: ADMIN, CASHIER, STOCK_CLERK, VIEWER (customizable per user)

---

## Modules

| Module | Entities | Key Operations |
|--------|----------|---------------|
| Auth | users | Login, JWT, refresh, permissions |
| Categories | categories | CRUD + activate/deactivate |
| Items | items, stock | CRUD + item_code generation + stock tracking |
| Customers | customers | CRUD + TIN (PH) |
| Suppliers | suppliers | CRUD + TIN |
| Stock Operations | stock_transactions, transaction_addons | Stock In/Out/Adjust → Approve/Cancel |
| POS | sales, sale_items, sale_addons, sale_payments, shifts | Cart → Pay → Close/Void, Shift open/close |
| Add-ons | addon_master | Predefined charge types |
| Reports | — | 19 PDF reports (inventory, financial, POS) |
| Audit Trail | audit_trail | Automatic field-level change tracking |
| Settings | app_settings | Company, tax, documents, POS, discounts |

---

## Implementation Phases

### Phase 1 — Foundation (Week 1)
- Maven project (pom.xml, WAR packaging)
- application.yml + environment configs
- Database schema SQL (16 tables) + seed data
- All 16 entity classes (JPA annotations, BaseEntity)
- All 16 repository interfaces
- JWT security (filter, provider, util)
- Auth controller (login, refresh, logout, me)
- Global exception handler + API response wrapper
- Audit trail listener
- CORS configuration
- App settings service

### Phase 2 — Master Data APIs + UI (Week 2)
- Category, Item, Customer, Supplier, Add-on, User, Settings, Audit services + controllers
- Vue project setup (Vite, Router, Pinia, Axios, Bootstrap 5)
- Login page + auth store
- App layout (navbar, sidebar)
- DataTable reusable component
- All master data views (Category, Item, Customer, Supplier)

### Phase 3 — Inventory Operations + UI (Week 3)
- Stock In/Out/Adjust services (with status, tax, discount, addons, documents)
- Approval/Cancel workflow
- Tax computation service
- Document number generator
- Stock views (overview, in, out, adjust)
- Transaction history (with status filter, approve/cancel actions, reprint)

### Phase 4 — POS Module (Week 4)
- Sale service (OPEN → PAID → CLOSED, stock deduction)
- Void service (stock reversal)
- Payment service (single + split)
- Shift service (open/close)
- Receipt service
- POS Terminal UI (full-screen, cart, payment, receipt)
- Shift open/close UI
- Sales history (with resume, reprint, void)

### Phase 5 — Dashboard, Reports, Settings & Audit UI (Week 5)
- Dashboard API + page
- 19 JasperReport templates
- Report generation service + controller
- Reports page (config-driven, PDF viewer)
- Settings page (company, tax, documents, POS, discounts)
- Audit trail page
- User management page

### Phase 6 — Polish & Testing (Week 6)
- Mobile/tablet/POS responsive pass
- Touch optimization, kiosk mode
- Playwright E2E tests (5 viewports)
- JUnit unit/integration tests
- Postman/Newman API tests
- Swagger UI
- Performance tuning
- WAR deployment to Tomcat 9

---

## How to Use This File

1. **Start a session**: Tell me to read the project docs (or just reference this file)
2. **Tell me what to work on**: Examples:
   - "Start Phase 1"
   - "Create the pom.xml"
   - "Build the Item entity"
   - "Work on Phase 3, Step: Tax computation service"
   - "Phase 4: POS Terminal UI"
   - "Fix the stock out approval logic"
3. **I'll use the documentation** for context — coding standards, schema, API design, configuration patterns — without you needing to repeat them.

---

## Project Location

```
C:\Users\joven\inventory\
├── PROJECT_INDEX.md              ← Start here (master index)
├── PROGRESS_TRACKER.md           ← Live progress (updated during dev)
├── IMPLEMENTATION_PLAN.md
├── DATABASE_SCHEMA.md
├── PH_STANDARDS.md
├── BACKEND_STANDARDS.md
├── CONFIGURATION_STANDARDS.md
└── AGENT_ASSIGNMENTS.md
```
