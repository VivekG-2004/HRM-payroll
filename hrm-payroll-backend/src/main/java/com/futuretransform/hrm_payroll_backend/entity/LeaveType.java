package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String code; // CL, SL, EL

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "annual_allocation", nullable = false)
    private Integer annualAllocation;
}