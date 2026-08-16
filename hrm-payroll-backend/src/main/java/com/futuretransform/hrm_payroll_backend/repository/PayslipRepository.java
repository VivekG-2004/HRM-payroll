package com.futuretransform.hrm_payroll_backend.repository;

import com.futuretransform.hrm_payroll_backend.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    Optional<Payslip> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, Integer payMonth, Integer payYear);
    List<Payslip> findByPayMonthAndPayYear(Integer payMonth, Integer payYear);
    List<Payslip> findByEmployeeId(Long employeeId);
}