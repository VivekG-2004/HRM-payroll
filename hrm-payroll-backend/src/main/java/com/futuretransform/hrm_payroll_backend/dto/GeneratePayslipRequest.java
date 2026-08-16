package com.futuretransform.hrm_payroll_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeneratePayslipRequest {
    @NotNull
    private Long employeeId;

    @NotNull @Min(1) @Max(12)
    private Integer payMonth;

    @NotNull
    private Integer payYear;
}