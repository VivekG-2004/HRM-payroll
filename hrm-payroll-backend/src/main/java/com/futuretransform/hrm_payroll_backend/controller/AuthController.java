package com.futuretransform.hrm_payroll_backend.controller;

import com.futuretransform.hrm_payroll_backend.dto.LoginRequest;
import com.futuretransform.hrm_payroll_backend.dto.LoginResponse;
import com.futuretransform.hrm_payroll_backend.security.CustomUserDetails;
import com.futuretransform.hrm_payroll_backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String role = userDetails.getUser().getRole().name();
        String token = jwtUtil.generateToken(userDetails.getUsername(), role);

        Long employeeId = userDetails.getUser().getEmployee() != null
                ? userDetails.getUser().getEmployee().getId() : null;
        String employeeName = userDetails.getUser().getEmployee() != null
                ? userDetails.getUser().getEmployee().getName() : null;

        return ResponseEntity.ok(new LoginResponse(token, userDetails.getUsername(), role, employeeId, employeeName));
    }
}