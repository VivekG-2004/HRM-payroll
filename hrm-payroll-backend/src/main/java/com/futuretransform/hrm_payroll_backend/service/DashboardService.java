package com.futuretransform.hrm_payroll_backend.service;

import com.futuretransform.hrm_payroll_backend.dto.DashboardSummaryDTO;

public interface DashboardService {
    DashboardSummaryDTO getSummary(Integer month, Integer year);
}