package com.chathuvi.Smart_loan_Pricing_System.services;

import com.chathuvi.Smart_loan_Pricing_System.entity.LoanData;
import com.chathuvi.Smart_loan_Pricing_System.repository.LoanDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private final LoanDataRepository loanDataRepository;

    public byte[] exportDashboardReport() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String username = getLoggedInUsername();

            List<LoanData> loanList = loanDataRepository.findByCreatedBy(username);

            Sheet sheet = workbook.createSheet("Dashboard Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Application ID");
            header.createCell(1).setCellValue("Customer Name");
            header.createCell(2).setCellValue("Loan Amount");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("Created Date");

            int rowNum = 1;
            for (LoanData loan : loanList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(loan.getId() != null ? loan.getId() : 0);
                row.createCell(1).setCellValue(loan.getCustomerName() != null ? loan.getCustomerName() : "");
                row.createCell(2).setCellValue(loan.getLoanAmount() != null ? loan.getLoanAmount() : 0.0);
                row.createCell(3).setCellValue(loan.getStatus() != null ? loan.getStatus() : "");
                row.createCell(4).setCellValue(loan.getCreatedAt() != null ? loan.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to export dashboard report", e);
        }
    }

    private String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return principal.toString();
    }
}
