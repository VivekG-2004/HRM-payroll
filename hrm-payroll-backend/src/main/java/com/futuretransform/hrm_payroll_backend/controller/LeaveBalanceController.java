package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.LeaveBalanceDTO;
import com.futuretransform.hrm_payroll_backend.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<LeaveBalanceDTO>> getBalances(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year) {
        int y = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(leaveBalanceService.getBalances(employeeId, y));
    }
}