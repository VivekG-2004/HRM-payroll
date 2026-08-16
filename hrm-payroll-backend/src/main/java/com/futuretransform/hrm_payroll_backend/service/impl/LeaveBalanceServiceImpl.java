package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.LeaveBalanceDTO;
import com.futuretransform.hrm_payroll_backend.entity.Employee;
import com.futuretransform.hrm_payroll_backend.entity.LeaveBalance;
import com.futuretransform.hrm_payroll_backend.entity.LeaveType;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.EmployeeRepository;
import com.futuretransform.hrm_payroll_backend.repository.LeaveBalanceRepository;
import com.futuretransform.hrm_payroll_backend.repository.LeaveTypeRepository;
import com.futuretransform.hrm_payroll_backend.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<LeaveBalanceDTO> getBalances(Long employeeId, Integer year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void initializeBalancesForEmployee(Long employeeId, Integer year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

        for (LeaveType type : leaveTypes) {
            boolean exists = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, type.getId(), year)
                    .isPresent();

            if (!exists) {
                LeaveBalance balance = LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(type)
                        .year(year)
                        .allocated(type.getAnnualAllocation())
                        .used(0)
                        .remaining(type.getAnnualAllocation())
                        .build();
                leaveBalanceRepository.save(balance);
            }
        }
    }

    private LeaveBalanceDTO toDTO(LeaveBalance b) {
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.setId(b.getId());
        dto.setEmployeeId(b.getEmployee().getId());
        dto.setLeaveTypeCode(b.getLeaveType().getCode());
        dto.setLeaveTypeName(b.getLeaveType().getName());
        dto.setYear(b.getYear());
        dto.setAllocated(b.getAllocated());
        dto.setUsed(b.getUsed());
        dto.setRemaining(b.getRemaining());
        return dto;
    }
}