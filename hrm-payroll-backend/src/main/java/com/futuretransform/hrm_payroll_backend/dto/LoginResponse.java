package com.futuretransform.hrm_payroll_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private Long employeeId;   // null for pure HR-only users with no linked employee
    private String employeeName;
}