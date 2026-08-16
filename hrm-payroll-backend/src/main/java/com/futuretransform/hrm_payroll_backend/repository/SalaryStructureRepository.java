package com.futuretransform.hrm_payroll_backend.repository;

import com.futuretransform.hrm_payroll_backend.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
}