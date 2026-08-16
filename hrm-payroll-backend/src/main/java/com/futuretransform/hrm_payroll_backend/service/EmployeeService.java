package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    void delete(Long id);
    EmployeeDTO getById(Long id);
    List<EmployeeDTO> getAll();
}
