package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.EmployeeDTO;
import com.futuretransform.hrm_payroll_backend.entity.Employee;
import com.futuretransform.hrm_payroll_backend.entity.User;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.EmployeeRepository;
import com.futuretransform.hrm_payroll_backend.repository.UserRepository;
import com.futuretransform.hrm_payroll_backend.service.EmployeeService;
import com.futuretransform.hrm_payroll_backend.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public EmployeeDTO create(EmployeeDTO dto) {
        Employee employee = Employee.builder()
                .empCode(dto.getEmpCode())
                .name(dto.getName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .designation(dto.getDesignation())
                .joiningDate(dto.getJoiningDate())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Employee saved = employeeRepository.save(employee);
        leaveBalanceService.initializeBalancesForEmployee(saved.getId(), java.time.LocalDate.now().getYear());

        // Cascade: create a linked login for this employee
        String username = saved.getEmpCode().toLowerCase(); // e.g. "emp002"
        String tempPassword = generateTempPassword();

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(tempPassword))
                .role(User.Role.EMPLOYEE)
                .employee(saved)
                .build();
        userRepository.save(user);

        EmployeeDTO result = toDTO(saved);
        result.setUsername(username);
        result.setTemporaryPassword(tempPassword); // shown once, HR must communicate it to the employee
        return result;
    }

    private String generateTempPassword() {
        // Simple readable temp password for assignment purposes
        return "Welcome@" + (int) (Math.random() * 9000 + 1000);
    }

    @Override
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setJoiningDate(dto.getJoiningDate());
        if (dto.getActive() != null) employee.setActive(dto.getActive());
        // empCode intentionally not editable after creation

        return toDTO(employeeRepository.save(employee));
    }

    @Override
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setActive(false); // soft delete — preserves payslip/leave history
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeDTO getById(Long id) {
        return toDTO(employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id)));
    }

    @Override
    public List<EmployeeDTO> getAll() {
        return employeeRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private EmployeeDTO toDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setEmpCode(e.getEmpCode());
        dto.setName(e.getName());
        dto.setEmail(e.getEmail());
        dto.setDepartment(e.getDepartment());
        dto.setDesignation(e.getDesignation());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setActive(e.getActive());
        return dto;
    }
}