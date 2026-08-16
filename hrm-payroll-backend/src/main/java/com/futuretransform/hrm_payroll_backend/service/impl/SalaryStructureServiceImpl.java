package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.dto.SalaryStructureDTO;
import com.futuretransform.hrm_payroll_backend.entity.SalaryStructure;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.SalaryStructureRepository;
import com.futuretransform.hrm_payroll_backend.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository repository;

    @Override
    public SalaryStructureDTO create(SalaryStructureDTO dto) {
        SalaryStructure entity = buildEntityWithComputedValues(new SalaryStructure(), dto);
        return toDTO(repository.save(entity));
    }

    @Override
    public SalaryStructureDTO update(Long id, SalaryStructureDTO dto) {
        SalaryStructure entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + id));
        entity = buildEntityWithComputedValues(entity, dto);
        return toDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Salary structure not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public SalaryStructureDTO getById(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + id)));
    }

    @Override
    public List<SalaryStructureDTO> getAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Server-side computation — never trust client-sent gross/net values
    private SalaryStructure buildEntityWithComputedValues(SalaryStructure entity, SalaryStructureDTO dto) {
        BigDecimal basic = dto.getBasicSalary();
        BigDecimal hra = dto.getHra();
        BigDecimal special = dto.getSpecialAllowance();
        BigDecimal deductions = dto.getDeductions() != null ? dto.getDeductions() : BigDecimal.ZERO;

        BigDecimal gross = basic.add(hra).add(special);
        BigDecimal net = gross.subtract(deductions);

        entity.setStructureName(dto.getStructureName());
        entity.setBasicSalary(basic);
        entity.setHra(hra);
        entity.setSpecialAllowance(special);
        entity.setDeductions(deductions);
        entity.setGrossSalary(gross);
        entity.setNetSalary(net);

        return entity;
    }

    private SalaryStructureDTO toDTO(SalaryStructure e) {
        SalaryStructureDTO dto = new SalaryStructureDTO();
        dto.setId(e.getId());
        dto.setStructureName(e.getStructureName());
        dto.setBasicSalary(e.getBasicSalary());
        dto.setHra(e.getHra());
        dto.setSpecialAllowance(e.getSpecialAllowance());
        dto.setDeductions(e.getDeductions());
        dto.setGrossSalary(e.getGrossSalary());
        dto.setNetSalary(e.getNetSalary());
        return dto;
    }
}