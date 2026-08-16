# HRM Salary Structure & Payslip Automation Module

**Assignment for:** Future Transform — Java Full Stack Developer Intern
**Module:** HRM – Payroll & Leave Management

A full-stack HR payroll system supporting leave policy configuration, salary structure management, automated monthly payslip generation (PDF), and automatic email delivery to employees — with role-based access for HR and Employees.

---

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.3.x
- Spring Data JPA / Hibernate
- Spring Security + JWT authentication
- MySQL
- iText7 — PDF payslip generation
- Spring Mail (Gmail SMTP) — email delivery
- Maven

**Frontend**
- React 18 (Vite)
- React Router v6 — routing + role-based route protection
- Axios — API client with JWT interceptor
- Plain CSS (custom design tokens via CSS variables) — no UI framework
- Fonts: Playfair Display (headings), Plus Jakarta Sans (body/UI)

**Database**
- MySQL 8

---

## Project Structure
HRM-payroll/
├── hrm-payroll-backend/ # Spring Boot project
├── hrm-payroll-frontend/ # React (Vite) project
├── payslips/ # Generated PDF storage (created at runtime)
├── database/
│ ├── schema.sql # Table creation script
│ └── seed-data.sql # Sample/seed data
└── postman/
└── HRM-Payroll-Backend.postman_collection.json


---

## Database Setup

1. Create the database:
```sql
CREATE DATABASE hrm_payroll_db;
```

2. Run the schema script:
```bash
mysql -u root -p hrm_payroll_db < database/schema.sql
```

3. Run the seed data script (leave type allocations, sample employee, sample salary structure):
```bash
mysql -u root -p hrm_payroll_db < database/seed-data.sql
```

This creates the leave type allocations (CL=12, SL=12, EL=15), one sample employee (`EMP001` — Ravi Kumar), and an assigned salary structure.

---

## Backend Setup

```bash
cd hrm-payroll-backend
```

Update `src/main/resources/application.properties` with your local values:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrm_payroll_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password

app.jwt.secret=replace_with_a_long_random_base64_secret_key_min_256bits
app.jwt.expiration-ms=86400000

spring.mail.username=your.sender.email@gmail.com
spring.mail.password=your_16_char_gmail_app_password
```

> **Gmail App Password required** — generate one under Google Account → Security → 2-Step Verification → App Passwords. A regular Gmail password will not work with SMTP.

Run the application:
```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

On first startup, a default **HR login** and a linked **Employee login** are auto-seeded (see Test Credentials below).

---

## Frontend Setup

```bash
cd hrm-payroll-frontend
npm install
```

Create a `.env` file in the project root (see `.env.example`):
VITE_API_BASE_URL=http://localhost:8080/api


Run the dev server:
```bash
npm run dev
```

App runs on `http://localhost:5173`. Ensure the backend is running on `http://localhost:8080` and its CORS configuration allows `http://localhost:5173`.

---

## Test Credentials

| Role | Username | Password |
|---|---|---|
| HR | `hradmin` | `Admin@123` |
| Employee | `ravi.kumar` | `Employee@123` |

---

## Frontend Structure
src/
api/ — one file per backend resource, thin axios wrappers
auth/ — AuthContext (login/logout/session), ProtectedRoute (role guard)
layouts/ — AppLayout (sidebar + topbar shell, role-aware nav)
components/ — shared UI: Modal, StatusBadge, EmployeeSelect, ErrorBoundary
pages/ — one file per screen


---

## Role-Based Access

- **HR**: full access — Employees, Salary Structures, Salary Assignments, Leave Applications (approve/reject), Payslips (generate/view all), Email Logs, Dashboard.
- **EMPLOYEE**: scoped access — own Dashboard (leave balances), My Leave (apply + own history), My Payslips (own payslips only, via employee-scoped backend endpoints with server-side ownership checks).

Route protection is enforced on the frontend via `ProtectedRoute` (redirects unauthorized roles), but **the backend is the actual authority** — every employee-scoped endpoint independently verifies the requester owns the data (via the JWT-linked `Employee` record), regardless of what the frontend allows.

