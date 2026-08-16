package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_structures")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "structure_name", nullable = false, length = 100)
    private String structureName;

    @Column(name = "basic_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hra;

    @Column(name = "special_allowance", nullable = false, precision = 10, scale = 2)
    private BigDecimal specialAllowance;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "gross_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "net_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}