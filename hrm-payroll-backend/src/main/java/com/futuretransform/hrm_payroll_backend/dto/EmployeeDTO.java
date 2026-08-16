package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long id;
    private String empCode;
    private String name;
    private String email;
    private String department;
    private String designation;
    private LocalDate joiningDate;
    private Boolean active;
    private String temporaryPassword;
    private String username;
}