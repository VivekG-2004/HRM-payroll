package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SalaryAssignmentDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;       // for display in response
    private Long salaryStructureId;
    private String structureName;      // for display in response
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}