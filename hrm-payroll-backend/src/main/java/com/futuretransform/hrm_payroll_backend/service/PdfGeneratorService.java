package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.entity.Payslip;

public interface PdfGeneratorService {
    String generatePdf(Payslip payslip);
}