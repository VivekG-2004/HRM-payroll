-- ============================================
-- Seed Data / Sample Data Script
-- HRM Salary Structure & Payslip Automation Module
-- Run this AFTER schema.sql
-- ============================================

USE hrm_payroll_db;

-- 1. Leave Types (required — annual allocations from spec)
INSERT INTO leave_types (code, name, annual_allocation) VALUES
('CL', 'Casual Leave', 12),
('SL', 'Sick Leave', 12),
('EL', 'Earned Leave', 15);

-- 2. Sample Employees
INSERT INTO employees (emp_code, name, email, department, designation, joining_date, active) VALUES
('EMP001', 'Ravi Kumar', 'ravi.kumar@example.com', 'Engineering', 'Software Developer', '2024-01-15', TRUE),
('EMP002', 'Priya Sharma', 'priya.sharma@example.com', 'Engineering', 'Senior Software Developer', '2023-06-10', TRUE),
('EMP003', 'Arjun Mehta', 'arjun.mehta@example.com', 'HR', 'HR Executive', '2022-11-01', TRUE);

-- 3. Leave Balances for current year (one row per employee per leave type)
INSERT INTO leave_balances (employee_id, leave_type_id, year, allocated, used, remaining)
SELECT e.id, lt.id, 2026, lt.annual_allocation, 0, lt.annual_allocation
FROM employees e
CROSS JOIN leave_types lt;

-- 4. Sample Salary Structures
INSERT INTO salary_structures (structure_name, basic_salary, hra, special_allowance, deductions, gross_salary, net_salary) VALUES
('Software Developer - Standard', 20000, 8000, 7000, 1800, 35000, 33200),
('Senior Developer - Standard', 35000, 14000, 11000, 3200, 60000, 56800),
('HR Executive - Standard', 18000, 7200, 4800, 1500, 30000, 28500);

-- 5. Assign salary structures to employees
INSERT INTO employee_salary_assignments (employee_id, salary_structure_id, effective_from, effective_to) VALUES
(1, 1, '2026-01-01', NULL),
(2, 2, '2026-01-01', NULL),
(3, 3, '2026-01-01', NULL);

-- 6. Sample Leave Applications (mix of statuses — shows the workflow)
INSERT INTO leave_applications (employee_id, leave_type_id, from_date, to_date, days_count, status, applied_on) VALUES
(1, 1, '2026-08-20', '2026-08-22', 3, 'APPROVED', '2026-08-10 10:00:00'),
(2, 2, '2026-08-25', '2026-08-25', 1, 'PENDING', '2026-08-14 09:30:00'),
(1, 3, '2026-09-01', '2026-09-03', 3, 'PENDING', '2026-08-15 11:00:00');

-- Reflect the one APPROVED leave in the balance table (id=1, CL, employee 1)
UPDATE leave_balances
SET used = 3, remaining = 9
WHERE employee_id = 1 AND leave_type_id = 1 AND year = 2026;

-- 7. Users (login accounts) — HR + Employee logins
-- NOTE: passwords below are BCrypt hashes and MUST be regenerated for your own environment.
-- Do not rely on copy-pasted hashes matching arbitrary passwords across BCrypt implementations/salts.
-- Recommended: let your app's DataSeeder (CommandLineRunner) create these on first run instead of
-- hardcoding hashes here — see README for hradmin / ravi.kumar auto-seeding.