package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.entity.Payslip;
import com.futuretransform.hrm_payroll_backend.service.PdfGeneratorService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    @Value("${app.payslip.storage-path}")
    private String storagePath;

    @Override
    public String generatePdf(Payslip p) {
        try {
            File dir = new File(storagePath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "payslip_" + p.getEmployee().getEmpCode() + "_" + p.getPayMonth() + "_" + p.getPayYear() + ".pdf";
            String fullPath = storagePath + File.separator + fileName;

            PdfWriter writer = new PdfWriter(new FileOutputStream(fullPath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            String monthName = java.time.Month.of(p.getPayMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            // Header
            document.add(new Paragraph("Future Transformation Company")
                    .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Payslip for " + monthName + " " + p.getPayYear())
                    .setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // Employee details table
            Table empTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
            addRow(empTable, "Employee ID", p.getEmployee().getEmpCode());
            addRow(empTable, "Employee Name", p.getEmployee().getName());
            addRow(empTable, "Department", p.getEmployee().getDepartment());
            addRow(empTable, "Designation", p.getEmployee().getDesignation());
            document.add(empTable);

            // Earnings & Deductions table
            document.add(new Paragraph("Earnings & Deductions").setBold().setFontSize(12));
            Table payTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
            addRow(payTable, "Basic Salary", "₹" + p.getBasicSalary());
            addRow(payTable, "HRA", "₹" + p.getHra());
            addRow(payTable, "Special Allowance", "₹" + p.getSpecialAllowance());
            addRow(payTable, "Gross Salary", "₹" + p.getGrossSalary());
            addRow(payTable, "Deductions (PF/ESI/PT)", "₹" + p.getDeductions());
            addRow(payTable, "LOP Days", String.valueOf(p.getLopDays()));
            addRow(payTable, "LOP Amount", "₹" + p.getLopAmount());
            document.add(payTable);

            // Net Pay - highlighted
            document.add(new Paragraph("Net Pay: ₹" + p.getNetSalary())
                    .setBold().setFontSize(13).setMarginBottom(15));

            // Leave balance summary
            document.add(new Paragraph("Leave Balance Summary").setBold().setFontSize(12));
            Table leaveTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));
            leaveTable.addHeaderCell(headerCell("Casual Leave"));
            leaveTable.addHeaderCell(headerCell("Sick Leave"));
            leaveTable.addHeaderCell(headerCell("Earned Leave"));
            leaveTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getClBalance()))));
            leaveTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getSlBalance()))));
            leaveTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getElBalance()))));
            document.add(leaveTable);

            document.add(new Paragraph("\nThis is a system-generated payslip.")
                    .setFontSize(8).setItalic().setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

            document.close();
            return fullPath;

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF payslip: " + e.getMessage(), e);
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold())
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-"))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
    }

    private Cell headerCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
}