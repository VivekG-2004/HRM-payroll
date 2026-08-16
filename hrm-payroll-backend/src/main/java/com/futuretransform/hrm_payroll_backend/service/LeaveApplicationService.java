package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.ApplyLeaveRequest;
import com.futuretransform.hrm_payroll_backend.dto.LeaveApplicationDTO;
import java.util.List;

public interface LeaveApplicationService {
    LeaveApplicationDTO apply(ApplyLeaveRequest request);
    LeaveApplicationDTO approve(Long leaveApplicationId);
    LeaveApplicationDTO reject(Long leaveApplicationId);
    List<LeaveApplicationDTO> getByEmployee(Long employeeId);
    List<LeaveApplicationDTO> getPending();
}