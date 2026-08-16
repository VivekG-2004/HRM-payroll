package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalaryStructureDTO {
    private Long id;
    private String structureName;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal specialAllowance;
    private BigDecimal deductions;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
}