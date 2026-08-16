package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;

@Data
public class LeaveBalanceDTO {
    private Long id;
    private Long employeeId;
    private String leaveTypeCode;   // CL, SL, EL
    private String leaveTypeName;
    private Integer year;
    private Integer allocated;
    private Integer used;
    private Integer remaining;
}