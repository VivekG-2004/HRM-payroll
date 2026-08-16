package com.futuretransform.hrm_payroll_backend.repository;

import com.futuretransform.hrm_payroll_backend.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    Optional<LeaveType> findByCode(String code);
}