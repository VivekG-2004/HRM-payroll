package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailLogDTO {
    private Long id;
    private Long payslipId;
    private String sentTo;
    private String status;
    private String errorMessage;
    private Integer attemptCount;
    private LocalDateTime lastAttemptAt;
    private String errorCategory;
    private Boolean retryable;
}