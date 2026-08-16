package com.futuretransform.hrm_payroll_backend.repository;

import com.futuretransform.hrm_payroll_backend.entity.EmployeeSalaryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeSalaryAssignmentRepository extends JpaRepository<EmployeeSalaryAssignment, Long> {

    // current active assignment = effectiveTo IS NULL
    Optional<EmployeeSalaryAssignment> findByEmployeeIdAndEffectiveToIsNull(Long employeeId);

    java.util.List<EmployeeSalaryAssignment> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
}