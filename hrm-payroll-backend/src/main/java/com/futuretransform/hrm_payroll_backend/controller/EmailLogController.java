package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.EmailLogDTO;
import com.futuretransform.hrm_payroll_backend.entity.EmailLog;
import com.futuretransform.hrm_payroll_backend.repository.EmailLogRepository;
import com.futuretransform.hrm_payroll_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hr/email-logs")
@RequiredArgsConstructor
public class EmailLogController {

    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<EmailLogDTO>> getAll() {
        return ResponseEntity.ok(emailLogRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/failed")
    public ResponseEntity<List<EmailLogDTO>> getFailed() {
        return ResponseEntity.ok(emailLogRepository.findByStatus(EmailLog.Status.FAILED)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retry(@PathVariable Long id) {
        emailService.retryFailedEmail(id);
        return ResponseEntity.ok().build();
    }

    private EmailLogDTO toDTO(EmailLog e) {
        EmailLogDTO dto = new EmailLogDTO();
        dto.setId(e.getId());
        dto.setPayslipId(e.getPayslip().getId());
        dto.setSentTo(e.getSentTo());
        dto.setStatus(e.getStatus().name());
        dto.setErrorMessage(e.getErrorMessage());
        dto.setAttemptCount(e.getAttemptCount());
        dto.setLastAttemptAt(e.getLastAttemptAt());
        dto.setErrorCategory(e.getErrorCategory());
        dto.setRetryable(e.getRetryable());
        return dto;
    }
}