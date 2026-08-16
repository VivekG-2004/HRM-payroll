package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.ApplyLeaveRequest;
import com.futuretransform.hrm_payroll_backend.dto.LeaveApplicationDTO;
import com.futuretransform.hrm_payroll_backend.entity.*;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.*;
import com.futuretransform.hrm_payroll_backend.service.LeaveApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public LeaveApplicationDTO apply(ApplyLeaveRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        LeaveType leaveType = leaveTypeRepository.findByCode(request.getLeaveTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid leave type: " + request.getLeaveTypeCode()));

        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new IllegalArgumentException("toDate cannot be before fromDate");
        }

        int days = (int) (ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1);

        LeaveApplication application = LeaveApplication.builder()
                .employee(employee)
                .leaveType(leaveType)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .daysCount(days)
                .status(LeaveApplication.Status.PENDING)
                .build();

        return toDTO(leaveApplicationRepository.save(application));
        // Note: balance is NOT deducted here — only on approval (see approve())
    }

    @Override
    @Transactional
    public LeaveApplicationDTO approve(Long leaveApplicationId) {
        LeaveApplication application = leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found: " + leaveApplicationId));

        if (application.getStatus() != LeaveApplication.Status.PENDING) {
            throw new IllegalArgumentException("Only PENDING applications can be approved");
        }

        int year = application.getFromDate().getYear();

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        application.getEmployee().getId(),
                        application.getLeaveType().getId(),
                        year)
                .orElseThrow(() -> new ResourceNotFoundException("No leave balance found for this employee/type/year"));

        int balanceBeforeApproval = balance.getRemaining();
        int requestedDays = application.getDaysCount();

        // How many of the requested days are covered by balance vs. LOP
        int coveredDays = Math.min(requestedDays, balanceBeforeApproval);
        int lopDays = requestedDays - coveredDays;

        int newUsed = balance.getUsed() + coveredDays;
        int newRemaining = balance.getAllocated() - newUsed;

        balance.setUsed(newUsed);
        balance.setRemaining(Math.max(newRemaining, 0));
        leaveBalanceRepository.save(balance);

        application.setLopDays(lopDays);
        application.setStatus(LeaveApplication.Status.APPROVED);
        return toDTO(leaveApplicationRepository.save(application));
    }

    @Override
    @Transactional
    public LeaveApplicationDTO reject(Long leaveApplicationId) {
        LeaveApplication application = leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found: " + leaveApplicationId));

        if (application.getStatus() != LeaveApplication.Status.PENDING) {
            throw new IllegalArgumentException("Only PENDING applications can be rejected");
        }

        application.setStatus(LeaveApplication.Status.REJECTED);
        return toDTO(leaveApplicationRepository.save(application));
    }

    @Override
    public List<LeaveApplicationDTO> getByEmployee(Long employeeId) {
        return leaveApplicationRepository.findByEmployeeId(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<LeaveApplicationDTO> getPending() {
        return leaveApplicationRepository.findByStatus(LeaveApplication.Status.PENDING)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private LeaveApplicationDTO toDTO(LeaveApplication a) {
        LeaveApplicationDTO dto = new LeaveApplicationDTO();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getName());
        dto.setLeaveTypeCode(a.getLeaveType().getCode());
        dto.setFromDate(a.getFromDate());
        dto.setToDate(a.getToDate());
        dto.setDaysCount(a.getDaysCount());
        dto.setStatus(a.getStatus().name());
        dto.setAppliedOn(a.getAppliedOn());
        return dto;
    }
}