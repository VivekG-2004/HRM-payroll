package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payslip_id", nullable = false)
    private Payslip payslip;

    @Column(name = "sent_to", nullable = false, length = 100)
    private String sentTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 1;

    @Column(name = "last_attempt_at")
    @Builder.Default
    private LocalDateTime lastAttemptAt = LocalDateTime.now();

    @Column(name = "error_category", length = 30)
    private String errorCategory; // INVALID_EMAIL, AUTH_ERROR, TIMEOUT, CONNECTION_ERROR, UNKNOWN

    @Column(name = "retryable")
    private Boolean retryable = true;

    public enum Status {
        SUCCESS, FAILED
    }
}