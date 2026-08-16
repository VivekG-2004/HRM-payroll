package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.entity.Payslip;

public interface EmailService {
    void sendPayslipEmail(Payslip payslip);
    void retryFailedEmail(Long emailLogId);
}