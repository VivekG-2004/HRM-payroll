package com.futuretransform.hrm_payroll_backend.config;

import com.futuretransform.hrm_payroll_backend.entity.Employee;
import com.futuretransform.hrm_payroll_backend.entity.User;
import com.futuretransform.hrm_payroll_backend.repository.EmployeeRepository;
import com.futuretransform.hrm_payroll_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("hradmin").isEmpty()) {
            User hr = User.builder()
                    .username("hradmin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(User.Role.HR)
                    .build();
            userRepository.save(hr);
            System.out.println("Seeded HR user -> hradmin / Admin@123");
        }

        if (userRepository.findByUsername("ravi.kumar").isEmpty()) {
            Employee emp = employeeRepository.findByEmpCode("EMP001").orElse(null);
            if (emp != null) {
                User empUser = User.builder()
                        .username("ravi.kumar")
                        .password(passwordEncoder.encode("Employee@123"))
                        .role(User.Role.EMPLOYEE)
                        .employee(emp)
                        .build();
                userRepository.save(empUser);
                System.out.println("Seeded EMPLOYEE user -> ravi.kumar / Employee@123 (linked to EMP001)");
            }
        }
    }
}