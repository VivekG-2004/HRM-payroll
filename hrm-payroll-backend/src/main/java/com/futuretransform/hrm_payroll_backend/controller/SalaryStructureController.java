package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.SalaryStructureDTO;
import com.futuretransform.hrm_payroll_backend.service.SalaryStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/salary-structures")
@RequiredArgsConstructor
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    @PostMapping
    public ResponseEntity<SalaryStructureDTO> create(@Valid @RequestBody SalaryStructureDTO dto) {
        return ResponseEntity.ok(salaryStructureService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryStructureDTO> update(@PathVariable Long id, @Valid @RequestBody SalaryStructureDTO dto) {
        return ResponseEntity.ok(salaryStructureService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salaryStructureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryStructureDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salaryStructureService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SalaryStructureDTO>> getAll() {
        return ResponseEntity.ok(salaryStructureService.getAll());
    }
}