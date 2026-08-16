package com.futuretransform.hrm_payroll_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplyLeaveRequest {
    @NotNull
    private Long employeeId;

    @NotBlank
    private String leaveTypeCode; // CL / SL / EL

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;
}