package com.futuretransform.hrm_payroll_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_code", unique = true, nullable = false, length = 20)
    private String empCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 50)
    private String department;

    @Column(length = 50)
    private String designation;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(nullable = false)
    private Boolean active = true;
}