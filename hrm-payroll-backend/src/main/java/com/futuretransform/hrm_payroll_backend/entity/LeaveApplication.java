package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_applications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lop_days")
    @Builder.Default
    private Integer lopDays = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "days_count", nullable = false)
    private Integer daysCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "applied_on")
    @Builder.Default
    private LocalDateTime appliedOn = LocalDateTime.now();

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}