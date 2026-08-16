package com.futuretransform.hrm_payroll_backend.repository;

import com.futuretransform.hrm_payroll_backend.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByStatus(EmailLog.Status status);
    long countByStatus(EmailLog.Status status);
}