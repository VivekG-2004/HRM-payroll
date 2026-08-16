package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.AssignSalaryRequest;
import com.futuretransform.hrm_payroll_backend.dto.SalaryAssignmentDTO;
import java.util.List;

public interface SalaryAssignmentService {
    SalaryAssignmentDTO assign(AssignSalaryRequest request);
    SalaryAssignmentDTO getCurrent(Long employeeId);
    List<SalaryAssignmentDTO> getHistory(Long employeeId);
}