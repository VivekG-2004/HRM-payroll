package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.AssignSalaryRequest;
import com.futuretransform.hrm_payroll_backend.dto.SalaryAssignmentDTO;
import com.futuretransform.hrm_payroll_backend.service.SalaryAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/salary-assignments")
@RequiredArgsConstructor
public class SalaryAssignmentController {

    private final SalaryAssignmentService salaryAssignmentService;

    @PostMapping
    public ResponseEntity<SalaryAssignmentDTO> assign(@Valid @RequestBody AssignSalaryRequest request) {
        return ResponseEntity.ok(salaryAssignmentService.assign(request));
    }

    @GetMapping("/employee/{employeeId}/current")
    public ResponseEntity<SalaryAssignmentDTO> getCurrent(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryAssignmentService.getCurrent(employeeId));
    }

    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<List<SalaryAssignmentDTO>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(salaryAssignmentService.getHistory(employeeId));
    }
}