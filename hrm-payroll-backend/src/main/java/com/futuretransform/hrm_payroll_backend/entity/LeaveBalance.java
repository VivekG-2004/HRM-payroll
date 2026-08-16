package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "year"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer allocated;

    @Column(nullable = false)
    @Builder.Default
    private Integer used = 0;

    @Column(nullable = false)
    private Integer remaining;
}