package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.DashboardSummaryDTO;
import com.futuretransform.hrm_payroll_backend.entity.EmailLog;
import com.futuretransform.hrm_payroll_backend.entity.LeaveBalance;
import com.futuretransform.hrm_payroll_backend.entity.Payslip;
import com.futuretransform.hrm_payroll_backend.repository.*;
import com.futuretransform.hrm_payroll_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final PayslipRepository payslipRepository;
    private final EmailLogRepository emailLogRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public DashboardSummaryDTO getSummary(Integer month, Integer year) {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        long totalActive = employeeRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActive()))
                .count();

        List<Payslip> payslipsThisMonth = payslipRepository.findByPayMonthAndPayYear(month, year);
        long processedCount = payslipsThisMonth.size();

        dto.setTotalActiveEmployees(totalActive);
        dto.setEmployeesProcessedThisMonth(processedCount);
        dto.setPendingPayroll(Math.max(totalActive - processedCount, 0));

        // Email logs tied to this month's payslips only
        List<Long> payslipIds = payslipsThisMonth.stream().map(Payslip::getId).collect(Collectors.toList());

        long successCount = emailLogRepository.findByStatus(EmailLog.Status.SUCCESS).stream()
                .filter(log -> payslipIds.contains(log.getPayslip().getId()))
                .count();

        long failedCount = emailLogRepository.findByStatus(EmailLog.Status.FAILED).stream()
                .filter(log -> payslipIds.contains(log.getPayslip().getId()))
                .count();

        dto.setPayslipsSentSuccessfully(successCount);
        dto.setFailedEmailDeliveries(failedCount);

        // Org-wide leave balance summary for the year, grouped by leave type code
        List<LeaveBalance> allBalances = leaveBalanceRepository.findByYear(year);
        Map<String, List<LeaveBalance>> grouped = allBalances.stream()
                .collect(Collectors.groupingBy(b -> b.getLeaveType().getCode()));

        List<DashboardSummaryDTO.LeaveTypeSummary> summaries = grouped.entrySet().stream()
                .map(entry -> {
                    long allocated = entry.getValue().stream().mapToLong(LeaveBalance::getAllocated).sum();
                    long used = entry.getValue().stream().mapToLong(LeaveBalance::getUsed).sum();
                    long remaining = entry.getValue().stream().mapToLong(LeaveBalance::getRemaining).sum();
                    return new DashboardSummaryDTO.LeaveTypeSummary(entry.getKey(), allocated, used, remaining);
                })
                .collect(Collectors.toList());

        dto.setLeaveBalanceSummary(summaries);

        return dto;
    }
}