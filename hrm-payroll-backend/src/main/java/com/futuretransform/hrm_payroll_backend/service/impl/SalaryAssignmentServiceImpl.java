package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.AssignSalaryRequest;
import com.futuretransform.hrm_payroll_backend.dto.SalaryAssignmentDTO;
import com.futuretransform.hrm_payroll_backend.entity.Employee;
import com.futuretransform.hrm_payroll_backend.entity.EmployeeSalaryAssignment;
import com.futuretransform.hrm_payroll_backend.entity.SalaryStructure;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.EmployeeRepository;
import com.futuretransform.hrm_payroll_backend.repository.EmployeeSalaryAssignmentRepository;
import com.futuretransform.hrm_payroll_backend.repository.SalaryStructureRepository;
import com.futuretransform.hrm_payroll_backend.service.SalaryAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryAssignmentServiceImpl implements SalaryAssignmentService {

    private final EmployeeSalaryAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryStructureRepository;

    @Override
    @Transactional
    public SalaryAssignmentDTO assign(AssignSalaryRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        SalaryStructure structure = salaryStructureRepository.findById(request.getSalaryStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + request.getSalaryStructureId()));

        // Close out the currently active assignment, if one exists
        Optional<EmployeeSalaryAssignment> currentOpt =
                assignmentRepository.findByEmployeeIdAndEffectiveToIsNull(employee.getId());

        currentOpt.ifPresent(current -> {
            // new assignment must start after the current one started
            if (!request.getEffectiveFrom().isAfter(current.getEffectiveFrom())) {
                throw new IllegalArgumentException(
                        "New effectiveFrom date must be after the current assignment's start date ("
                                + current.getEffectiveFrom() + ")");
            }
            current.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
            assignmentRepository.save(current);
        });

        EmployeeSalaryAssignment newAssignment = EmployeeSalaryAssignment.builder()
                .employee(employee)
                .salaryStructure(structure)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(null)
                .build();

        return toDTO(assignmentRepository.save(newAssignment));
    }

    @Override
    public SalaryAssignmentDTO getCurrent(Long employeeId) {
        EmployeeSalaryAssignment current = assignmentRepository.findByEmployeeIdAndEffectiveToIsNull(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active salary assignment for employee id: " + employeeId));
        return toDTO(current);
    }

    @Override
    public List<SalaryAssignmentDTO> getHistory(Long employeeId) {
        return assignmentRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private SalaryAssignmentDTO toDTO(EmployeeSalaryAssignment a) {
        SalaryAssignmentDTO dto = new SalaryAssignmentDTO();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getName());
        dto.setSalaryStructureId(a.getSalaryStructure().getId());
        dto.setStructureName(a.getSalaryStructure().getStructureName());
        dto.setEffectiveFrom(a.getEffectiveFrom());
        dto.setEffectiveTo(a.getEffectiveTo());
        return dto;
    }
}