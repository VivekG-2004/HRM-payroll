package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.ApplyLeaveRequest;
import com.futuretransform.hrm_payroll_backend.dto.LeaveApplicationDTO;
import com.futuretransform.hrm_payroll_backend.service.LeaveApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/leave-applications")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    @PostMapping
    public ResponseEntity<LeaveApplicationDTO> apply(@Valid @RequestBody ApplyLeaveRequest request) {
        return ResponseEntity.ok(leaveApplicationService.apply(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveApplicationDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.approve(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveApplicationDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(leaveApplicationService.reject(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveApplicationDTO>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveApplicationService.getByEmployee(employeeId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveApplicationDTO>> getPending() {
        return ResponseEntity.ok(leaveApplicationService.getPending());
    }
}