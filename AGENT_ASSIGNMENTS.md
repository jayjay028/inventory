# Inventory + POS System - Agent Assignment Plan

## Overview

This document assigns specialist agents to each area of the project. Each agent handles a specific domain, ensuring expert-level output across backend, frontend, database, testing, security, and documentation.

---

## Agent Roster

### 1. Backend Development

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **senior-software-engineer** | Lead Backend Developer | Java 25, Spring Boot, REST APIs, business logic, POS sale flow, stock operations, JWT auth, audit trail, tax computation, document numbering |
| **enterprise-api-architect** | API Design & Contracts | REST endpoint design, OpenAPI/Swagger specs, request/response DTOs, pagination patterns, error models, API versioning strategy |

### 2. Database

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **sql-database-architect** | Database Architect | MySQL 8.0 schema design, indexes, query optimization, stored procedures (if needed), migration scripts, performance tuning, data integrity |

### 3. Frontend Development

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **enterprise-frontend-engineer** | Lead Frontend Developer | Vue 3, Vite, Bootstrap 5, POS terminal UI, responsive forms, data tables, receipt layout, mobile optimization |
| **enterprise-uiux-specialist** | UI/UX Design | Layout design, mobile-first wireframes, POS terminal UX, form usability, accessibility, color system, component patterns |

### 4. Testing

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **test-automation-engineer** | Test Engineer (Backend) | Unit tests (JUnit 5), integration tests (Spring Boot Test), service layer testing, POS sale flow tests, stock deduction tests |
| **joven-api-test-agent** | API Test Engineer | REST API test cases, Postman collections, Newman CI regression, positive/negative scenarios, validation testing, auth testing |

### 5. Code Review

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **senior-code-reviewer** | Code Reviewer (General) | Code correctness, maintainability, performance, patterns, error handling, naming conventions |
| **joven-code-reviewer-agent** | Code Reviewer (Java/Enterprise) | Java EE patterns, OOP principles, Spring best practices, transaction safety, service layer design |

### 6. Security

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **application-security-engineer** | Security Engineer | JWT implementation review, authentication/authorization, input validation, SQL injection prevention, CORS, session handling, POS fraud prevention |

### 7. Architecture & Coordination

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **enterprise-solution-architect** | Solution Architect | Overall architecture decisions, layer boundaries, POS ↔ Inventory integration, scalability, ADRs |
| **enterprise-orchestrator** | Project Coordinator | Phase planning, task breakdown, dependency tracking, cross-agent coordination |

### 8. Infrastructure & Deployment

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **devops-cloud-engineer** | DevOps Engineer | Maven build configuration, WAR deployment to Tomcat 9, environment setup, Vite build pipeline, CI/CD (if needed) |

### 9. Documentation

| Agent | Role | Responsibilities |
|-------|------|-----------------|
| **technical-documentation-engineer** | Technical Writer | API documentation, user guides, deployment guide, database dictionary, POS operation manual |

---

## Agent Assignment by Phase

### Phase 1 — Project Foundation (Week 1)

| Task | Primary Agent | Reviewer |
|------|--------------|----------|
| pom.xml + Maven setup | senior-software-engineer | devops-cloud-engineer |
| application.yml | senior-software-engineer | application-security-engineer |
| Database schema SQL | sql-database-architect | senior-code-reviewer |
| Entity classes (JPA) | senior-software-engineer | joven-code-reviewer-agent |
| Repository interfaces | senior-software-engineer | senior-code-reviewer |
| JWT Security config | senior-software-engineer | application-security-engineer |
| Auth controller | senior-software-engineer | application-security-engineer |
| Global exception handler | senior-software-engineer | enterprise-api-architect |
| API response wrapper | enterprise-api-architect | senior-code-reviewer |
| Audit trail listener | senior-software-engineer | joven-code-reviewer-agent |
| CORS config | senior-software-engineer | application-security-engineer |

### Phase 2 — Master Data APIs + UI (Week 2)

| Task | Primary Agent | Reviewer |
|------|--------------|----------|
| Category/Item/Customer/Supplier services | senior-software-engineer | joven-code-reviewer-agent |
| Category/Item/Customer/Supplier controllers | senior-software-engineer | enterprise-api-architect |
| User management (Admin) | senior-software-engineer | application-security-engineer |
| Settings service | senior-software-engineer | senior-code-reviewer |
| Vue project setup | enterprise-frontend-engineer | devops-cloud-engineer |
| Login page + auth store | enterprise-frontend-engineer | application-security-engineer |
| App layout (Navbar, Sidebar) | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Data table component | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Category/Item/Customer/Supplier views | enterprise-frontend-engineer | enterprise-uiux-specialist |
| API test cases (master data) | joven-api-test-agent | senior-code-reviewer |

### Phase 3 — Inventory Operations + UI (Week 3)

| Task | Primary Agent | Reviewer |
|------|--------------|----------|
| Stock service + controller | senior-software-engineer | enterprise-api-architect |
| Stock In/Out/Adjust logic | senior-software-engineer | joven-code-reviewer-agent |
| Tax computation service | senior-software-engineer | senior-code-reviewer |
| Document number generator | senior-software-engineer | sql-database-architect |
| Stock views (Vue) | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Transaction history view | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Unit tests (stock services) | test-automation-engineer | senior-code-reviewer |
| API test cases (stock) | joven-api-test-agent | joven-code-reviewer-agent |

