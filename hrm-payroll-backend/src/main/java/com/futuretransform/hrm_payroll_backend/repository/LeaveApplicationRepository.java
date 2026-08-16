package com.futuretransform.hrm_payroll_backend.repository;

import com.futuretransform.hrm_payroll_backend.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByEmployeeId(Long employeeId);
    List<LeaveApplication> findByStatus(LeaveApplication.Status status);
}