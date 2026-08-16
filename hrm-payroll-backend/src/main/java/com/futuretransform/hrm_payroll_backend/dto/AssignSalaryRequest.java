package com.futuretransform.hrm_payroll_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignSalaryRequest {
    @NotNull
    private Long employeeId;

    @NotNull
    private Long salaryStructureId;

    @NotNull
    private LocalDate effectiveFrom;
}