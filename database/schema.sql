CREATE DATABASE hrm_payroll_db;
USE hrm_payroll_db;

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    department VARCHAR(50),
    designation VARCHAR(50),
    joining_date DATE,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    employee_id BIGINT UNIQUE,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE salary_structures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    structure_name VARCHAR(100) NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    hra DECIMAL(10,2) NOT NULL,
    special_allowance DECIMAL(10,2) NOT NULL,
    deductions DECIMAL(10,2) DEFAULT 0,
    gross_salary DECIMAL(10,2) NOT NULL,
    net_salary DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_salary_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    salary_structure_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (salary_structure_id) REFERENCES salary_structures(id)
);

CREATE TABLE leave_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    annual_allocation INT NOT NULL
);

CREATE TABLE leave_balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    year INT NOT NULL,
    allocated INT NOT NULL,
    used INT DEFAULT 0,
    remaining INT NOT NULL,
    UNIQUE KEY uq_emp_leave_year (employee_id, leave_type_id, year),
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE TABLE leave_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    days_count INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE TABLE payslips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    pay_month INT NOT NULL,
    pay_year INT NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    hra DECIMAL(10,2) NOT NULL,
    special_allowance DECIMAL(10,2) NOT NULL,
    gross_salary DECIMAL(10,2) NOT NULL,
    deductions DECIMAL(10,2) NOT NULL,
    lop_days INT DEFAULT 0,
    lop_amount DECIMAL(10,2) DEFAULT 0,
    net_salary DECIMAL(10,2) NOT NULL,
    cl_balance INT,
    sl_balance INT,
    el_balance INT,
    pdf_path VARCHAR(255),
    generated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_emp_month_year (employee_id, pay_month, pay_year),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE email_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payslip_id BIGINT NOT NULL,
    sent_to VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(500),
    attempt_count INT DEFAULT 1,
    last_attempt_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payslip_id) REFERENCES payslips(id)
);

INSERT INTO leave_types (code, name, annual_allocation) VALUES
('CL', 'Casual Leave', 12),
('SL', 'Sick Leave', 12),
('EL', 'Earned Leave', 15);

INSERT INTO employees (emp_code, name, email, department, designation, joining_date, active)
VALUES ('EMP001', 'Ravi Kumar', 'your.test.email@gmail.com', 'Engineering', 'Software Developer', '2024-01-15', TRUE);

INSERT INTO leave_balances (employee_id, leave_type_id, year, allocated, used, remaining)
SELECT 1, id, 2026, annual_allocation, 0, annual_allocation FROM leave_types;

INSERT INTO salary_structures (structure_name, basic_salary, hra, special_allowance, deductions, gross_salary, net_salary)
VALUES ('Software Developer - Standard', 20000, 8000, 7000, 1800, 35000, 33200);

INSERT INTO employee_salary_assignments (employee_id, salary_structure_id, effective_from, effective_to)
VALUES (1, 1, '2026-01-01', NULL);

select * from employees;

ALTER TABLE leave_applications 
ADD COLUMN lop_days INT DEFAULT 0;

ALTER TABLE email_logs ADD COLUMN error_category VARCHAR(30);
ALTER TABLE email_logs ADD COLUMN retryable BOOLEAN DEFAULT TRUE;