---

## API Documentation

Full Postman collection: `postman/HRM-Payroll-Backend.postman_collection.json`

Import into Postman, then run **Auth → Login (HR)** first — the token is captured automatically into a collection variable and reused by every other request.

Covers all endpoints across: Auth, Employees, Salary Structures, Salary Assignments, Leave Balances, Leave Applications, Payslips, Email Logs, Dashboard.

---

## Implemented Payroll Flow (Summary)

1. **HR configures a Salary Structure** (Basic, HRA, Special Allowance, Deductions) — Gross and Net are always calculated server-side, never trusted from client input.
2. **HR assigns the structure to an Employee** with an effective date. If the employee already has an active assignment, it's automatically closed out (effective-to = new start date − 1 day), preserving full salary history rather than overwriting it.
3. **Employees apply for leave** (CL/SL/EL). Balances are only deducted on **approval**, not on application.
4. **On approval**, days are deducted from the employee's leave balance up to what's remaining; any days beyond that are recorded as **Loss of Pay (LOP)** on the leave application itself, rather than recalculated later — this keeps payslip generation deterministic.
5. **HR generates a monthly payslip** for an employee. The system:
   - Pulls their currently active salary structure
   - Sums LOP days from all approved leave applications overlapping that pay month
   - Calculates LOP deduction (Gross ÷ 30 standard working days × LOP days)
   - Computes Net Pay = Gross − Deductions − LOP amount
   - Snapshots the leave balances (CL/SL/EL) at that point in time
   - Duplicate generation for the same employee/month is blocked
6. **A PDF payslip is generated** (iText7) containing employee details, earnings/deductions breakdown, LOP, leave balance summary, and net pay.
7. **The payslip is automatically emailed** to the employee's registered address as an attachment, immediately after generation — no separate manual "send" step.
8. **Every email attempt is logged** (`EmailLog`) with a status (`SUCCESS`/`FAILED`), an error category (`INVALID_EMAIL`, `AUTH_ERROR`, `TIMEOUT`, `CONNECTION_ERROR`, `UNKNOWN`), and a `retryable` flag. Only transient failures (timeout/connection) are retryable; permanent failures (bad address, auth misconfiguration) are not — retrying those would never succeed. Retries are capped at 3 attempts, after which the log is marked `MAX_RETRIES_EXCEEDED` and requires manual investigation.
9. **HR Dashboard** aggregates: total active employees, employees processed this month, pending payroll, successful/failed email deliveries, and org-wide leave balance totals by type.

**Note on email delivery status:** a `SUCCESS` status reflects successful SMTP handoff, not confirmed mailbox delivery — most providers (including Gmail) accept mail for any syntactically valid address on a real domain and bounce asynchronously afterward if the mailbox doesn't exist. True bounce detection would require provider-side webhook integration (e.g. SendGrid/SES), which is outside this assignment's scope.

---

## Key Implementation Notes

- **Never trust client-calculated financial values** — gross/net salary and payslip totals are always computed server-side, regardless of what the client sends.
- **Soft delete, not hard delete** — deactivating an employee preserves their historical payslips/leave records (FK-safe) rather than destroying them. Reactivation reuses the same update endpoint with `active: true`.
- **Salary history via effective-dated assignments**, not overwriting a single "current salary" field — supports the "store salary history for future reference" requirement.
- **JWT handling (frontend)**: token stored in `localStorage`, attached to every request via an Axios interceptor; a 401 response anywhere triggers auto-logout and redirect to login.
- **PDF downloads (frontend)**: fetched as an authenticated blob (not a plain `<a href>` link), since the endpoint requires a JWT header; a temporary object URL triggers the browser's save dialog.
- **New employee onboarding**: creating an employee cascades into creating a linked login (`User` record, role `EMPLOYEE`), with an auto-generated username and temporary password returned once in the creation response — shown to HR in a one-time confirmation modal, never persisted or re-displayed.

---

