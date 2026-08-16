package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveApplicationDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveTypeCode;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer daysCount;
    private String status;
    private LocalDateTime appliedOn;
}