package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.GeneratePayslipRequest;
import com.futuretransform.hrm_payroll_backend.dto.PayslipDTO;
import com.futuretransform.hrm_payroll_backend.entity.*;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.*;
import com.futuretransform.hrm_payroll_backend.service.EmailService;
import com.futuretransform.hrm_payroll_backend.service.PayslipService;
import com.futuretransform.hrm_payroll_backend.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private static final int STANDARD_WORKING_DAYS = 30;

    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryAssignmentRepository assignmentRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    @Override
    @Transactional
    public PayslipDTO generate(GeneratePayslipRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        // Prevent duplicate generation for the same month
        payslipRepository.findByEmployeeIdAndPayMonthAndPayYear(
                        employee.getId(), request.getPayMonth(), request.getPayYear())
                .ifPresent(p -> {
                    throw new IllegalArgumentException(
                            "Payslip already generated for this employee for " + request.getPayMonth() + "/" + request.getPayYear());
                });

        // Get salary structure active during that pay period
        EmployeeSalaryAssignment assignment = assignmentRepository
                .findByEmployeeIdAndEffectiveToIsNull(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active salary assignment for employee id: " + employee.getId()));
        SalaryStructure structure = assignment.getSalaryStructure();

        // Sum LOP days from approved leave applications overlapping this pay month
        YearMonth ym = YearMonth.of(request.getPayYear(), request.getPayMonth());
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        List<LeaveApplication> approvedInMonth = leaveApplicationRepository.findByEmployeeId(employee.getId())
                .stream()
                .filter(la -> la.getStatus() == LeaveApplication.Status.APPROVED)
                .filter(la -> !la.getFromDate().isAfter(monthEnd) && !la.getToDate().isBefore(monthStart))
                .collect(Collectors.toList());

        int totalLopDays = approvedInMonth.stream()
                .mapToInt(la -> la.getLopDays() != null ? la.getLopDays() : 0)
                .sum();

        // Per-day salary based on gross, for LOP deduction
        BigDecimal perDaySalary = structure.getGrossSalary()
                .divide(BigDecimal.valueOf(STANDARD_WORKING_DAYS), 2, RoundingMode.HALF_UP);
        BigDecimal lopAmount = perDaySalary.multiply(BigDecimal.valueOf(totalLopDays));

        BigDecimal netSalary = structure.getGrossSalary()
                .subtract(structure.getDeductions())
                .subtract(lopAmount);

        // Snapshot current leave balances
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndYear(employee.getId(), request.getPayYear());
        Integer clBal = findBalance(balances, "CL");
        Integer slBal = findBalance(balances, "SL");
        Integer elBal = findBalance(balances, "EL");

        Payslip payslip = Payslip.builder()
                .employee(employee)
                .payMonth(request.getPayMonth())
                .payYear(request.getPayYear())
                .basicSalary(structure.getBasicSalary())
                .hra(structure.getHra())
                .specialAllowance(structure.getSpecialAllowance())
                .grossSalary(structure.getGrossSalary())
                .deductions(structure.getDeductions())
                .lopDays(totalLopDays)
                .lopAmount(lopAmount)
                .netSalary(netSalary)
                .clBalance(clBal)
                .slBalance(slBal)
                .elBalance(elBal)
                .build();

        Payslip saved = payslipRepository.save(payslip);

        String pdfPath = pdfGeneratorService.generatePdf(saved);
        saved.setPdfPath(pdfPath);
        saved = payslipRepository.save(saved);

        emailService.sendPayslipEmail(saved);

        return toDTO(saved);
    }

    private Integer findBalance(List<LeaveBalance> balances, String code) {
        return balances.stream()
                .filter(b -> b.getLeaveType().getCode().equals(code))
                .map(LeaveBalance::getRemaining)
                .findFirst()
                .orElse(0);
    }

    @Override
    public PayslipDTO getById(Long id) {
        return toDTO(payslipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found with id: " + id)));
    }

    @Override
    public List<PayslipDTO> getByEmployee(Long employeeId) {
        return payslipRepository.findByEmployeeId(employeeId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PayslipDTO> getByMonth(Integer month, Integer year) {
        return payslipRepository.findByPayMonthAndPayYear(month, year).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private PayslipDTO toDTO(Payslip p) {
        PayslipDTO dto = new PayslipDTO();
        dto.setId(p.getId());
        dto.setEmployeeId(p.getEmployee().getId());
        dto.setEmployeeName(p.getEmployee().getName());
        dto.setEmpCode(p.getEmployee().getEmpCode());
        dto.setDepartment(p.getEmployee().getDepartment());
        dto.setDesignation(p.getEmployee().getDesignation());
        dto.setPayMonth(p.getPayMonth());
        dto.setPayYear(p.getPayYear());
        dto.setBasicSalary(p.getBasicSalary());
        dto.setHra(p.getHra());
        dto.setSpecialAllowance(p.getSpecialAllowance());
        dto.setGrossSalary(p.getGrossSalary());
        dto.setDeductions(p.getDeductions());
        dto.setLopDays(p.getLopDays());
        dto.setLopAmount(p.getLopAmount());
        dto.setNetSalary(p.getNetSalary());
        dto.setClBalance(p.getClBalance());
        dto.setSlBalance(p.getSlBalance());
        dto.setElBalance(p.getElBalance());
        dto.setPdfPath(p.getPdfPath());
        dto.setGeneratedOn(p.getGeneratedOn());
        return dto;
    }
}