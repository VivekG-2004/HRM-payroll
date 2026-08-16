package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.LeaveBalanceDTO;
import java.util.List;

public interface LeaveBalanceService {
    List<LeaveBalanceDTO> getBalances(Long employeeId, Integer year);
    void initializeBalancesForEmployee(Long employeeId, Integer year); // used in Step 6 gap fix too
}