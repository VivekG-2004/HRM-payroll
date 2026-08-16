package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.DashboardSummaryDTO;
import com.futuretransform.hrm_payroll_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hr/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        int y = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(dashboardService.getSummary(m, y));
    }
}