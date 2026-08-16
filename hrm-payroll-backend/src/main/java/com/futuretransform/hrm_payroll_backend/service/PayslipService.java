package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.GeneratePayslipRequest;
import com.futuretransform.hrm_payroll_backend.dto.PayslipDTO;
import java.util.List;

public interface PayslipService {
    PayslipDTO generate(GeneratePayslipRequest request);
    PayslipDTO getById(Long id);
    List<PayslipDTO> getByEmployee(Long employeeId);
    List<PayslipDTO> getByMonth(Integer month, Integer year);
}