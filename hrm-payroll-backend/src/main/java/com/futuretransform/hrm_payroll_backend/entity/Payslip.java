package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payslips",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month", "pay_year"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_month", nullable = false)
    private Integer payMonth;

    @Column(name = "pay_year", nullable = false)
    private Integer payYear;

    @Column(name = "basic_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hra;

    @Column(name = "special_allowance", nullable = false, precision = 10, scale = 2)
    private BigDecimal specialAllowance;

    @Column(name = "gross_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossSalary;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deductions;

    @Column(name = "lop_days")
    private Integer lopDays = 0;

    @Column(name = "lop_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lopAmount = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "cl_balance")
    private Integer clBalance;

    @Column(name = "sl_balance")
    private Integer slBalance;

    @Column(name = "el_balance")
    private Integer elBalance;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "generated_on")
    @Builder.Default
    private LocalDateTime generatedOn = LocalDateTime.now();
}