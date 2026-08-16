package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.PayslipDTO;
import com.futuretransform.hrm_payroll_backend.entity.User;
import com.futuretransform.hrm_payroll_backend.security.CustomUserDetails;
import com.futuretransform.hrm_payroll_backend.service.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employee/payslips")
@RequiredArgsConstructor
public class EmployeePayslipController {

    private final PayslipService payslipService;

    @GetMapping("/my/{employeeId}")
    public ResponseEntity<List<PayslipDTO>> getMyPayslips(
            @PathVariable Long employeeId,
            Authentication authentication) {

        validateOwnership(employeeId, authentication);
        return ResponseEntity.ok(payslipService.getByEmployee(employeeId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMyPayslip(
            @PathVariable Long id,
            Authentication authentication) throws IOException {

        PayslipDTO dto = payslipService.getById(id);
        validateOwnership(dto.getEmployeeId(), authentication);

        File file = new File(dto.getPdfPath());
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    private void validateOwnership(Long requestedEmployeeId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();

        boolean isHr = currentUser.getRole() == User.Role.HR;
        boolean isOwner = currentUser.getEmployee() != null
                && currentUser.getEmployee().getId().equals(requestedEmployeeId);

        if (!isHr && !isOwner) {
            throw new AccessDeniedException("You are not authorized to view this employee's payslips");
        }
    }
}