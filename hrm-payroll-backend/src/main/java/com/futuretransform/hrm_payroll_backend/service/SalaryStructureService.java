package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.SalaryStructureDTO;
import java.util.List;

public interface SalaryStructureService {
    SalaryStructureDTO create(SalaryStructureDTO dto);
    SalaryStructureDTO update(Long id, SalaryStructureDTO dto);
    void delete(Long id);
    SalaryStructureDTO getById(Long id);
    List<SalaryStructureDTO> getAll();
}