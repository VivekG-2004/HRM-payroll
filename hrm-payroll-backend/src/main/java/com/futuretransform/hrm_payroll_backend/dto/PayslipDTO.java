package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayslipDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empCode;
    private String department;
    private String designation;
    private Integer payMonth;
    private Integer payYear;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal specialAllowance;
    private BigDecimal grossSalary;
    private BigDecimal deductions;
    private Integer lopDays;
    private BigDecimal lopAmount;
    private BigDecimal netSalary;
    private Integer clBalance;
    private Integer slBalance;
    private Integer elBalance;
    private String pdfPath;
    private LocalDateTime generatedOn;
}