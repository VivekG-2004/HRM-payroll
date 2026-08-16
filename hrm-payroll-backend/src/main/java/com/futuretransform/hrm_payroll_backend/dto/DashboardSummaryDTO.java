package com.futuretransform.hrm_payroll_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardSummaryDTO {
    private long totalActiveEmployees;
    private long employeesProcessedThisMonth;   // payslips generated for current pay period
    private long pendingPayroll;                 // active employees not yet processed this month
    private long payslipsSentSuccessfully;        // EmailLog SUCCESS count, this month
    private long failedEmailDeliveries;           // EmailLog FAILED count, this month
    private List<LeaveTypeSummary> leaveBalanceSummary; // org-wide totals per leave type

    @Data
    public static class LeaveTypeSummary {
        private String leaveTypeCode;
        private long totalAllocated;
        private long totalUsed;
        private long totalRemaining;

        public LeaveTypeSummary(String code, long allocated, long used, long remaining) {
            this.leaveTypeCode = code;
            this.totalAllocated = allocated;
            this.totalUsed = used;
            this.totalRemaining = remaining;
        }
    }
}