### Phase 4 — POS Module (Week 4)

| Task | Primary Agent | Reviewer |
|------|--------------|----------|
| Sale service (cart, stock deduction) | senior-software-engineer | joven-code-reviewer-agent |
| Payment processing service | senior-software-engineer | application-security-engineer |
| Shift service | senior-software-engineer | senior-code-reviewer |
| Void/return logic | senior-software-engineer | joven-code-reviewer-agent |
| POS controllers | senior-software-engineer | enterprise-api-architect |
| POS Terminal UI | enterprise-frontend-engineer | enterprise-uiux-specialist |
| POS Payment dialog | enterprise-frontend-engineer | enterprise-uiux-specialist |
| POS Receipt layout | enterprise-frontend-engineer | enterprise-uiux-specialist |
| POS Shift UI | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Unit tests (POS sale flow) | test-automation-engineer | senior-code-reviewer |
| API test cases (POS) | joven-api-test-agent | joven-code-reviewer-agent |
| POS security review | application-security-engineer | senior-code-reviewer |

### Phase 5 — Dashboard, Reports, Settings & Audit UI (Week 5)

| Task | Primary Agent | Reviewer |
|------|--------------|----------|
| Dashboard API | senior-software-engineer | enterprise-api-architect |
| JasperReport templates (19) | senior-software-engineer | sql-database-architect |
| Report controller | senior-software-engineer | enterprise-api-architect |
| Dashboard view | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Reports view + PDF viewer | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Settings view | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Audit trail view | enterprise-frontend-engineer | enterprise-uiux-specialist |
| User management view | enterprise-frontend-engineer | application-security-engineer |
| API test cases (reports) | joven-api-test-agent | senior-code-reviewer |

### Phase 6 — Polish & Testing (Week 6)

| Task | Primary Agent | Reviewer |
|------|--------------|----------|
| Mobile responsiveness | enterprise-frontend-engineer | enterprise-uiux-specialist |
| POS mobile layout | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Form validation UX | enterprise-frontend-engineer | enterprise-uiux-specialist |
| Receipt printing integration | enterprise-frontend-engineer | devops-cloud-engineer |
| Performance optimization | sql-database-architect | senior-code-reviewer |
| Full regression test suite | test-automation-engineer | joven-api-test-agent |
| Security audit | application-security-engineer | senior-code-reviewer |
| API documentation (Swagger) | enterprise-api-architect | technical-documentation-engineer |
| User/deployment guide | technical-documentation-engineer | enterprise-orchestrator |
| Vite build → WAR packaging | devops-cloud-engineer | senior-software-engineer |
| Tomcat 9 deployment test | devops-cloud-engineer | senior-software-engineer |
| Final code review | senior-code-reviewer | joven-code-reviewer-agent |

---

## Agent Usage Summary

| Agent | Primary Tasks | Reviews |
|-------|--------------|---------|
| senior-software-engineer | Backend (all services, controllers, JPA, JWT, reports) | Build/deploy review |
| enterprise-api-architect | API design, response format, Swagger docs | Controller reviews |
| sql-database-architect | Schema, indexes, query optimization | Document numbering, reports |
| enterprise-frontend-engineer | Vue 3 (all views, components, POS terminal) | — |
| enterprise-uiux-specialist | UI/UX guidance, wireframes | All frontend views |
| test-automation-engineer | Unit tests, integration tests | Regression |
| joven-api-test-agent | API test cases, Postman, Newman | API regression |
| senior-code-reviewer | General code quality | Cross-cutting |
| joven-code-reviewer-agent | Java/Spring patterns, OOP | Service layer |
| application-security-engineer | Security review, JWT, auth | Security-sensitive code |
| devops-cloud-engineer | Maven, Tomcat, Vite build, CI | Build config |
| enterprise-orchestrator | Coordination, planning | — |
| technical-documentation-engineer | User guide, API docs, deployment | — |
| enterprise-solution-architect | Architecture decisions | — |

**Total: 14 agents**

---

## Review Workflow

```
Developer writes code
       ↓
Primary Reviewer checks (domain expert)
       ↓
Security Review (if auth/payment/data related)
       ↓
API Test Engineer validates endpoints
       ↓
Test Engineer runs unit/integration tests
       ↓
Final approval
```

### Critical Paths (require 2+ reviewers)

| Area | Reviewers Required |
|------|-------------------|
| JWT Authentication | application-security-engineer + senior-code-reviewer |
| POS Sale Flow | joven-code-reviewer-agent + test-automation-engineer |
| Payment Processing | application-security-engineer + joven-code-reviewer-agent |
| Stock Deduction Logic | joven-code-reviewer-agent + test-automation-engineer |
| Database Schema | sql-database-architect + senior-code-reviewer |
| Tax Computation | senior-code-reviewer + joven-api-test-agent |
| Document Numbering | sql-database-architect + senior-code-reviewer |

---

## Notes

- **senior-software-engineer** is the workhorse — handles most backend implementation
- **enterprise-frontend-engineer** handles all Vue development
- **Reviewers don't block progress** — reviews happen after initial implementation, issues are fixed in next pass
- **API test cases** are created alongside implementation (not after)
- **Security reviews** are mandatory for: auth, payments, void operations, user management
- Agents can be invoked in parallel (e.g., backend + frontend in same phase)
