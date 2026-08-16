package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.GeneratePayslipRequest;
import com.futuretransform.hrm_payroll_backend.dto.PayslipDTO;
import com.futuretransform.hrm_payroll_backend.service.PayslipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;

    @PostMapping("/generate")
    public ResponseEntity<PayslipDTO> generate(@Valid @RequestBody GeneratePayslipRequest request) {
        return ResponseEntity.ok(payslipService.generate(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayslipDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payslipService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayslipDTO>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payslipService.getByEmployee(employeeId));
    }

    @GetMapping
    public ResponseEntity<List<PayslipDTO>> getByMonth(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(payslipService.getByMonth(month, year));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id) throws java.io.IOException {
        PayslipDTO dto = payslipService.getById(id);
        if (dto.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }

        java.io.File file = new java.io.File(dto.getPdfPath());
        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(resource);
    }
}